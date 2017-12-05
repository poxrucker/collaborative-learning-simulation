package de.dfki.parking.behavior;

import java.util.List;

import allow.simulator.util.Coordinate;
import allow.simulator.world.Street;

public interface IParkingSelectionStrategy {

  List<ParkingPossibility> selectParking(Street current, Coordinate position, Coordinate destination, long currentTime);
  
}
