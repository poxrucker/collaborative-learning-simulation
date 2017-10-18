package de.dfki.parking.model;

import com.vividsolutions.jts.index.strtree.STRtree;

public final class SpatialParkingIndex {

  private final STRtree spatialIndex;
  
  public SpatialParkingIndex() {
    spatialIndex = new STRtree();
  }
  
}
