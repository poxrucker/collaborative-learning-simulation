package de.dfki.parking.behavior.baseline;

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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class BaselineSelectionStrategy implements IParkingSelectionStrategy {
  // Local ParkingMap instance
  private final ParkingMap knowledge;

  // Parking preferences
  private final ParkingPreferences preferences;

  // Function for evaluating utility of parking spot
  private final ParkingUtility utility;

  // Time during which information from parking maps is considered valid
  private final long validTime;

  public BaselineSelectionStrategy(ParkingMap knowledge, ParkingPreferences preferences,
      ParkingUtility utility, long validTime) {
    this.knowledge = knowledge;
    this.preferences = preferences;
    this.utility = utility;
    this.validTime = validTime;
  }

  @Override
  public ParkingPossibility selectParking(StreetNode current, Coordinate destination, long currentTime, long arrivalTime) {
    // Find possible parking possibilities in knowledge
    List<ParkingMapEntry> freeParkings = findPossibleParkings(current, destination, currentTime);
    
    if (freeParkings.size() == 0)
      return null;
    
    // Rank possibilities or return empty list
    List<ParkingPossibility> rankedParkings = rank(freeParkings, current.getPosition(), destination);
    return (rankedParkings.size() > 0) ? rankedParkings.get(0) : null;
  }

  private List<ParkingMapEntry> findPossibleParkings(StreetNode current, Coordinate destination, long currentTime) {
    // Get possibilities from parking maps
    Collection<ParkingMapEntry> initial = knowledge.findStreetParking(current);
    initial.addAll(knowledge.findGarageParking(current));
    // Collection<ParkingMapEntry> initial = knowledge.findParkingNearby(destination, 250);

    // Filter those which are valid and which have free parking spots
    List<ParkingMapEntry> possible = new ObjectArrayList<>(initial.size());

    for (ParkingMapEntry entry : initial) {
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
