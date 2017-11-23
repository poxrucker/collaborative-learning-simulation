package de.dfki.parking.index;

import java.util.List;

import allow.simulator.util.Coordinate;
import de.dfki.parking.model.Parking;

public final class ParkingIndexEntry {
  // Referenced Parking instance
  private final Parking parking;

  // List of positions through which Parking is accessible
  private final List<Coordinate> accessPositions;

  // Reference position of Parking instance
  private final Coordinate referencePosition;

  public ParkingIndexEntry(Parking parking, List<Coordinate> accessPositions, Coordinate referencePosition) {
    this.parking = parking;
    this.accessPositions = accessPositions;
    this.referencePosition = referencePosition;
  }

  /**
   * Returns the Parking instance the index entry points to.
   * 
   * @return Parking instance the index entry points to
   */
  public Parking getParking() {
    return parking;
  }

  /**
   * Returns all access positions associated with the Parking instance.
   * 
   * @return Access positions associated with the Parking instance
   */
  public List<Coordinate> getAllAccessPositions() {
    return accessPositions;
  }

  /**
   * Returns the reference position associated with the Parking instance.
   * 
   * @return Reference position associated with the Parking instance
   */
  public Coordinate getReferencePosition() {
    return referencePosition;
  }
}