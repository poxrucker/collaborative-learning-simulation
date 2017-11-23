package de.dfki.parking.index;

import java.util.List;

import com.vividsolutions.jts.index.ItemVisitor;

import allow.simulator.util.Coordinate;

final class DistanceFilter implements ItemVisitor {
  // List of ParkingMapEntry instances filtered by distance
  private final List<ParkingIndexEntry> results;

  // Reference position
  private final Coordinate referencePosition;

  // Maximum allowed distance from reference position
  private final double maxDistance;

  public DistanceFilter(List<ParkingIndexEntry> results, Coordinate referencePosition, double maxDistance) {
    this.results = results;
    this.referencePosition = referencePosition;
    this.maxDistance = maxDistance;
  }

  @Override
  public void visitItem(Object item) {
    ParkingIndexEntry entry = (ParkingIndexEntry) item;

    // Filter by maxDistance
    if (allow.simulator.util.Geometry.haversineDistance(referencePosition, entry.getReferencePosition()) > maxDistance)
      return;

    results.add(entry);
  }
}
