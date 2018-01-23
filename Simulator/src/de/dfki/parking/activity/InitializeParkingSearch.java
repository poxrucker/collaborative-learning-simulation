package de.dfki.parking.activity;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.util.Coordinate;
import de.dfki.parking.model.Parking;
import de.dfki.parking.model.ParkingState;

public final class InitializeParkingSearch extends Activity<Person> {

  public InitializeParkingSearch(Person entity) {
    super(ActivityType.LEAVE_PARKING_SPOT, entity);
  }

  @Override
  public double execute(double deltaT) {
    // Initialize parking state
    ParkingState parkingState = entity.getParkingState();
    parkingState.setSearchStartTime(0);
    parkingState.setSearchEndTime(0);
    parkingState.setParkingRequestId(-1);
    parkingState.setParkingCandidate(null);

    Parking parking = entity.getParkingState().getCurrentParking();

    if (parking != null) {
      parking.leave(entity);
      parkingState.setCurrentParking(null);
      updateParkingMaps(parking);
    }    
    
    // Initialize parking search
    if (entity.getCurrentItinerary() != null) {
      long arrivalTime = entity.getCurrentItinerary().endTime;
      Coordinate destination = entity.getCurrentItinerary().to;
      long requestTime = entity.getContext().getTime().getTimestamp();
      entity.getParkingBehavior().getInitializationStrategy().initialize(arrivalTime, requestTime, destination);
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
