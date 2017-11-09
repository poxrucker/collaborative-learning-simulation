package de.dfki.parking.behavior;

import allow.simulator.util.Coordinate;

public interface IExplorationStrategy {

  Coordinate findNextPossibleParking(Coordinate position, Coordinate destination, long currentTime);
  
}
