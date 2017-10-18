package de.dfki.parking.exploration;

import java.util.List;

import allow.simulator.world.Street;

public interface IExplorationStrategy {

  List<Street> getPathToNextPossibleParking(Street current);
  
}
