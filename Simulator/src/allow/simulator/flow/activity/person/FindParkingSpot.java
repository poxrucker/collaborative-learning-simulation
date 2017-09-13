package allow.simulator.flow.activity.person;

import java.util.List;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.parking.Parking;
import allow.simulator.parking.ParkingMap;
import allow.simulator.world.Street;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class FindParkingSpot extends Activity<Person> {
  // Visited streets
  private final List<Street> visitedStreets;
  
  public FindParkingSpot(Person entity, Street street) {
    super(ActivityType.FIND_PARKING_SPOT, entity);
    visitedStreets = new ObjectArrayList<>(new Street[] { street });
  }

  public FindParkingSpot(Person entity, List<Street> visitedStreets) {
    super(ActivityType.FIND_PARKING_SPOT, entity);
    this.visitedStreets = visitedStreets;
  }
  
  @Override
  public double execute(double deltaT) {
    // Current street
    Street current = visitedStreets.get(visitedStreets.size() - 1);
    
    // ParkingMap instance
    ParkingMap parkingMap = entity.getContext().getParkingMap();
    
    if (current.getEndNode().getPosition().equals(entity.getHome())) {
      // If destination is home, set finished as person parks at their home
      setFinished();
      return deltaT;
    }
    
    // Check if there is a free parking spot in the current street
    List<Parking> parkings = parkingMap.getParkingInStreet(current.getName());
    
    if (parkings == null) {
      // If there is no parking at all, choose next street to look for spot
      Street nextStreet = chooseNextStreet();
      entity.getFlow().addActivity(new Drive(entity, new ObjectArrayList<>(new Street[] { nextStreet })));
      entity.getFlow().addActivity(new FindParkingSpot(entity, visitedStreets));
      setFinished();
      return 0;
    }
    
    // Choose suitable parking if available
    Parking parking = chooseParking(parkings);
    
    if (parking == null) {
      // If there is no parking at all, choose next street to look for spot
      Street nextStreet = chooseNextStreet();
      entity.getFlow().addActivity(new Drive(entity, new ObjectArrayList<>(new Street[] { nextStreet })));
      entity.getFlow().addActivity(new FindParkingSpot(entity, visitedStreets));
      setFinished();
      return 0;
    }
    
    // If parking was found, park car and calculate way to original destination
    parking.park(entity);
    entity.setCurrentParking(parking);
    setFinished();
    return deltaT;
  }

  private Parking chooseParking(List<Parking> parkings) {
    
    for (Parking parking : parkings) {
      
      if (parking.hasFreeParkingSpot())
        return parking;
    }
    return null;
  }
  
  private Street chooseNextStreet() {
    return null;
  }
}
