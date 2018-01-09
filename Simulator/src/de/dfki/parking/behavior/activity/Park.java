package de.dfki.parking.behavior.activity;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.statistics.Statistics;
import allow.simulator.util.Geometry;
import allow.simulator.world.StreetNode;
import de.dfki.parking.model.Parking;
import de.dfki.parking.utility.ParkingParameters;

public class Park extends Activity<Person> {

  private final StreetNode currentNode;

  public Park(Person entity, StreetNode currentNode) {
    super(ActivityType.DRIVE, entity);
    this.currentNode = currentNode;
  }

  @Override
  public double execute(double deltaT) {
    // Check if there are free parking spots
    if (!entity.parkingCandidate.getParking().hasFreeParkingSpot()) {
      // Update parking knowledge
      updateParkingMaps(entity.parkingCandidate.getParking(), entity.hasSensorCar());
      
      // Add SelectParkingSpot activity to find new alternative parking spot
      entity.getFlow().addAfter(this, new SelectParkingSpot(entity, currentNode));
      setFinished();
      return 0;
    }
    // Save end time of parking spot search
    entity.setSearchEndTime(entity.getContext().getTime().getTimestamp());

    // Assign parking spot to entity
    entity.setCurrentParking(entity.parkingCandidate.getParking());
    entity.parkingCandidate.getParking().park(entity);

    // Report parking spot search statistics
    reportStatistics();

    // Update parking maps
    updateParkingMaps(entity.parkingCandidate.getParking(), entity.isUser());
    setFinished();
    return deltaT;
  }

  private void updateParkingMaps(Parking parking, boolean parked) {
    long time = entity.getContext().getTime().getTimestamp();
    int nSpots = parking.getNumberOfParkingSpots();
    int nFreeSpots = parking.getNumberOfFreeParkingSpots();
    double price = parking.getCurrentPricePerHour();
    entity.getParkingBehavior().getUpdateStrategy().update(parking, nSpots, nFreeSpots, price, time, parked);
  }
  
  private void reportStatistics() {
    Statistics stats = entity.getContext().getStatistics();
    
    // Report success
    stats.reportSuccessfulParking();
    
    // Compute and report parking utility parameters
    double c = entity.parkingCandidate.getParking().getCurrentPricePerHour();
    double wd = Geometry.haversineDistance(entity.getPosition(), entity.getCurrentItinerary().to);
    double st = (entity.getSearchEndTime() - entity.getSearchStartTime()) / 1000.0;
    stats.reportSearchTimeParking((long) st);
    stats.reportParkingCosts(c);
    stats.reportParkingWalkingDistance(wd);
    
    // Compute and report parking utility
    double u = entity.getParkingUtility().computeUtility(new ParkingParameters(c, wd, st), entity.getParkingPreferences());
    stats.reportSearchUtility(u);
  }
}