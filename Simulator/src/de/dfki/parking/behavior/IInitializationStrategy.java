package de.dfki.parking.behavior;

import allow.simulator.util.Coordinate;

public interface IInitializationStrategy {

  void initialize(long arrivalTime, long requestTime, Coordinate destination);
  
}
