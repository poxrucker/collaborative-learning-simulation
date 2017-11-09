package de.dfki.parking.behavior;

import java.util.List;

import allow.simulator.util.Coordinate;
import allow.simulator.world.Street;
import de.dfki.parking.model.ParkingGuidanceSystem;

public final class GuidanceSystemSelectionStrategy implements IParkingSelectionStrategy {
  // GuidanceSystem instance assigning parking spots
  private final ParkingGuidanceSystem guidanceSystem;
  
  public GuidanceSystemSelectionStrategy(ParkingGuidanceSystem guidanceSystem) {
    this.guidanceSystem = guidanceSystem;
  }
  
  @Override
  public List<ParkingPossibility> selectParking(Street current, Coordinate destination, long currentTime) {
    // TODO Auto-generated method stub
    return null;
  }
}
