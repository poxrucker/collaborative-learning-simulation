package de.dfki.parking.behavior;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.util.Triple;
import allow.simulator.world.Street;
import de.dfki.parking.knowledge.ParkingKnowledge;
import de.dfki.parking.knowledge.ParkingKnowledge.ParkingKnowledgeEntry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class BaselineSelectionStrategy implements IParkingSelectionStrategy {

  // Local ParkingMap instance
  private final ParkingKnowledge knowledge;

  // Parking preferences
  private final ParkingPreferences preferences;

  // Function for evaluating utility of parking spot
  private final ParkingUtility utility;

  // Time during which information from parking maps is considered valid
  private final long validTime;

  public BaselineSelectionStrategy(ParkingKnowledge knowledge, ParkingPreferences preferences,
      ParkingUtility utility, long validTime) {
    this.knowledge = knowledge;
    this.preferences = preferences;
    this.utility = utility;
    this.validTime = validTime;
  }

  @Override
  public List<ParkingPossibility> selectParking(Street current, Coordinate destination, long currentTime) {
    // Filter those which are completely occupied
    List<ParkingKnowledgeEntry> freeParkings = findPossibleParkings(current, destination, currentTime);

    if (freeParkings.size() == 0)
      return new ObjectArrayList<>(0);

    // If there is free parking possibilities, select a random one. Otherwise
    // return null
    return rank(freeParkings, current.getEndNode().getPosition(), destination);
  }

  private List<ParkingKnowledgeEntry> findPossibleParkings(Street current, Coordinate destination, long currentTime) {
    // Get possibilities from parking maps
    List<ParkingKnowledgeEntry> initial = knowledge.findParkingInStreet(current);

    // Filter those which are valid and which have free parking spots
    List<ParkingKnowledgeEntry> possible = new ObjectArrayList<>(initial.size());

    for (ParkingKnowledgeEntry entry : initial) {
      // Filter by time
      if ((currentTime - entry.getLastUpdate()) / 1000.0 > validTime)
        continue;

      // Filter by free parking spots
      if (entry.getNFreeParkingSpots() == 0)
        continue;

      possible.add(entry);
    }
    return possible;
  }

  private List<ParkingPossibility> rank(List<ParkingKnowledgeEntry> parkings, Coordinate currentPosition, Coordinate destination) {
    List<Triple<ParkingKnowledgeEntry, Coordinate, Double>> temp = new ObjectArrayList<>();

    for (ParkingKnowledgeEntry parking : parkings) {
      double c = parking.getParkingIndexEntry().getParking().getCurrentPricePerHour();
      Coordinate pos = parking.getParkingIndexEntry().getNodes()
          .get(ThreadLocalRandom.current().nextInt(parking.getParkingIndexEntry().getNodes().size()));
      double wd = Geometry.haversineDistance(pos, destination);
      double st = (Geometry.haversineDistance(pos, currentPosition) / 4.1);
      temp.add(new Triple<>(parking, pos, utility.computeUtility(new Triple<>(c, wd, st), preferences)));
    }
    temp.sort((t1, t2) -> (int) (t2.third - t1.third));

    List<ParkingPossibility> ret = new ObjectArrayList<>(temp.size());

    for (Triple<ParkingKnowledgeEntry, Coordinate, Double> p : temp) {
      ret.add(new ParkingPossibility(p.first.getParkingIndexEntry().getParking(), p.second));
    }
    return ret;
  }
}
