package de.dfki.parking.behavior;

import de.dfki.parking.model.Parking;

public interface IUpdateStrategy {

  void updateParkingState(Parking parking, int nSpots, int nFreeSpots, double price, long time, boolean parked);
  
}
