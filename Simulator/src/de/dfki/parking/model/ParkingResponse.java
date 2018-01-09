package de.dfki.parking.model;

import allow.simulator.util.Coordinate;

public final class ParkingResponse {

  // Parking
  private final Parking parking;

  // Position of parking
  private final Coordinate position;

  public ParkingResponse(Parking parking, Coordinate position) {
    this.parking = parking;
    this.position = position;
  }

  public Parking getParking() {
    return parking;
  }

  public Coordinate getPosition() {
    return position;
  }
}
