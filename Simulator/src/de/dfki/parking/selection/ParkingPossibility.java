package de.dfki.parking.selection;

import allow.simulator.util.Coordinate;
import de.dfki.parking.model.Parking;

public final class ParkingPossibility {
  // Parking
  private final Parking parking;
  
  // Position of parking
  private final Coordinate position;
  
  public ParkingPossibility(Parking parking, Coordinate position) {
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
