package de.dfki.parking.behavior.guidance;

import java.util.List;

import allow.simulator.util.Coordinate;
import allow.simulator.world.StreetNode;
import de.dfki.parking.behavior.IParkingSelectionStrategy;
import de.dfki.parking.behavior.ParkingPossibility;
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
  public List<ParkingPossibility> selectParking(StreetNode current, Coordinate destination, long currentTime) {
    ParkingRequest request = new ParkingRequest(0, currentTime, destination, utility, preferences);
    
    guidanceSystem.addRequest(request);
    return null;
  }
}
