package de.dfki.parking.behavior.mappingdisplay;

import de.dfki.parking.behavior.IUpdateStrategy;
import de.dfki.parking.knowledge.ParkingMap;
import de.dfki.parking.model.Parking;

public final class MappingDisplayUpdateStrategy implements IUpdateStrategy {
  // Local ParkingMap instance to update
  private final ParkingMap localMap;
  
  // Global ParkingMap instance to update
  private final ParkingMap globalMap;
  
  // Indicates if update is done with a sensor car
  private final boolean sensorCar;
  
  /**
   * Creates a new instance updating the given local and global ParkingMap.
   * 
   * @param localMap Local ParkingMap
   * @param globalMap Global ParkingMap
   * @param sensorCar True to indicate that a sensor car is used to update the
   * Maps, false otherwise
   */
  public MappingDisplayUpdateStrategy(ParkingMap localMap, ParkingMap globalMap, boolean sensorCar) {
    this.localMap = localMap;
    this.globalMap = globalMap;
    this.sensorCar = sensorCar;
  }
  
  @Override
  public void update(Parking parking, int nSpots, int nFreeSpots, double price, long time, boolean parked) {
    // Always update local map
    localMap.update(parking, nSpots, nFreeSpots, price, time);
    
    // Update global map if sensor car is used or if user has parked
    if (sensorCar || parked) {
      globalMap.update(parking, nSpots, nFreeSpots, price, time);
    } 
  }
}