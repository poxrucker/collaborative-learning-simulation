package de.dfki.parking.behavior.guidance;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;

import allow.simulator.util.Coordinate;
import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.index.ParkingIndexEntry;
import de.dfki.parking.knowledge.ParkingKnowledge;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public final class GuidanceSystem {
  
  private static final class ArrivalTimeComparator implements Comparator<ParkingRequest> {

    @Override
    public int compare(ParkingRequest o1, ParkingRequest o2) {
      return Long.compare(o1.getExpectedArrivalTime(), o2.getExpectedArrivalTime());
    }   
  }
  
  private static final class RequestTimeComparator implements Comparator<ParkingRequest> {

    @Override
    public int compare(ParkingRequest o1, ParkingRequest o2) {
      return Long.compare(o1.getRequestTime(), o2.getRequestTime());
    }   
  }
  
  private int requestIds;
  
  // Parking knowledge instance providing global view of all parking possibilities
  private final ParkingKnowledge knowledge;
  
  // Parking index providing a spatial index to query parking possibilities 
  private ParkingIndex parkingIndex;
  
  // Contains requests submitted to the guidance system
  private final Int2ObjectMap<ParkingRequest> requests;
  
  // Request id to possible parking ids
  private final Int2ObjectMap<IntList> requestAssignment;
  
  // Parking id to assignment
  private final Int2ObjectMap<SortedSet<ParkingRequest>> parkingSpotAssignment;
  
  private final Comparator<ParkingRequest> comp;
  
  public GuidanceSystem(ParkingKnowledge knowledge) {
    this.knowledge = knowledge;
    requests = new Int2ObjectOpenHashMap<>();
    requestAssignment = new Int2ObjectOpenHashMap<>();
    parkingSpotAssignment = new Int2ObjectOpenHashMap<>();
    comp = new ArrivalTimeComparator();
  }
  
  public int addRequest(ParkingRequest request) {    
    // Store request with generated request id to obtain constraints later 
    int requestId = getNextRequestId();
    requests.put(requestId, request);
    
    // Get all parking possibilities for request based on specified spatial constraints
    Coordinate dest = request.getDestination();
    double maxDistance = request.getPreferences().getWdMax();
    Collection<ParkingIndexEntry> entries = parkingIndex.getParkingsWithMaxDistance(dest, maxDistance);
    IntList possibleParking = new IntArrayList(entries.size()); // Stores parking possibilities
    
    for (ParkingIndexEntry entry : entries) {
      possibleParking.add(entry.getParking().getId());
      SortedSet<ParkingRequest> assignment = parkingSpotAssignment.get(entry.getParking().getId());
      
      if (assignment == null) {
        assignment = new ObjectRBTreeSet<>(comp);
        parkingSpotAssignment.put(entry.getParking().getId(), assignment);
      }
      assignment.add(request);
    }
    
    requestAssignment.put(requestId, possibleParking);
    return requestId;
  }
 
  public ParkingResponse getParkingPossibility(int requestId) {
    // Get original request
    ParkingRequest request = requests.remove(requestId);
    
    // Get parking according to user constraints
    IntList possibleParking = requestAssignment.remove(requestId);
    
    // Get priority list
    IntList priorities = new IntArrayList(possibleParking.size());
    
    for (int parkingId : possibleParking) {
      SortedSet<ParkingRequest> assignments = parkingSpotAssignment.get(parkingId);
      priorities.add(indexOf(assignments, request));
    }
    
    // Find 
    System.out.println(priorities);
    return null;
  }
  
  private int getNextRequestId() {
    return requestIds++;
  }
  
  private static <T> int indexOf(SortedSet<T> set, T element) {
    return set.contains(element)? set.headSet(element).size(): -1;
  }
}
