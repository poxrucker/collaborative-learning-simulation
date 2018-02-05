package de.dfki.parking.model;

import allow.simulator.util.Coordinate;
import de.dfki.parking.utility.ParkingPreferences;
import de.dfki.parking.utility.ParkingUtility;
import it.unimi.dsi.fastutil.ints.IntList;

public final class ParkingReservationRequest {
  // Reservation id
  private final int id;
  
  // Request time
  private final long requestTime;

  // Expected arrival time of person
  private final long expectedArrivalTime;

  // Destination of requesting user
  private final Coordinate destination;

  // Utility function of requesting user to choose parking spot
  private final ParkingUtility utility;

  // Parking preferences of requesting user
  private final ParkingPreferences preferences;
  
  // Ids of possible parkings according to users constraints
  private final IntList possibleParking;

  public ParkingReservationRequest(int id, long requestTime, long expectedArrivalTime,
      Coordinate destination, ParkingUtility utility, ParkingPreferences preferences,
      IntList possibleParking) {
    this.id = id;
    this.requestTime = requestTime;
    this.expectedArrivalTime = expectedArrivalTime;
    this.destination = destination;
    this.utility = utility;
    this.preferences = preferences;
    this.possibleParking = possibleParking;
  }
  
  /**
   * Returns the id of the ParkingReservationRequest instance.
   * 
   * @return Reservation id 
   */
  public int getId() {
    return id;
  }
  
  /**
   * Returns the time the ParkingReservationRequest instance was created.
   * 
   * @return Time parking spot request was created
   */
  public long getRequestTime() {
    return requestTime;
  }
  
  /**
   * Returns the time the requesting user expects to arrive at her 
   * destination and requires a parking spot.
   * 
   * @return Expected arrival time
   */
  public long getExpectedArrivalTime() {
    return expectedArrivalTime;
  }
  
  /**
   * Returns the destination the requesting user needs to find a parking spot at.
   * 
   * @return Destination to find a parking spot
   */
  public Coordinate getDestination() {
    return destination;
  }
  
  /**
   * Returns the utility of the requesting user to choose a suitable parking spot.
   * 
   * @return Utility function of requesting user
   */
  public ParkingUtility getUtility() {
    return utility;
  }
  
  /**
   * Returns the preferences of the requesting user to choose a suitable parking spot.
   * 
   * @return Preferences of requesting user
   */
  public ParkingPreferences getPreferences() {
    return preferences;
  }
  
  public IntList getPossibleParking() {
    return possibleParking;
  }
}
