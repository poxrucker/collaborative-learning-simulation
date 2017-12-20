package de.dfki.parking.behavior.guidance;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;

import allow.simulator.util.Coordinate;
import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.index.ParkingIndexEntry;
import de.dfki.parking.knowledge.ParkingMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;

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
  private final ParkingMap knowledge;
  
  // Parking index providing a spatial index to query parking possibilities 
  private ParkingIndex parkingIndex;
  
  // Contains requests submitted to the guidance system
  private final Int2ObjectMap<ParkingRequest> requestIndex;
  
  // Request id to possible parking ids
  private final Int2ObjectMap<IntList> requestAssignment;
  
  // Parking id to assignment
  private final Int2ObjectMap<SortedSet<ParkingRequest>> parkingSpotAssignment;
  
  private final Comparator<ParkingRequest> comp;
  
  public GuidanceSystem(ParkingMap knowledge, ParkingIndex parkingIndex) {
    this.knowledge = knowledge;
    this.parkingIndex = parkingIndex;
    requestIndex = new Int2ObjectOpenHashMap<>();
    requestAssignment = new Int2ObjectOpenHashMap<>();
    parkingSpotAssignment = new Int2ObjectOpenHashMap<>();
    comp = new ArrivalTimeComparator();
  }
  
  public int addRequest(ParkingRequest request) {    
    // Store request with generated request id to obtain constraints later 
    int requestId = getNextRequestId();
    requestIndex.put(requestId, request);
    
    // Get all parking possibilities for request based on specified spatial constraints
    Coordinate dest = request.getDestination();
    double maxDistance = request.getPreferences().getWdMax();
    Collection<ParkingIndexEntry> entries = parkingIndex.getParkingsWithMaxDistance(dest, maxDistance);
    IntList preferredParking = new IntArrayList(entries.size()); // Stores parking possibilities
    
    for (ParkingIndexEntry entry : entries) {
      preferredParking.add(entry.getParking().getId());
      SortedSet<ParkingRequest> assignment = parkingSpotAssignment.get(entry.getParking().getId());
      
      if (assignment == null) {
        assignment = new ObjectRBTreeSet<>(comp);
        parkingSpotAssignment.put(entry.getParking().getId(), assignment);
      }
      assignment.add(request);
    }
    
    requestAssignment.put(requestId, preferredParking);
    return requestId;
  }
 
  public ParkingResponse getParkingPossibility(int requestId) {
    // Get original request
    ParkingRequest request = requestIndex.remove(requestId);
    
    // Get parking according to user constraints
    IntList preferredParking = requestAssignment.remove(requestId);
    
    // Get position of request in parking spot priority queues, and find current capacities
    IntList priorities = new IntArrayList(preferredParking.size());
    int highestPriority = Integer.MAX_VALUE;
    
    IntList queueSize = new IntArrayList(preferredParking.size());

    IntList capacities = new IntArrayList(preferredParking.size());
    IntList maxCapacity = new IntArrayList(preferredParking.size());
    
    for (int parkingId : preferredParking) {
      SortedSet<ParkingRequest> assignments = parkingSpotAssignment.get(parkingId);
      int priority = indexOf(assignments, request);
      priorities.add(priority);
      
      if (priority < highestPriority)
        highestPriority = priority;
      
      queueSize.add(assignments.size());   
      assignments.remove(request);
      
      ParkingIndexEntry entry = parkingIndex.getEntryForParkingId(parkingId);
      capacities.add(entry.getParking().getNumberOfFreeParkingSpots());
      maxCapacity.add(entry.getParking().getNumberOfParkingSpots());
    }
    
    if (priorities.size() == 0)
      return null;
    
    StringBuilder bldr = new StringBuilder();
    bldr.append("[");  
    for (int i = 0; i < priorities.size(); i++) {
      bldr.append("(" + (priorities.get(i) +  1) + "/" + queueSize.get(i) + ", " + capacities.get(i) + "/" + maxCapacity.get(i) + ")");
      
      if (i < priorities.size() - 1)
        bldr.append(", ");
    }
    bldr.append("]");
    // System.out.println(bldr);

    return null;
  }
  
  private int getNextRequestId() {
    return requestIds++;
  }
  
  private static <T> int indexOf(SortedSet<T> set, T element) {
    return set.contains(element)? set.headSet(element).size(): -1;
  }
}
