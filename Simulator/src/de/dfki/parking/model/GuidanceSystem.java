package de.dfki.parking.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.util.Pair;
import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.index.ParkingIndexEntry;
import de.dfki.parking.knowledge.ParkingMap;
import de.dfki.parking.knowledge.ParkingMapEntry;
import de.dfki.parking.utility.ParkingParameters;
import de.dfki.parking.utility.ParkingPreferences;
import de.dfki.parking.utility.ParkingUtility;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;

public final class GuidanceSystem {
  // Parking knowledge instance providing global view of all parking possibilities
  private final ParkingMap parkingMap;
  
  // Parking index providing a spatial index to query parking possibilities 
  private ParkingIndex parkingIndex;
  
  // Provides unique ids assigned to incoming requests
  private int reservationIdProvider;
  
  // Used to sort reservation requests
  private final Comparator<ParkingReservationRequest> comp;

  // Buffers ParkingReservationRequests by reservation id
  private final Int2ObjectMap<ParkingReservationRequest> reservationRequestIndex;
 
  // Buffers pending reservation assignments by parking id
  private final Int2ObjectMap<SortedSet<ParkingReservationRequest>> pendingIndex;
  
  // Buffers assigned reservations by parking id
  private final Int2ObjectMap<IntSet> assignmentIndex;
  
  public GuidanceSystem(ParkingMap parkingMap, ParkingIndex parkingIndex, 
      Comparator<ParkingReservationRequest> comp) {
    this.parkingMap = parkingMap;
    this.parkingIndex = parkingIndex;
    this.comp = comp;
    reservationRequestIndex = new Int2ObjectOpenHashMap<>();
    pendingIndex = new Int2ObjectOpenHashMap<>();
    assignmentIndex = new Int2ObjectOpenHashMap<>();
  }
  
  public int request(long requestTime, long expectedArrivalTime, Coordinate destination,
      ParkingUtility utility, ParkingPreferences preferences) {    
    // Get ids of all parking possibilities based on specified constraints
    IntList possibleParking = findPossibleParking(destination, preferences.getWdMax());
    
    // Create ParkingReservationRequest
    int reservationId = getNextReservationId();
    ParkingReservationRequest request = new ParkingReservationRequest(reservationId, requestTime, 
        expectedArrivalTime, destination, utility, preferences, possibleParking);
   
    // Buffer request
    reservationRequestIndex.put(reservationId, request);
    
    // Add pending reservation to all possible parking possibilities
    for (int id : possibleParking) {
      SortedSet<ParkingReservationRequest> pending = pendingIndex.get(id);
      
      if (pending == null) {
        pending = new ObjectRBTreeSet<>(comp);
        pendingIndex.put(id, pending);
      }
      pending.add(request);
    }
    return reservationId;
  }
 
