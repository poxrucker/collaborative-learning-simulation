package de.dfki.parking.behavior.guidance;

import java.util.List;

import allow.simulator.util.Coordinate;
import allow.simulator.world.StreetNode;
import de.dfki.parking.behavior.IParkingSelectionStrategy;
import de.dfki.parking.behavior.ParkingPossibility;

public final class GuidanceSystemSelectionStrategy implements IParkingSelectionStrategy {

  
  public GuidanceSystemSelectionStrategy() {
  }
  
  @Override
  public List<ParkingPossibility> selectParking(StreetNode current, Coordinate destination, long currentTime) {
    // TODO Auto-generated method stub
    return null;
  }
}
