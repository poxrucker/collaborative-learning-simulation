package de.dfki.parking.behavior.guidance;

import java.util.Collection;
import java.util.Comparator;

import allow.simulator.util.Coordinate;
import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.index.ParkingIndexEntry;
import de.dfki.parking.knowledge.ParkingKnowledge;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public final class GuidanceSystem {
  
  private static final class RequestComparator implements Comparator<ParkingRequest> {

    @Override
    public int compare(ParkingRequest o1, ParkingRequest o2) {
      return Long.compare(o1.getExpectedArrivalTime(), o2.getExpectedArrivalTime());
    }   
  }  
  private int requestIds;
  
  // Parking knowledge instance providing global view of all parking possibilities
  private final ParkingKnowledge knowledge;
  private ParkingIndex parkingIndex;
  
  // Contains requests submitted to the guidance system
  private final Int2ObjectMap<ParkingRequest> requests;
  
  // Request id to possible parking ids
  private final Int2ObjectMap<IntList> requestAssignment;
  
  // Parking id to assignment
  private final Int2ObjectMap<IntSet> parkingSpotAssignment;
  
  public GuidanceSystem(ParkingKnowledge knowledge) {
    this.knowledge = knowledge;
    requests = new Int2ObjectOpenHashMap<>();
    requestAssignment = new Int2ObjectOpenHashMap<>();
    parkingSpotAssignment = new Int2ObjectOpenHashMap<>();
  }
  
  public int addRequest(ParkingRequest request) {    
    // Add request 
    int requestId = getNextRequestId();
    requests.put(requestId, request);
    
    // Get all parking possibilities for request
    Coordinate dest = request.getDestination();
    double maxDistance = request.getPreferences().getWdMax();
    Collection<ParkingIndexEntry> entries = parkingIndex.getParkingsWithMaxDistance(dest, maxDistance);   
    IntList possibleParking = new IntArrayList(entries.size());
    
    for (ParkingIndexEntry entry : entries) {
      possibleParking.add(entry.getParking().getId());
      IntSet assignment = parkingSpotAssignment.get(entry.getParking().getId());
      
      if (assignment == null) {
        assignment = new IntOpenHashSet();
        parkingSpotAssignment.put(entry.getParking().getId(), assignment);
      }
      assignment.add(requestId);
    }
    
    requestAssignment.put(requestId, possibleParking);
    return requestId;
  }
 
  public ParkingResponse getParkingPossibility(int requestId) {
    // Get original request
    return null;
  }
  
  private int getNextRequestId() {
    return requestIds++;
  }
}
