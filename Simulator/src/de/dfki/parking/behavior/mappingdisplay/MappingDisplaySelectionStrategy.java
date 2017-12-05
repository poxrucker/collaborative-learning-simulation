package de.dfki.parking.behavior.mappingdisplay;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.util.Triple;
import allow.simulator.world.Street;
import de.dfki.parking.behavior.IParkingSelectionStrategy;
import de.dfki.parking.behavior.ParkingPossibility;
import de.dfki.parking.behavior.ParkingPreferences;
import de.dfki.parking.behavior.ParkingUtility;
import de.dfki.parking.knowledge.ParkingKnowledge;
import de.dfki.parking.knowledge.ParkingKnowledgeEntry;
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
  public List<ParkingPossibility> selectParking(Street current, Coordinate position, Coordinate destination, long currentTime) {
    // Filter those which are completely occupied
    List<ParkingKnowledgeEntry> freeParkings = findPossibleParkings(current, destination, currentTime);

    if (freeParkings.size() == 0)
      return new ObjectArrayList<>(0);

    // If there is free parking possibilities, select a random one. Otherwise
    // return null
    return rank(freeParkings, position, destination);
  }

  private List<ParkingKnowledgeEntry> findPossibleParkings(Street current, Coordinate destination, long currentTime) {
    // Get possibilities from parking maps
    Collection<ParkingKnowledgeEntry> local = localParkingMap.findStreetParking(current);
    local.addAll(localParkingMap.findGarageParking(current.getEndNode()));

    Collection<ParkingKnowledgeEntry> global = globalParkingMap.findStreetParking(current);
    global.addAll(globalParkingMap.findGarageParking(current.getEndNode()));

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
    List<Triple<ParkingKnowledgeEntry, Coordinate, Double>> temp = new ObjectArrayList<>();

    for (ParkingKnowledgeEntry parking : parkings) {
      double c = parking.getParkingIndexEntry().getParking().getCurrentPricePerHour();
      Coordinate pos = parking.getParkingIndexEntry().getReferencePosition();
      double wd = Geometry.haversineDistance(pos, destination);
      double st = (Geometry.haversineDistance(pos, currentPosition) / 3.0);
      temp.add(new Triple<>(parking, pos, utility.computeUtility(new Triple<>(c, wd, st), preferences)));
    }
    temp.sort((t1, t2) -> (int) (t1.third - t2.third));

    List<ParkingPossibility> ret = new ObjectArrayList<>(temp.size());

    for (Triple<ParkingKnowledgeEntry, Coordinate, Double> p : temp) {
      // Remove current position from list of possible positions
      List<Coordinate> positions = new ObjectArrayList<>(p.first.getParkingIndexEntry().getAllAccessPositions());
      
      // Remove current position from list of possible positions
      while (positions.remove(currentPosition)) {}
      
      ret.add(new ParkingPossibility(p.first.getParkingIndexEntry().getParking(), positions.get(ThreadLocalRandom.current().nextInt(positions.size()))));
    }
    return ret;
  }
}
