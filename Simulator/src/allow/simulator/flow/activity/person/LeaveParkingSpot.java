package allow.simulator.flow.activity.person;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import de.dfki.parking.behavior.guidance.ParkingRequest;
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

    // Create parking request
    /*if (parkingSpotRequired()) {
      ParkingRequest request = new ParkingRequest(entity.getCurrentItinerary().endTime, entity.getContext().getTime().getTimestamp(),
          entity.getCurrentItinerary().to, entity.getParkingUtility(), entity.getParkingPreferences());
      entity.parkingRequestId = entity.getContext().getGuidanceSystem().addRequest(request);
    }*/
    setFinished();
    return 0;
  }

  public String toString() {
    return "LeaveParkingSpot " + entity;
  }

  private boolean parkingSpotRequired() {
    return !entity.getCurrentItinerary().to.equals(entity.getHome())
        && entity.getContext().getParkingMap().containedInSpatialIndex(entity.getCurrentItinerary().to);
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
