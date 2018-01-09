package de.dfki.parking.behavior.guidance;

import allow.simulator.util.Coordinate;
import allow.simulator.world.StreetNode;
import de.dfki.parking.behavior.IParkingSelectionStrategy;
import de.dfki.parking.behavior.ParkingPossibility;
import de.dfki.parking.model.GuidanceSystem;
import de.dfki.parking.model.ParkingRequest;
import de.dfki.parking.model.ParkingResponse;
import de.dfki.parking.utility.ParkingPreferences;
import de.dfki.parking.utility.ParkingUtility;

public final class GuidanceSystemSelectionStrategy implements IParkingSelectionStrategy {
  // Parking preferences
  private final ParkingPreferences preferences;

  // Function for evaluating utility of parking spot
  private final ParkingUtility utility;

  // Guidance system assigning parking spots
  private final GuidanceSystem guidanceSystem;

  public GuidanceSystemSelectionStrategy(ParkingPreferences preferences, ParkingUtility utility,
      GuidanceSystem guidanceSystem) {
    this.preferences = preferences;
    this.utility = utility;
    this.guidanceSystem = guidanceSystem;
  }

  @Override
  public ParkingPossibility selectParking(StreetNode current, Coordinate destination, long currentTime, long arrivalTime) {
    // Create new ParkngRequest and submit it to the guidance system
    ParkingRequest request = new ParkingRequest(currentTime, arrivalTime, destination, utility, preferences);
    int requestId = guidanceSystem.addRequest(request);
    
    // Get response from guidance system
    ParkingResponse response = guidanceSystem.getParkingPossibility(requestId);
    return (response != null) ? new ParkingPossibility(response.getParking(), response.getPosition(), 0) : null;
  }
}
