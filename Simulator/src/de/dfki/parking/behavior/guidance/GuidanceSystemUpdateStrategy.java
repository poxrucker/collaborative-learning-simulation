package de.dfki.parking.behavior.guidance;

import de.dfki.parking.behavior.IUpdateStrategy;
import de.dfki.parking.knowledge.ParkingMap;
import de.dfki.parking.model.GuidanceSystem;
import de.dfki.parking.model.Parking;

public final class GuidanceSystemUpdateStrategy implements IUpdateStrategy {

  private final ParkingMap localMap;
  private final GuidanceSystem guidanceSystem;
  private final boolean hasSensorCar;
  
  public GuidanceSystemUpdateStrategy(ParkingMap localMap, GuidanceSystem guidanceSystem, boolean hasSensorCar) {
    this.localMap = localMap;
    this.guidanceSystem = guidanceSystem;
    this.hasSensorCar = hasSensorCar;
  }
  
  @Override
  public void updateParkingState(Parking parking, int nSpots, int nFreeSpots, double price, long time, boolean parked) {
    localMap.update(parking, nSpots, nFreeSpots, price, time);
    
    if (hasSensorCar || parked) {
      // guidanceSystem.update(parking, nSpots, nFreeSpots, price, time);
    } 
  }
}
