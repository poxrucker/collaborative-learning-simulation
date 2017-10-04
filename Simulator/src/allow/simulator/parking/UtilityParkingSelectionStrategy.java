package allow.simulator.parking;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.world.Street;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class UtilityParkingSelectionStrategy implements IParkingSelectionStrategy {
  // ParkingMap instance
  private final ParkingRepository parkingMap;

  public UtilityParkingSelectionStrategy(ParkingRepository parkingMap) {
    this.parkingMap = parkingMap;
  }
  
  @Override
  public Parking selectParking(Street street, double parkingTime) {
    // Filter those which are completely occupied
    List<Parking> freeParkings = getFreeParkings(street);

    // If there is free parking possibilities, select a random one. Otherwise return null
    return (!freeParkings.isEmpty()) ? selectRandom(freeParkings) : null;
  }
  
  private List<Parking> getFreeParkings(Street street) {
    // Get list of possible parking possibilities
    List<Parking> parkings = parkingMap.getParkingInStreet(street);
    
    if (parkings == null || parkings.size() == 0)
      return Collections.emptyList();
    
    // Filter for those which have a free parking spot
    List<Parking> freeParkings = new ObjectArrayList<>(parkings.size());
    
    for (Parking parking : parkings) {
      
      if (!parking.hasFreeParkingSpot())
        continue;
      freeParkings.add(parking);
    }
    return freeParkings;
  }
  
  private Parking selectRandom(List<Parking> parkings) {
    return parkings.get(ThreadLocalRandom.current().nextInt(parkings.size()));
  }

}
