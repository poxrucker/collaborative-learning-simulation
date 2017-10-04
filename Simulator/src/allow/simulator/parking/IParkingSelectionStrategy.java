package allow.simulator.parking;

import allow.simulator.world.Street;

public interface IParkingSelectionStrategy {

  Parking selectParking(Street street, double parkingTime);
  
}
