package de.dfki.parking.behavior;

import allow.simulator.util.Coordinate;
import allow.simulator.world.StreetNode;

public interface IParkingSelectionStrategy {

  ParkingPossibility selectParking(StreetNode current, Coordinate destination, long currentTime, long arrivalTime);
  
}
