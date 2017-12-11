package de.dfki.parking.behavior.mappingdisplay;

import java.util.Collection;
import java.util.List;

import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.util.Pair;
import allow.simulator.world.StreetNode;
import de.dfki.parking.behavior.IParkingSelectionStrategy;
import de.dfki.parking.behavior.ParkingPossibility;
import de.dfki.parking.knowledge.ParkingKnowledge;
import de.dfki.parking.knowledge.ParkingKnowledgeEntry;
import de.dfki.parking.utility.ParkingParameters;
import de.dfki.parking.utility.ParkingPreferences;
import de.dfki.parking.utility.ParkingUtility;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class MappingDisplaySelectionStrategy implements IParkingSelectionStrategy {
  // Local ParkingMap instance
  private final ParkingKnowledge localParkingMap;

  // Global ParkingMap instance
  private final ParkingKnowledge globalParkingMap;

  // Parking preferences
  private final ParkingPreferences preferences;

  // Function for evaluating utility of parking spot
  private final ParkingUtility utility;

  // Time during which information from parking maps is considered valid
  private final long validTime;

  public MappingDisplaySelectionStrategy(ParkingKnowledge localParkingMap, ParkingKnowledge globalParkingMap, 
      ParkingPreferences preferences, ParkingUtility utility, long validTime) {
    this.localParkingMap = localParkingMap;
    this.globalParkingMap = globalParkingMap;
    this.preferences = preferences;
    this.utility = utility;
    this.validTime = validTime;
  }

  @Override
  public List<ParkingPossibility> selectParking(StreetNode current, Coordinate destination, long currentTime) {
    // Find possible parking possibilities in knowledge
    List<ParkingKnowledgeEntry> freeParkings = findPossibleParkings(current, destination, currentTime);

    // Rank possibilities or return empty list
    return (freeParkings.size() > 0) ? rank(freeParkings, current.getPosition(), destination) : new ObjectArrayList<>();
  }

  private List<ParkingKnowledgeEntry> findPossibleParkings(StreetNode current, Coordinate destination, long currentTime) {
    // Get possibilities from parking maps
    Collection<ParkingKnowledgeEntry> local = localParkingMap.findStreetParking(current);
    local.addAll(localParkingMap.findGarageParking(current));

    Collection<ParkingKnowledgeEntry> global = globalParkingMap.findStreetParking(current);
    global.addAll(globalParkingMap.findGarageParking(current));

    Collection<ParkingKnowledgeEntry> merged = mergeByTime(local, global);

    // Filter those which are valid and which have free parking spots
    List<ParkingKnowledgeEntry> possible = new ObjectArrayList<>(local.size());

    for (ParkingKnowledgeEntry entry : merged) {
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

  private Collection<ParkingKnowledgeEntry> mergeByTime(Collection<ParkingKnowledgeEntry> local, Collection<ParkingKnowledgeEntry> global) {
    // Create a map to merge entries
    List<ParkingKnowledgeEntry> merged = new ObjectArrayList<>(local.size() + global.size());
    merged.addAll(local);
    merged.addAll(global);
    merged.sort((e1, e2) -> Long.compare(e2.getLastUpdate(), e1.getLastUpdate()));

    List<ParkingKnowledgeEntry> ret = new ObjectArrayList<>();
    IntSet addedParking = new IntOpenHashSet();
    
    for (ParkingKnowledgeEntry entry : merged) {

      if (addedParking.contains(entry.getParkingIndexEntry().getParking().getId()))
        continue;
      ret.add(entry);
      addedParking.add(entry.getParkingIndexEntry().getParking().getId());
    }
    return ret;
  }

  private List<ParkingPossibility> rank(List<ParkingKnowledgeEntry> parkings, Coordinate currentPosition, Coordinate destination) {
    List<Pair<ParkingKnowledgeEntry, Double>> temp = new ObjectArrayList<>();

    for (ParkingKnowledgeEntry parking : parkings) {
      double c = parking.getParkingIndexEntry().getParking().getCurrentPricePerHour();
      double wd = Geometry.haversineDistance(parking.getParkingIndexEntry().getReferencePosition(), destination);
      double st = (Geometry.haversineDistance(parking.getParkingIndexEntry().getReferencePosition(), currentPosition) / 3.0);
      temp.add(new Pair<>(parking, utility.computeUtility(new ParkingParameters(c, wd, st), preferences)));
    }
    temp.sort((t1, t2) -> (int) (t1.second - t2.second));

    if (temp.size() > 0 && temp.get(0).second == 0.0)
      return new ObjectArrayList<>(0);
    
    List<ParkingPossibility> ret = new ObjectArrayList<>(temp.size());

    for (Pair<ParkingKnowledgeEntry, Double> p : temp) {
      // Remove current position from list of possible positions
      //List<Coordinate> positions = new ObjectArrayList<>(p.first.getParkingIndexEntry().getAllAccessPositions());
      
      // Remove current position from list of possible positions
      //while (positions.remove(currentPosition)) {}
      
      // ret.add(new ParkingPossibility(p.first.getParkingIndexEntry().getParking(), positions.get(ThreadLocalRandom.current().nextInt(positions.size()))));
      ret.add(new ParkingPossibility(p.first.getParkingIndexEntry().getParking(), new Coordinate(currentPosition.x, currentPosition.y)));
    }
    return ret;
  }
}
