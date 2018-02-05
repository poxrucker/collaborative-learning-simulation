package de.dfki.parking.model;

import allow.simulator.util.Coordinate;

public final class ParkingReservationResponse {
  // Reservation id
  private final int reservationId;
  
  // Parking
  private final Parking parking;

  // Position of parking
  private final Coordinate position;

  // Estimated utility of the assigned parking spot
  private final double estimatedUtility;
  
  public ParkingReservationResponse(int reservationId, Parking parking, 
      Coordinate position, double estimatedUtility) {
    this.parking = parking;
    this.position = position;
    this.reservationId = reservationId;
    this.estimatedUtility = estimatedUtility;
  }

  public int getReservationId() {
    return reservationId;
  }
  
  public Parking getParking() {
    return parking;
  }

  public Coordinate getPosition() {
    return position;
  }
  
  public double getEstimatedUtility() {
    return estimatedUtility;
  }
}
