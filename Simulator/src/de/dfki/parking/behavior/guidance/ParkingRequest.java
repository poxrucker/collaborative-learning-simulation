package de.dfki.parking.behavior.guidance;

import allow.simulator.util.Coordinate;
import de.dfki.parking.behavior.ParkingUtility;

public final class ParkingRequest {
  // Expected arrival time of person
  private final long expectedArrivalTime;
  
  // Destination of person
  private final Coordinate destination;
  
  // Utility function of person to choose parking spot
  private final ParkingUtility utility;
  
  public ParkingRequest(long expectedArrivalTime, Coordinate destination, ParkingUtility utility) {
    this.expectedArrivalTime = expectedArrivalTime;
    this.destination = destination;
    this.utility = utility;
  }
  
  /**
   * Returns the time a person expects to arrives at her destination and requires a parking spot.
   * 
   * @return Expected arrival time
   */
  public long getExpectedArrivalTime() {
    return expectedArrivalTime;
  }
  
  /**
   * Returns the destination a person needs to find a parking spot at.
   * 
   * @return Destination to find a parking spot
   */
  public Coordinate getDestination() {
    return destination;
  }
  
  /**
   * Returns the utility of the requesting person to choose a suitable parking spot.
   * 
   * @return Utility function of requesting person
   */
  public ParkingUtility getUtility() {
    return utility;
  }
}