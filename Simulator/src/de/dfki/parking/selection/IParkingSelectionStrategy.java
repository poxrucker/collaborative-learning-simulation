package de.dfki.parking.selection;

import java.util.List;

import allow.simulator.util.Coordinate;

public interface IParkingSelectionStrategy {

  List<ParkingPossibility> selectParking(Coordinate currentPosition, Coordinate destination, long currentTime);
  
}
