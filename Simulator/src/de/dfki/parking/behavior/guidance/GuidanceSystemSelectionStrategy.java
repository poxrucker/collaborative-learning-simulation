package de.dfki.parking.behavior.guidance;

import java.util.List;

import allow.simulator.util.Coordinate;
import allow.simulator.world.Street;
import de.dfki.parking.behavior.IParkingSelectionStrategy;
import de.dfki.parking.behavior.ParkingPossibility;

public final class GuidanceSystemSelectionStrategy implements IParkingSelectionStrategy {

  
  public GuidanceSystemSelectionStrategy() {
  }
  
  @Override
  public List<ParkingPossibility> selectParking(Street current, Coordinate destination, long currentTime) {
    // TODO Auto-generated method stub
    return null;
  }
}
