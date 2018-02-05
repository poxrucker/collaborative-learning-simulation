package de.dfki.parking.behavior.guidance;

import allow.simulator.util.Coordinate;
import de.dfki.parking.behavior.IInitializationStrategy;
import de.dfki.parking.model.GuidanceSystem;
import de.dfki.parking.model.ParkingState;

public final class GuidanceSystemInitializationStrategy implements IInitializationStrategy {
  // GuidanceSystem instance assigning parking spots to users
  private final GuidanceSystem guidanceSystem;
  
  // ParkingState management
  private final ParkingState parkingState;
  
  public GuidanceSystemInitializationStrategy(GuidanceSystem guidanceSystem,
      ParkingState parkingState) {
    this.guidanceSystem = guidanceSystem;
    this.parkingState = parkingState;
  }
  
  @Override
  public void initialize(long arrivalTime, long requestTime, Coordinate destination) {
    int requestId = guidanceSystem.request(requestTime, arrivalTime, destination, 
        parkingState.getParkingUtility(), parkingState.getParkingPreferences());
    parkingState.setParkingReservationId(requestId);
  }
}
