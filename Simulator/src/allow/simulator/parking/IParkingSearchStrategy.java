package allow.simulator.parking;

import java.util.List;

import allow.simulator.world.Street;

public interface IParkingSearchStrategy {

  List<Street> getPathToNextPossibleParking(Street current);
  
}
