package de.dfki.parking.behavior;

import java.util.List;

import allow.simulator.util.Coordinate;
import allow.simulator.world.StreetNode;

public interface IParkingSelectionStrategy {

  List<ParkingPossibility> selectParking(StreetNode current, Coordinate position, Coordinate destination, long currentTime);
  
}
