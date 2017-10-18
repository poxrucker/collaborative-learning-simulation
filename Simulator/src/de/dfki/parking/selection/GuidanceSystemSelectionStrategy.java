package de.dfki.parking.selection;

import java.util.List;

import allow.simulator.util.Coordinate;
import de.dfki.parking.model.ParkingGuidanceSystem;

public final class GuidanceSystemSelectionStrategy implements IParkingSelectionStrategy {
  // GuidanceSystem instance assigning parking spots
  private final ParkingGuidanceSystem guidanceSystem;
  
  public GuidanceSystemSelectionStrategy(ParkingGuidanceSystem guidanceSystem) {
    this.guidanceSystem = guidanceSystem;
  }
  
  @Override
  public List<ParkingPossibility> selectParking(Coordinate currentPosition, Coordinate destination, long currentTime) {
    // TODO Auto-generated method stub
    return null;
  }
}
