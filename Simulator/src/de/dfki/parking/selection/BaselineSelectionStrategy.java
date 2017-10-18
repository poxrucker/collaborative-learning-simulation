package de.dfki.parking.selection;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.util.Triple;
import de.dfki.parking.knowledge.ParkingKnowledge;
import de.dfki.parking.knowledge.ParkingKnowledge.ParkingKnowledgeEntry;
import de.dfki.parking.model.ParkingPreferences;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class BaselineSelectionStrategy implements IParkingSelectionStrategy {
  // Local ParkingMap instance
  private final ParkingKnowledge knowledge;
  
  // Parking preferences
  private final ParkingPreferences preferences;
  
  // Time during which information from parking maps is considered valid
  private final long validTime;
  
  public BaselineSelectionStrategy(ParkingKnowledge knowledge, ParkingPreferences preferences, long validTime) {
    this.knowledge = knowledge;
    this.preferences = preferences;
    this.validTime = validTime;
  }
  
  @Override
  public List<ParkingPossibility> selectParking(Coordinate currentPosition, Coordinate destination, long currentTime) {
    // Filter those which are completely occupied
    List<ParkingKnowledgeEntry> freeParkings = findPossibleParkings(destination, preferences.getWdmax(), currentTime);

    if (freeParkings.size() == 0)
      return new ObjectArrayList<>(0);
    
    // If there is free parking possibilities, select a random one. Otherwise return null
    return rank(freeParkings, currentPosition, destination);
  }

  private List<ParkingKnowledgeEntry> findPossibleParkings(Coordinate destination, double maxDistance, long currentTime) {
    // Get possibilities from parking maps
    List<ParkingKnowledgeEntry> initial = knowledge.findParkingNearby(destination, maxDistance);
    
    // Filter those which are valid and which have free parking spots
    List<ParkingKnowledgeEntry> possible = new ObjectArrayList<>(initial.size());
    
    for (ParkingKnowledgeEntry entry : initial) {
      // Filter by time
      if ((entry.getLastUpdate() - currentTime) / 1000.0 > validTime)
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
      double c = parking.getParkingMapEntry().getParking().getCurrentPricePerHour();
      Coordinate pos = parking.getParkingMapEntry().getNodes().get(ThreadLocalRandom.current().nextInt(parking.getParkingMapEntry().getNodes().size())).getPosition();
      double wd = Geometry.haversineDistance(pos, destination);
      double st = (Geometry.haversineDistance(pos, currentPosition) / 4.1);
      temp.add(new Triple<>(parking, pos, calculateUtility(c, wd, st)));
    }
    temp.sort((t1, t2) -> (int) (t2.third - t1.third));
    
    List<ParkingPossibility> ret = new ObjectArrayList<>(temp.size());
    
    for (Triple<ParkingKnowledgeEntry, Coordinate, Double> p : temp) {
      ret.add(new ParkingPossibility(p.first.getParkingMapEntry().getParking(), p.second));
    }
    return ret;
  }

  private double calculateUtility(double c, double wd, double st) {
    c = Math.min(c, preferences.getCmax());
    wd = Math.min(wd, preferences.getWdmax());
    st = Math.min(st, preferences.getStmax());   
    double cfc = preferences.getCweight() * (1 - c / preferences.getCmax());
    double cfwd = preferences.getWdweight() * (1 - wd / preferences.getWdmax());
    double cfst = preferences.getStweight() * (1 - st / preferences.getStmax());
    return cfc + cfwd + cfst;
  }
}
