package de.dfki.parking.exploration;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.util.Triple;
import de.dfki.parking.knowledge.ParkingKnowledge;
import de.dfki.parking.knowledge.ParkingKnowledge.ParkingKnowledgeEntry;
import de.dfki.parking.model.ParkingIndex;
import de.dfki.parking.model.ParkingIndex.ParkingIndexEntry;
import de.dfki.parking.model.ParkingPreferences;
import de.dfki.parking.model.ParkingUtility;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class LocalExplorationStrategy implements IExplorationStrategy {

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

  public LocalExplorationStrategy(ParkingKnowledge knowledge, ParkingPreferences prefs, 
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
    List<ParkingKnowledgeEntry> fromKnowledge = knowledge.findParkingNearby(destination, 500);

    // See if recent relevant entries in knowledge exist and return them ranked
    // by utility
    List<ParkingIndexEntry> relevant = getRelevantEntriesFromKnowlede(fromKnowledge, position, destination, currentTime);

    if (relevant.size() != 0) {
      // If there is an entry, return position
      ParkingIndexEntry entry = relevant.get(0);

      // Sample random position to reach
      Coordinate ret = entry.getNodes().get(ThreadLocalRandom.current().nextInt(entry.getNodes().size())).getPosition();
      return ret;
    }

    List<ParkingIndexEntry> fromMap = getPossibleParkingFromMap(fromKnowledge, position, destination, currentTime);

    if (fromMap.size() != 0) {
      // If there is an entry, return position
      ParkingIndexEntry entry = fromMap.get(0);

      // Sample random position to reach
      Coordinate ret = entry.getNodes().get(ThreadLocalRandom.current().nextInt(entry.getNodes().size())).getPosition();
      return ret;
    }
    return null;
  }

  private List<ParkingIndexEntry> getRelevantEntriesFromKnowlede(List<ParkingKnowledgeEntry> fromKnowledge, Coordinate position,
      Coordinate destination, long currentTime) {
    List<ParkingKnowledgeEntry> filtered = new ObjectArrayList<>(fromKnowledge.size());

    for (ParkingKnowledgeEntry entry : fromKnowledge) {
      // Filter by time
      if ((entry.getLastUpdate() - currentTime) / 1000.0 > validTime)
        continue;

      // Filter by free parking spots
      if (entry.getNFreeParkingSpots() == 0)
        continue;

      filtered.add(entry);
    }
    return rankFromKnowledge(filtered, position, destination);
  }

  private List<ParkingIndexEntry> rankFromKnowledge(List<ParkingKnowledgeEntry> parkings, Coordinate currentPosition, Coordinate destination) {
    List<Triple<ParkingKnowledgeEntry, Coordinate, Double>> temp = new ObjectArrayList<>();

    for (ParkingKnowledgeEntry parking : parkings) {
      double c = parking.getParkingMapEntry().getParking().getCurrentPricePerHour();
      Coordinate pos = parking.getParkingMapEntry().getNodes()
          .get(ThreadLocalRandom.current().nextInt(parking.getParkingMapEntry().getNodes().size())).getPosition();
      double wd = Geometry.haversineDistance(pos, destination);
      double st = (Geometry.haversineDistance(pos, currentPosition) / 4.1);
      temp.add(new Triple<>(parking, pos, utility.computeUtility(new Triple<>(c, wd, st), preferences)));
    }
    temp.sort((t1, t2) -> Double.compare(t2.third, t1.third));

    List<ParkingIndexEntry> ret = new ObjectArrayList<>(temp.size());

    for (Triple<ParkingKnowledgeEntry, Coordinate, Double> p : temp) {
      ret.add(p.first.getParkingMapEntry());
    }
    return ret;
  }

  private List<ParkingIndexEntry> getPossibleParkingFromMap(List<ParkingKnowledgeEntry> fromKnowledge, Coordinate position, Coordinate destination,
      long currentTime) {
    // Filter those which are valid and which have free parking spots
    List<ParkingIndexEntry> fromMap = parkingIndex.getParkingsWithMaxDistance(destination, 5000);
    IntSet knowledgeIds = new IntOpenHashSet();

    for (ParkingKnowledgeEntry entry : fromKnowledge) {
      knowledgeIds.add(entry.getParkingMapEntry().getParking().getId());
    }

    List<ParkingIndexEntry> ret = new ObjectArrayList<>();

    for (ParkingIndexEntry entry : fromMap) {

      if (knowledgeIds.contains(entry.getParking().getId()))
        continue;
      ret.add(entry);
    }
    return ret;
  }
}
