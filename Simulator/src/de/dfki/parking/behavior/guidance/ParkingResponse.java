package de.dfki.parking.behavior.guidance;

import allow.simulator.util.Coordinate;
import de.dfki.parking.model.Parking;

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
