package de.dfki.parking.activity;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.util.Coordinate;

public final class InitializeParkingSearch extends Activity<Person> {

  public InitializeParkingSearch(Person entity) {
    super(ActivityType.LEAVE_PARKING_SPOT, entity);
  }

  @Override
  public double execute(double deltaT) {
    long arrivalTime = entity.getCurrentItinerary().endTime;
    Coordinate destination = entity.getCurrentItinerary().to;
    long requestTime = entity.getContext().getTime().getTimestamp();
    entity.getParkingBehavior().getInitializationStrategy().initialize(arrivalTime, requestTime, destination);
    setFinished();
    return 0;
  }

  public String toString() {
    return "InitializeParkingSearch " + entity;
  }
}
