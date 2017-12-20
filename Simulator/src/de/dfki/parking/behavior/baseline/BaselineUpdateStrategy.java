package de.dfki.parking.behavior.baseline;

import de.dfki.parking.behavior.IUpdateStrategy;
import de.dfki.parking.knowledge.ParkingMap;
import de.dfki.parking.model.Parking;

public final class BaselineUpdateStrategy implements IUpdateStrategy {

  private final ParkingMap localMap;

  public BaselineUpdateStrategy(ParkingMap localMap) {
    this.localMap = localMap;
  }

  @Override
  public void updateParkingState(Parking parking, int nSpots, int nFreeSpots, double price, long time, boolean parked) {
    localMap.update(parking, nSpots, nFreeSpots, price, time);
  }

}