  public ParkingReservationResponse reserve(int reservationId, Coordinate current) {
    // Get request from buffer
    ParkingReservationRequest request = reservationRequestIndex.remove(reservationId);
   
    if (request.getPossibleParking().size() == 0) {
      return null;
    }
    
    // Get position of request in parking spot priority queues, and find current capacities
    int highestPriority = Integer.MAX_VALUE; 
    IntList pendingPriorities = new IntArrayList(request.getPossibleParking().size());
    IntList pendingTotal = new IntArrayList(request.getPossibleParking().size());
    IntList freeCapacities = new IntArrayList(request.getPossibleParking().size());
    IntList assignedCapacities = new IntArrayList(request.getPossibleParking().size());
    IntList maxCapacities = new IntArrayList(request.getPossibleParking().size());
     
    for (int parkingId : request.getPossibleParking()) {
      // Calculate pending priority of request for parking
      SortedSet<ParkingReservationRequest> pendingRequests = pendingIndex.get(parkingId);
      int priority = indexOf(pendingRequests, request);
      pendingPriorities.add(priority);
      
      if (priority < highestPriority)
        highestPriority = priority;
      
      pendingTotal.add(pendingRequests.size());   
      pendingRequests.remove(request);
      
      // Get number of already assigned reservations for parking
      IntSet assignments = assignmentIndex.get(parkingId);
      assignedCapacities.add(assignments != null ? assignments.size() : 0);
      
      // Get capacities
      ParkingMapEntry entry = parkingMap.findById(parkingId);
      freeCapacities.add(entry.getNFreeParkingSpots());
      maxCapacities.add(entry.getNParkingSpots());
    }
    
    if (pendingPriorities.size() == 0)
      return null;
    
    // Find all parking spots where user has max priority and filter those with no capacity
    IntList indices = new IntArrayList();
    
    for (int i = 0; i < request.getPossibleParking().size(); i++) {
      
      if (pendingPriorities.get(i) > highestPriority)
        continue;
      
      if (freeCapacities.get(i) < assignedCapacities.get(i))
        continue;
      indices.add(i);
    }
    
    List<ParkingIndexEntry> indexEntries = new ObjectArrayList<>(indices.size());
    
    for (int i : indices) {
      indexEntries.add(parkingIndex.getEntryForParkingId(request.getPossibleParking().get(i)));
    }
    
    /*StringBuilder bldr = new StringBuilder();
    bldr.append("[");  
    for (int i = 0; i < priorities.size(); i++) {
      bldr.append("(" + (priorities.get(i) +  1) + "/" + queueSize.get(i) + ", " + capacities.get(i) + "/" + maxCapacity.get(i) + ")");
      
      if (i < priorities.size() - 1)
        bldr.append(", ");
    }
    bldr.append("]");
    System.out.println(bldr);*/
    List<ParkingPossibility> ranked = rank(indexEntries, current, request.getDestination(), request.getUtility(), request.getPreferences());
    
    if (ranked.size() == 0)
      return null;
    
    ParkingPossibility ret = ranked.get(0);
    IntSet assignment = assignmentIndex.get(ret.getParking().getId());
    
    if (assignment == null) {
      assignment = new IntOpenHashSet();
      assignmentIndex.put(ret.getParking().getId(), assignment);
    }
    assignment.add(reservationId);
    return (ranked.size() > 0) ? new ParkingReservationResponse(reservationId, ranked.get(0).getParking(), 
        ranked.get(0).getPosition(), ranked.get(0).getEstimatedUtility()) : null;
  }
  
  public void update(Parking parking, int nSpots, int nFreeSpots, double price, long time) {
    parkingMap.update(parking, nSpots, nFreeSpots, price, time);
  }
  
  public void update(int reservationId, Parking parking, int nSpots, int nFreeSpots, double price, long time) {
    // Remove reservationId from assignments
    if (reservationId > 0) {
      IntSet assignments = assignmentIndex.get(parking.getId());
      assignments.remove(reservationId);
    }
    // Update parking map
    update(parking, nSpots, nFreeSpots, price, time);
  }
  
  private int getNextReservationId() {
    return reservationIdProvider++;
  }
  
  private IntList findPossibleParking(Coordinate destination, double maxDistance) {
    // Find all within range in index
    Collection<ParkingIndexEntry> entries = parkingIndex.getParkingsWithMaxDistance(destination, maxDistance);
    IntList ret = new IntArrayList(entries.size());
    
    for (ParkingIndexEntry entry : entries) {
      ret.add(entry.getParking().getId());
    }
    return ret;
  }
  
  private List<ParkingPossibility> rank(List<ParkingIndexEntry> parkings, Coordinate currentPosition, Coordinate destination,
      ParkingUtility utility, ParkingPreferences preferences) {
    List<Pair<ParkingIndexEntry, Double>> temp = new ObjectArrayList<>();

    for (ParkingIndexEntry parking : parkings) {
      double c = parking.getParking().getDefaultPricePerHour();
      double wd = Geometry.haversineDistance(parking.getReferencePosition(), destination);
      double st = (Geometry.haversineDistance(parking.getReferencePosition(), currentPosition) / 3.0);
      temp.add(new Pair<>(parking, utility.computeUtility(new ParkingParameters(c, wd, st), preferences)));
    }
    temp.sort((t1, t2) -> Double.compare(t2.second, t1.second));

    if (temp.size() > 0 && temp.get(0).second == 0.0)
      return new ObjectArrayList<>(0);
    
    List<ParkingPossibility> ret = new ObjectArrayList<>(temp.size());

    for (Pair<ParkingIndexEntry, Double> p : temp) {
      List<Coordinate> positions = p.first.getPositions();
      Coordinate t = null;
      
      if (positions.contains(currentPosition)) {
        t = currentPosition;  
        
      } else {
        t = positions.get(ThreadLocalRandom.current().nextInt(positions.size()));
      }
      ret.add(new ParkingPossibility(p.first.getParking(), new Coordinate(t.x, t.y), p.second));
    }
    return ret;
  }
  
  private static <T> int indexOf(SortedSet<T> set, T element) {
    return set.contains(element)? set.headSet(element).size(): -1;
  }
}
