package de.dfki.parking.selection;

import java.util.List;

import allow.simulator.util.Coordinate;
import allow.simulator.world.Street;

public interface IParkingSelectionStrategy {

  List<ParkingPossibility> selectParking(Street current, Coordinate destination, long currentTime);
  
}
