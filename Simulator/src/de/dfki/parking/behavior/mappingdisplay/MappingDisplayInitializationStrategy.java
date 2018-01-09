package de.dfki.parking.behavior.mappingdisplay;

import allow.simulator.util.Coordinate;
import de.dfki.parking.behavior.IInitializationStrategy;

public final class MappingDisplayInitializationStrategy implements IInitializationStrategy {

  @Override
  public void initialize(long arrivalTime, long requestTime, Coordinate destination) {
    // Nothing to do in the baseline case
  }
}
