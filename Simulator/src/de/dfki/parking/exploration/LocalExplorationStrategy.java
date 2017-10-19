package de.dfki.parking.exploration;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.util.Triple;
import de.dfki.parking.knowledge.ParkingKnowledge;
import de.dfki.parking.knowledge.ParkingKnowledge.ParkingKnowledgeEntry;
import de.dfki.parking.model.ParkingMap;
import de.dfki.parking.model.ParkingMap.ParkingMapEntry;
import de.dfki.parking.model.ParkingPreferences;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class LocalExplorationStrategy implements IExplorationStrategy {
  // Queried first to find possible parking possibilities from observations
  private ParkingKnowledge knowledge;
  
  // ParkingMap in case no parking possibility was found in knowledge
  private ParkingMap parkingMap;

  // Preferences to rank parking possibilities
  private ParkingPreferences preferences;
  
  // Timespan during which information from knowledge is considered relevant
  private long validTime;
   
  public LocalExplorationStrategy(ParkingKnowledge knowledge, ParkingPreferences prefs, 
      ParkingMap parkingMap, long validTime) {
    this.knowledge = knowledge;
    this.preferences = prefs;
    this.parkingMap = parkingMap;
    this.validTime = validTime;
  }
  
  @Override
  public Coordinate findNextPossibleParking(Coordinate position, Coordinate destination, long currentTime) {
    // Find all parking possibilities in range from knowledge
    List<ParkingKnowledgeEntry> fromKnowledge = knowledge.findParkingNearby(destination, 500);

    // See if recent relevant entries in knowledge exist and return them ranked by utility
    List<ParkingMapEntry> relevant = getRelevantEntriesFromKnowlede(fromKnowledge, position, destination, currentTime);
    
    if (relevant.size() != 0) {
      // If there is an entry, return position
      ParkingMapEntry entry = relevant.get(0);
      
      // Sample random position to reach
      Coordinate ret = entry.getNodes().get(ThreadLocalRandom.current().nextInt(entry.getNodes().size())).getPosition();
      return ret;
    }
    
    List<ParkingMapEntry> fromMap = getPossibleParkingFromMap(fromKnowledge, position, destination, currentTime);
    
    if (fromMap.size() != 0) {
      // If there is an entry, return position
      ParkingMapEntry entry = fromMap.get(0);
      
      // Sample random position to reach
      Coordinate ret = entry.getNodes().get(ThreadLocalRandom.current().nextInt(entry.getNodes().size())).getPosition();
      return ret;
    }
   return null; 
  }
  
  private List<ParkingMapEntry> getRelevantEntriesFromKnowlede(List<ParkingKnowledgeEntry> fromKnowledge, 
      Coordinate position, Coordinate destination, long currentTime) {
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
  
  private List<ParkingMapEntry> rankFromKnowledge(List<ParkingKnowledgeEntry> parkings, Coordinate currentPosition, Coordinate destination) {
    List<Triple<ParkingKnowledgeEntry, Coordinate, Double>> temp = new ObjectArrayList<>();
    
    for (ParkingKnowledgeEntry parking : parkings) {
      double c = parking.getParkingMapEntry().getParking().getCurrentPricePerHour();
      Coordinate pos = parking.getParkingMapEntry().getNodes().get(ThreadLocalRandom.current().nextInt(parking.getParkingMapEntry().getNodes().size())).getPosition();
      double wd = Geometry.haversineDistance(pos, destination);
      double st = (Geometry.haversineDistance(pos, currentPosition) / 4.1);
      temp.add(new Triple<>(parking, pos, calculateUtility(c, wd, st)));
    }
    temp.sort((t1, t2) -> (int) (t2.third - t1.third));
    
    List<ParkingMapEntry> ret = new ObjectArrayList<>(temp.size());
    
    for (Triple<ParkingKnowledgeEntry, Coordinate, Double> p : temp) {
      ret.add(p.first.getParkingMapEntry());
    }
    return ret;
  }
  
  private List<ParkingMapEntry> getPossibleParkingFromMap(List<ParkingKnowledgeEntry> fromKnowledge, Coordinate position, Coordinate destination, long currentTime) {
    // Filter those which are valid and which have free parking spots
    List<ParkingMapEntry> fromMap = parkingMap.getParkingsWithMaxDistance(destination, 500);
    IntSet knowledgeIds = new IntOpenHashSet();
    
    for (ParkingKnowledgeEntry entry : fromKnowledge) {
      knowledgeIds.add(entry.getParkingMapEntry().getParking().getId());
    }
    
    List<ParkingMapEntry> ret = new ObjectArrayList<>();
    
    for (ParkingMapEntry entry : fromMap) {
      
      if (knowledgeIds.contains(entry.getParking().getId()))
        continue;
      ret.add(entry);
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
