package de.dfki.parking.behavior.mappingdisplay;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.util.Pair;
import allow.simulator.world.StreetNode;
import de.dfki.parking.behavior.IParkingSelectionStrategy;
import de.dfki.parking.behavior.ParkingPossibility;
import de.dfki.parking.knowledge.ParkingMap;
import de.dfki.parking.knowledge.ParkingMapEntry;
import de.dfki.parking.utility.ParkingParameters;
import de.dfki.parking.utility.ParkingPreferences;
import de.dfki.parking.utility.ParkingUtility;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class MappingDisplaySelectionStrategy implements IParkingSelectionStrategy {
  // Local ParkingMap instance
  private final ParkingMap localParkingMap;

  // Global ParkingMap instance
  private final ParkingMap globalParkingMap;

  // Parking preferences
  private final ParkingPreferences preferences;

  // Function for evaluating utility of parking spot
  private final ParkingUtility utility;

  // Time during which information from parking maps is considered valid
  private final long validTime;

  public MappingDisplaySelectionStrategy(ParkingMap localParkingMap, ParkingMap globalParkingMap, 
      ParkingPreferences preferences, ParkingUtility utility, long validTime) {
    this.localParkingMap = localParkingMap;
    this.globalParkingMap = globalParkingMap;
    this.preferences = preferences;
    this.utility = utility;
    this.validTime = validTime;
  }

  @Override
  public ParkingPossibility selectParking(StreetNode current, Coordinate destination, long currentTime, long arrivalTime) {
    // Find possible parking possibilities in knowledge
    List<ParkingMapEntry> freeParkings = findPossibleParkings(current, destination, currentTime);

    if (freeParkings == null)
      return null;
    
    List<ParkingPossibility> rankedParkings = rank(freeParkings, current.getPosition(), destination);
    
    // Rank possibilities or return empty list
    return (rankedParkings.size() > 0) ? rankedParkings.get(0) : null;
  }

  private List<ParkingMapEntry> findPossibleParkings(StreetNode current, Coordinate destination, long currentTime) {
    // Get possibilities from parking maps
    Collection<ParkingMapEntry> local = localParkingMap.findStreetParking(current);
    local.addAll(localParkingMap.findGarageParking(current));
    // Collection<ParkingMapEntry> local = localParkingMap.findParkingNearby(destination, 250);
 
    Collection<ParkingMapEntry> global = globalParkingMap.findStreetParking(current);
    global.addAll(globalParkingMap.findGarageParking(current));
    // Collection<ParkingMapEntry> global = globalParkingMap.findParkingNearby(destination, 250);

    Collection<ParkingMapEntry> merged = mergeByTime(local, global);

    // Filter those which are valid and which have free parking spots
    List<ParkingMapEntry> possible = new ObjectArrayList<>(local.size());

    for (ParkingMapEntry entry : merged) {
      // Filter by time
      if ((entry.getLastUpdate() < 0) || (currentTime - entry.getLastUpdate()) / 1000.0 > validTime)
        continue;

      // Filter by free parking spots
      if (entry.getNFreeParkingSpots() == 0)
        continue;

      possible.add(entry);
    }
    return possible;
  }

  private Collection<ParkingMapEntry> mergeByTime(Collection<ParkingMapEntry> local, Collection<ParkingMapEntry> global) {
    // Create a map to merge entries
    List<ParkingMapEntry> merged = new ObjectArrayList<>(local.size() + global.size());
    merged.addAll(local);
    merged.addAll(global);
    merged.sort((e1, e2) -> Long.compare(e2.getLastUpdate(), e1.getLastUpdate()));

    List<ParkingMapEntry> ret = new ObjectArrayList<>();
    IntSet addedParking = new IntOpenHashSet();
    
    for (ParkingMapEntry entry : merged) {

      if (addedParking.contains(entry.getParkingIndexEntry().getParking().getId()))
        continue;
      ret.add(entry);
      addedParking.add(entry.getParkingIndexEntry().getParking().getId());
    }
    return ret;
  }

  private List<ParkingPossibility> rank(List<ParkingMapEntry> parkings, Coordinate currentPosition, Coordinate destination) {
    List<Pair<ParkingMapEntry, Double>> temp = new ObjectArrayList<>();

    for (ParkingMapEntry parking : parkings) {
      double c = parking.getParkingIndexEntry().getParking().getDefaultPricePerHour();
      double wd = Geometry.haversineDistance(parking.getParkingIndexEntry().getReferencePosition(), destination);
      double st = (Geometry.haversineDistance(parking.getParkingIndexEntry().getReferencePosition(), currentPosition) / 3.0);
      temp.add(new Pair<>(parking, utility.computeUtility(new ParkingParameters(c, wd, st), preferences)));
    }
    temp.sort((t1, t2) -> Double.compare(t2.second, t1.second));

    if (temp.size() > 0 && temp.get(0).second == 0.0)
      return new ObjectArrayList<>(0);
    
    List<ParkingPossibility> ret = new ObjectArrayList<>(temp.size());

    for (Pair<ParkingMapEntry, Double> p : temp) {
      List<Coordinate> positions = p.first.getParkingIndexEntry().getPositions();
      Coordinate t = null;
      
      if (positions.contains(currentPosition)) {
        t = currentPosition;  
        
      } else {
        t = positions.get(ThreadLocalRandom.current().nextInt(positions.size()));
      }
      ret.add(new ParkingPossibility(p.first.getParkingIndexEntry().getParking(), new Coordinate(t.x, t.y), p.second));
    }
    return ret;
  }
}
