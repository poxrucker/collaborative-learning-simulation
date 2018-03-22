package de.dfki.parking.activity;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import de.dfki.parking.model.Parking;
import de.dfki.parking.model.ParkingState;

public final class ResetParkingState extends Activity<Person> {

  public ResetParkingState(Person entity) {
    super(ActivityType.LEAVE_PARKING_SPOT, entity);
  }

  @Override
  public double execute(double deltaT) {
    // Initialize parking state
    ParkingState parkingState = entity.getParkingState();
    
    if (parkingState == null) {
      setFinished();
      return 0;
    }
    parkingState.setSearchStartTime(0);
    parkingState.setSearchEndTime(0);
    parkingState.setParkingReservationId(-1);
    parkingState.setParkingCandidate(null);

    Parking parking = entity.getParkingState().getCurrentParking();

    if (parking != null) {
      parking.leave(entity);
      parkingState.setCurrentParking(null);
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
    double price = parking.getCurrentPricePerHour();
    entity.getParkingBehavior().getUpdateStrategy().update(parking, nSpots, nFreeSpots, price, time, true);
  }
}
