package de.dfki.parking.behavior.guidance;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import de.dfki.parking.knowledge.ParkingKnowledge;

public final class GuidanceSystem {
  
  private static final class RequestComparator implements Comparator<ParkingRequest> {

    @Override
    public int compare(ParkingRequest o1, ParkingRequest o2) {
      return Long.compare(o1.getExpectedArrivalTime(), o2.getExpectedArrivalTime());
    }   
  }  
  // Parking knowledge instance providing global view of all parking possibilities
  private final ParkingKnowledge knowledge;
  
  // Contains requests submitted to the guidance system
  private final Queue<ParkingRequest> requestQueue;
  
  public GuidanceSystem(ParkingKnowledge knowledge) {
    this.knowledge = knowledge;
    requestQueue = new PriorityQueue<>(new RequestComparator());
  }
 
}