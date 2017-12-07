package de.dfki.parking.behavior.baseline;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.util.Pair;
import de.dfki.parking.behavior.IExplorationStrategy;
import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.index.ParkingIndexEntry;
import de.dfki.parking.knowledge.ParkingKnowledge;
import de.dfki.parking.knowledge.ParkingKnowledgeEntry;
import de.dfki.parking.utility.ParkingParameters;
import de.dfki.parking.utility.ParkingPreferences;
import de.dfki.parking.utility.ParkingUtility;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class BaselineExplorationStrategy implements IExplorationStrategy {
  // Queried first to find possible parking possibilities from observations
  private ParkingKnowledge knowledge;

  // ParkingIndex in case no parking possibility was found in knowledge
  private ParkingIndex parkingIndex;

  // Preferences to rank parking possibilities
  private ParkingPreferences preferences;

  // Function for evaluating utility of parking spot
  private final ParkingUtility utility;

  // Timespan during which information from knowledge is considered relevant
  private long validTime;

  public BaselineExplorationStrategy(ParkingKnowledge knowledge, ParkingPreferences prefs, 
      ParkingUtility utility, ParkingIndex parkingMap, long validTime) {
    this.knowledge = knowledge;
    this.preferences = prefs;
    this.utility = utility;
    this.parkingIndex = parkingMap;
    this.validTime = validTime;
  }

  @Override
  public Coordinate findNextPossibleParking(Coordinate position, Coordinate destination, long currentTime) {
    // Find all parking possibilities in range from knowledge
    Collection<ParkingKnowledgeEntry> fromKnowledge = knowledge.findParkingNearby(position, 500);

    // See if recent relevant entries in knowledge exist and return them ranked by utility
    List<ParkingIndexEntry> relevant = getRelevantEntriesFromKnowlede(fromKnowledge, position, destination, currentTime);

    if (relevant.size() != 0) {
      // If there is an entry, return position
      ParkingIndexEntry entry = relevant.get(0);

      // Remove current position from list of possible positions
      List<Coordinate> temp = new ObjectArrayList<>(entry.getAllAccessPositions());
     
      while (temp.remove(position)) {}

      // Sample random position to reach
      Coordinate ret = temp.get(ThreadLocalRandom.current().nextInt(temp.size()));
      return ret;
    }
    List<ParkingIndexEntry> fromMap = getPossibleParkingFromIndex(fromKnowledge, position, destination, currentTime);

    if (fromMap.size() != 0) {
      // If there is an entry, return position
      ParkingIndexEntry entry = fromMap.get(0);

      // Remove current position from list of possible positions
      List<Coordinate> temp = new ObjectArrayList<>(entry.getAllAccessPositions());
      
      while (temp.remove(position)) {}
      
      // Sample random position
      Coordinate ret = temp.get(ThreadLocalRandom.current().nextInt(temp.size()));
      return ret;
    }
    return null;
  }

  private List<ParkingIndexEntry> getRelevantEntriesFromKnowlede(Collection<ParkingKnowledgeEntry> fromKnowledge, Coordinate position,
      Coordinate destination, long currentTime) {
    List<ParkingKnowledgeEntry> filtered = new ObjectArrayList<>(fromKnowledge.size());

    for (ParkingKnowledgeEntry entry : fromKnowledge) {
      // Filter by time and free parking spots
      if (((currentTime - entry.getLastUpdate()) / 1000.0 <= validTime) && entry.getNFreeParkingSpots() == 0)
        continue;
      
      if (entry.getNParkingSpots() == 0)
        continue;
 
      filtered.add(entry);
    }
    return rankFromKnowledge(filtered, position, destination);
  }

  private List<ParkingIndexEntry> rankFromKnowledge(List<ParkingKnowledgeEntry> parkings, Coordinate currentPosition, Coordinate destination) {
    List<Pair<ParkingKnowledgeEntry, Double>> temp = new ObjectArrayList<>();

    for (ParkingKnowledgeEntry parking : parkings) {
      double c = parking.getParkingIndexEntry().getParking().getCurrentPricePerHour();
      double wd = Geometry.haversineDistance(parking.getParkingIndexEntry().getReferencePosition(), destination);
      double st = (Geometry.haversineDistance(parking.getParkingIndexEntry().getReferencePosition(), currentPosition) / 3.0);
      temp.add(new Pair<>(parking, utility.computeUtility(new ParkingParameters(c, wd, st), preferences)));
    }
    temp.sort((t1, t2) -> Double.compare(t2.second, t1.second));

    if (temp.size() > 0 && temp.get(0).second == 0.0)
      return new ObjectArrayList<>();
    
    List<ParkingIndexEntry> ret = new ObjectArrayList<>(temp.size());

    for (Pair<ParkingKnowledgeEntry, Double> p : temp) {
      ret.add(p.first.getParkingIndexEntry());
    }
    return ret;
  }

  private List<ParkingIndexEntry> getPossibleParkingFromIndex(Collection<ParkingKnowledgeEntry> fromKnowledge, Coordinate position, Coordinate destination,
      long currentTime) {
    // Filter those which are valid and which have free parking spots
    Collection<ParkingIndexEntry> fromIndex = parkingIndex.getParkingsWithMaxDistance(position, 200);
    // Collection<ParkingIndexEntry> fromIndex = parkingIndex.getAllGarageParkingEntries();

    IntSet knowledgeIds = new IntOpenHashSet();

    for (ParkingKnowledgeEntry entry : fromKnowledge) {

      if (((currentTime - entry.getLastUpdate()) / 1000.0 <= validTime)) {
        knowledgeIds.add(entry.getParkingIndexEntry().getParking().getId());
        continue;
      }
      
      if (entry.getNParkingSpots() == 0) {
        knowledgeIds.add(entry.getParkingIndexEntry().getParking().getId());
        continue;
      }
    }

    List<ParkingIndexEntry> ret = new ObjectArrayList<>();

    for (ParkingIndexEntry entry : fromIndex) {

      if (knowledgeIds.contains(entry.getParking().getId()))
        continue;
      ret.add(entry);
    }
    return rankFromIndex(ret, position, destination);
  }
  
  private List<ParkingIndexEntry> rankFromIndex(List<ParkingIndexEntry> parkings, Coordinate currentPosition, Coordinate destination) {
    List<Pair<ParkingIndexEntry, Double>> temp = new ObjectArrayList<>();

    for (ParkingIndexEntry parking : parkings) {
      double c = 0.0;
      double wd = Geometry.haversineDistance(parking.getReferencePosition(), destination);
      double st = (Geometry.haversineDistance(parking.getReferencePosition(), currentPosition) / 3.0);
      temp.add(new Pair<>(parking, utility.computeUtility(new ParkingParameters(c, wd, st), preferences)));
    }
    temp.sort((t1, t2) -> Double.compare(t2.second, t1.second));

    if (temp.size() > 0 && temp.get(0).second == 0.0)
      return new ObjectArrayList<>();
    
    List<ParkingIndexEntry> ret = new ObjectArrayList<>(temp.size());

    for (Pair<ParkingIndexEntry, Double> p : temp) {
      ret.add(p.first);
    }
    return ret;
  }
}
