package de.dfki.parking.exploration;

import allow.simulator.util.Coordinate;

public interface IExplorationStrategy {

  Coordinate findNextPossibleParking(Coordinate position, Coordinate destination, long currentTime);
  
}
