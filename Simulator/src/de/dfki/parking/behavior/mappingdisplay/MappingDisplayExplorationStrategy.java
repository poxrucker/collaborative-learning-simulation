package de.dfki.parking.behavior.mappingdisplay;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.util.Triple;
import de.dfki.parking.behavior.IExplorationStrategy;
import de.dfki.parking.behavior.ParkingPreferences;
import de.dfki.parking.behavior.ParkingUtility;
import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.index.ParkingIndexEntry;
import de.dfki.parking.knowledge.ParkingKnowledge;
import de.dfki.parking.knowledge.ParkingKnowledgeEntry;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MappingDisplayExplorationStrategy implements IExplorationStrategy {
  // Local knowledge of the entity
  private final ParkingKnowledge localKnowledge;
  
  // Global knowledge of the system
  private final ParkingKnowledge globalKnowledge;
  
  // ParkingIndex in case no 
  private final ParkingIndex parkingIndex;

  // Preferences to rank parking possibilities
  private final ParkingPreferences preferences;
 
  // Function for evaluating utility of parking spot
  private final ParkingUtility utility;
  
  // Timespan during which information from knowledge is considered relevant
  private long validTime;

 public MappingDisplayExplorationStrategy(ParkingKnowledge localKnowledge, ParkingKnowledge globalKnowledge, 
     ParkingPreferences prefs, ParkingUtility utility, ParkingIndex parkingIndex, long validTime) {
   this.localKnowledge = localKnowledge;
   this.globalKnowledge = globalKnowledge;
   this.preferences = prefs;
   this.utility = utility;
   this.parkingIndex = parkingIndex;
   this.validTime = validTime;
 }

 @Override
 public Coordinate findNextPossibleParking(Coordinate position, Coordinate destination, long currentTime) {
   // Find all parking possibilities in range from knowledge
   Collection<ParkingKnowledgeEntry> fromLocalKnowledge = localKnowledge.findParkingNearby(destination, 500);
   Collection<ParkingKnowledgeEntry> fromGlobalKnowledge = globalKnowledge.findParkingNearby(destination, 500);
   List<ParkingKnowledgeEntry> fromKnowledge = mergeByTime(fromLocalKnowledge, fromGlobalKnowledge);
   
   // See if recent relevant entries in knowledge exist and return them ranked by utility
   List<ParkingIndexEntry> relevant = getRelevantEntriesFromKnowlede(fromKnowledge, position, destination, currentTime);
   
   if (relevant.size() != 0) {
     // If there is an entry, return position
     ParkingIndexEntry entry = relevant.get(0);
     
     // Sample random position to reach
     Coordinate ret = entry.getAllAccessPositions().get(ThreadLocalRandom.current().nextInt(entry.getAllAccessPositions().size()));
     return ret;
   }
   
   List<ParkingIndexEntry> fromMap = getPossibleParkingFromIndex(fromKnowledge, position, destination, currentTime);
   
   if (fromMap.size() != 0) {
     // If there is an entry, return position
     ParkingIndexEntry entry = fromMap.get(0);
     
     // Sample random position to reach
     Coordinate ret = entry.getAllAccessPositions().get(ThreadLocalRandom.current().nextInt(entry.getAllAccessPositions().size()));
     return ret;
   }
  return null; 
 }
 
 private List<ParkingKnowledgeEntry> mergeByTime(Collection<ParkingKnowledgeEntry> local, Collection<ParkingKnowledgeEntry> global) {
   // Create a map to merge entries 
   List<ParkingKnowledgeEntry> merged = new ObjectArrayList<>(local.size() + global.size());
   merged.addAll(local);
   merged.addAll(global);
   merged.sort((e1, e2) -> Long.compare(e2.getLastUpdate(), e1.getLastUpdate()));
   
   List<ParkingKnowledgeEntry> ret = new ObjectArrayList<>();
   IntSet added = new IntOpenHashSet();
   
   for (ParkingKnowledgeEntry entry : merged) {
     
     if (added.contains(entry.getParkingIndexEntry().getParking().getId()))
       continue;
     ret.add(entry);
     added.add(entry.getParkingIndexEntry().getParking().getId());
   }
   return ret;
 }
 
 private List<ParkingIndexEntry> getRelevantEntriesFromKnowlede(List<ParkingKnowledgeEntry> fromKnowledge, 
     Coordinate position, Coordinate destination, long currentTime) {
   List<ParkingKnowledgeEntry> filtered = new ObjectArrayList<>(fromKnowledge.size());
   
   for (ParkingKnowledgeEntry entry : fromKnowledge) {
     // Filter by free parking spots
     if (((currentTime - entry.getLastUpdate()) / 1000.0 <= validTime) && entry.getNFreeParkingSpots() == 0)
       continue;
     
     if (entry.getParkingIndexEntry().getParking().getNumberOfParkingSpots() == 0)
       continue;
     
     filtered.add(entry);
   }
   return rankFromKnowledge(filtered, position, destination);
 }
 
 private List<ParkingIndexEntry> rankFromKnowledge(List<ParkingKnowledgeEntry> parkings, Coordinate currentPosition, Coordinate destination) {
   List<Triple<ParkingKnowledgeEntry, Coordinate, Double>> temp = new ObjectArrayList<>();
   
   for (ParkingKnowledgeEntry parking : parkings) {
     double c = parking.getParkingIndexEntry().getParking().getCurrentPricePerHour();
     Coordinate pos = parking.getParkingIndexEntry().getAllAccessPositions().get(ThreadLocalRandom.current().nextInt(parking.getParkingIndexEntry().getAllAccessPositions().size()));
     double wd = Geometry.haversineDistance(pos, destination);
     double st = (Geometry.haversineDistance(pos, currentPosition) / 4.1);
     temp.add(new Triple<>(parking, pos, utility.computeUtility(new Triple<>(c, wd, st), preferences)));
   }
   temp.sort((t1, t2) -> Double.compare(t2.third, t1.third));
   
   if (temp.size() > 0 && temp.get(0).third == 0.0)
     return new ObjectArrayList<>();
   
   List<ParkingIndexEntry> ret = new ObjectArrayList<>(temp.size());
   
   for (Triple<ParkingKnowledgeEntry, Coordinate, Double> p : temp) {
     ret.add(p.first.getParkingIndexEntry());
   }
   return ret;
 }
 
 private List<ParkingIndexEntry> getPossibleParkingFromIndex(List<ParkingKnowledgeEntry> fromKnowledge, Coordinate position, Coordinate destination, long currentTime) {
   // Filter those which are valid and which have free parking spots
   Collection<ParkingIndexEntry> fromIndex = parkingIndex.getParkingsWithMaxDistance(destination, 500);
   //List<ParkingIndexEntry> fromIndex = parkingIndex.getAllGarageParkingEntries();
   IntSet knowledgeIds = new IntOpenHashSet();
   
   for (ParkingKnowledgeEntry entry : fromKnowledge) {
     
     if (((currentTime - entry.getLastUpdate()) / 1000.0 <= validTime) && entry.getNFreeParkingSpots() == 0) {
       knowledgeIds.add(entry.getParkingIndexEntry().getParking().getId());
       continue;
     }
     
     if (entry.getParkingIndexEntry().getParking().getNumberOfParkingSpots() == 0) {
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
   List<Triple<ParkingIndexEntry, Coordinate, Double>> temp = new ObjectArrayList<>();

   for (ParkingIndexEntry parking : parkings) {
     double c = 0.0;
     Coordinate pos = parking.getReferencePosition();
     double wd = Geometry.haversineDistance(pos, destination);
     double st = (Geometry.haversineDistance(pos, currentPosition) / 4.1);
     temp.add(new Triple<>(parking, pos, utility.computeUtility(new Triple<>(c, wd, st), preferences)));
   }
   temp.sort((t1, t2) -> Double.compare(t2.third, t1.third));

   if (temp.size() > 0 && temp.get(0).third == 0.0)
     return new ObjectArrayList<>();
   
   List<ParkingIndexEntry> ret = new ObjectArrayList<>(temp.size());

   for (Triple<ParkingIndexEntry, Coordinate, Double> p : temp) {
     ret.add(p.first);
   }
   return ret;
 }
}
