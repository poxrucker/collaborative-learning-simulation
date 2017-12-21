package de.dfki.parking.spatial;

import java.util.List;

import allow.simulator.util.Coordinate;

public interface ILocatable {

  List<Coordinate> getPositions();
  
  Coordinate getReferencePosition();
  
}
