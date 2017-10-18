package allow.simulator.flow.activity.person;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import de.dfki.parking.model.Parking;

public final class LeaveParkingSpot extends Activity<Person> {

  public LeaveParkingSpot(Person entity) {
    super(ActivityType.LEAVE_PARKING_SPOT, entity);
  }

  @Override
  public double execute(double deltaT) {
    // Remove person from current parking spot and finish
    Parking parking = entity.getCurrentParking();
    
    if (parking != null) { 
      parking.leave(entity);
      entity.setCurrentParking(null);
      updateParkingMaps(parking);
    }
    setFinished();
    return 0;
  }
  
  public String toString() {
    return "LeaveParkingSpot " + entity;
  }
  
  private void updateParkingMaps(Parking parking) {
    long time = entity.getContext().getTime().getTimestamp();
    int nSpots = parking.getNumberOfParkingSpots();
    int nFreeSpots = parking.getNumberOfFreeParkingSpots();
    entity.getLocalParkingKnowledge().update(parking, nSpots, nFreeSpots, time);

    if (entity.hasSensorCar())
        entity.getGlobalParkingKnowledge().update(parking, nSpots, nFreeSpots, time);  
  }
}
