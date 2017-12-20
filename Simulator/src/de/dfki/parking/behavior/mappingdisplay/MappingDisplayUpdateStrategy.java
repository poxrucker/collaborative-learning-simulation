package de.dfki.parking.behavior.mappingdisplay;

import de.dfki.parking.behavior.IUpdateStrategy;
import de.dfki.parking.knowledge.ParkingMap;
import de.dfki.parking.model.Parking;

public final class MappingDisplayUpdateStrategy implements IUpdateStrategy {
  
  private final ParkingMap localMap;
  private final ParkingMap globalMap;
  private final boolean hasSensorCar;
  
  public MappingDisplayUpdateStrategy(ParkingMap localMap, ParkingMap globalMap, boolean hasSensorCar) {
    this.localMap = localMap;
    this.globalMap = globalMap;
    this.hasSensorCar = hasSensorCar;
  }
  
  @Override
  public void updateParkingState(Parking parking, int nSpots, int nFreeSpots, double price, long time, boolean parked) {
    localMap.update(parking, nSpots, nFreeSpots, price, time);
    
    if (hasSensorCar || parked) {
      globalMap.update(parking, nSpots, nFreeSpots, price, time);
    } 
  }
}
