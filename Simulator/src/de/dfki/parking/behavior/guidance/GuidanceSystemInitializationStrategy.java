package de.dfki.parking.behavior.guidance;

import allow.simulator.util.Coordinate;
import de.dfki.parking.behavior.IInitializationStrategy;
import de.dfki.parking.model.GuidanceSystem;
import de.dfki.parking.model.ParkingRequest;
import de.dfki.parking.utility.ParkingPreferences;
import de.dfki.parking.utility.ParkingUtility;

public final class GuidanceSystemInitializationStrategy implements IInitializationStrategy {

  private final GuidanceSystem guidanceSystem;
  private final ParkingUtility utility;
  private final ParkingPreferences preferences;
  
  public GuidanceSystemInitializationStrategy(GuidanceSystem guidanceSystem,
      ParkingUtility utility, ParkingPreferences preferences) {
    this.guidanceSystem = guidanceSystem;
    this.utility = utility;
    this.preferences = preferences;
  }
  
  @Override
  public void initialize(long arrivalTime, long requestTime, Coordinate destination) {
    
  }
}
