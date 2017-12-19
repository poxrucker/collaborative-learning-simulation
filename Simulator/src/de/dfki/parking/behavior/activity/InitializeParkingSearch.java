package de.dfki.parking.behavior.activity;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import de.dfki.parking.model.Parking;

public final class InitializeParkingSearch extends Activity<Person> {

  public InitializeParkingSearch(Person entity) {
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
    // Initialize parking properties
    entity.setSearchStartTime(0);
    entity.setSearchEndTime(0);
    entity.parkingRequestId = -1;
    entity.parkingCandidate = null;
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
    double price = parking.getCurrentPricePerHour();

    entity.getLocalParkingKnowledge().update(parking, nSpots, nFreeSpots, price, time);

    if (entity.isUser())
      entity.getGlobalParkingKnowledge().update(parking, nSpots, nFreeSpots, price, time);
  }
}
