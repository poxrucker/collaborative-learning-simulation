package de.dfki.parking.behavior.guidance;

import allow.simulator.util.Coordinate;
import allow.simulator.world.StreetNode;
import de.dfki.parking.behavior.IParkingSelectionStrategy;
import de.dfki.parking.model.GuidanceSystem;
import de.dfki.parking.model.ParkingPossibility;
import de.dfki.parking.model.ParkingReservationResponse;
import de.dfki.parking.model.ParkingState;

public final class GuidanceSystemSelectionStrategy implements IParkingSelectionStrategy {
  // Guidance system assigning parking spots
  private final GuidanceSystem guidanceSystem;

  // ParkingState management
  private final ParkingState parkingState;
    
  public GuidanceSystemSelectionStrategy(GuidanceSystem guidanceSystem, ParkingState parkingState) {
    this.guidanceSystem = guidanceSystem;
    this.parkingState = parkingState;
  }

  @Override
  public ParkingPossibility selectParking(StreetNode current, Coordinate destination, long currentTime, long arrivalTime) {
    // Get response from guidance system
    ParkingReservationResponse response = guidanceSystem.reserve(parkingState.getParkingReservationId(), current.getPosition(), currentTime);
    
    if (response == null) {
      // If no parking spot could be assigned, reset reservation Id
      parkingState.setParkingReservationId(-1);
      return null;
    }
    return new ParkingPossibility(response.getParking(), response.getPosition(), response.getEstimatedUtility());
  }
}
