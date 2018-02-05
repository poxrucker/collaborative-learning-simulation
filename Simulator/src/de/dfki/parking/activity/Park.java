package de.dfki.parking.activity;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.statistics.Statistics;
import allow.simulator.util.Geometry;
import allow.simulator.world.StreetNode;
import de.dfki.parking.model.Parking;
import de.dfki.parking.model.ParkingState;
import de.dfki.parking.utility.ParkingParameters;

public class Park extends Activity<Person> {

  private final StreetNode currentNode;

  public Park(Person entity, StreetNode currentNode) {
    super(ActivityType.DRIVE, entity);
    this.currentNode = currentNode;
  }

  @Override
  public double execute(double deltaT) {
    ParkingState parkingState = entity.getParkingState();
    Parking parking = parkingState.getParkingCandidate().getParking();

    // Check if there are free parking spots
    if (!parkingState.getParkingCandidate().getParking().hasFreeParkingSpot()) {
      // Update parking knowledge
      updateParkingMaps(parking, false);
      
      // Add SelectParkingSpot activity to find new alternative parking spot
      Activity<Person> initParkingSearch = new InitializeParkingSearch(entity); 
      entity.getFlow().addAfter(this, initParkingSearch);
      entity.getFlow().addAfter(initParkingSearch, new SelectParkingSpot(entity, currentNode));
      setFinished();
      return deltaT;
    }
    // Save end time of parking spot search
    parkingState.setSearchEndTime(entity.getContext().getTime().getTimestamp());

    // Assign parking spot to entity
    parkingState.setCurrentParking(parking);
    parking.park(entity);

    // Report parking spot search statistics
    reportStatistics();

    // Update parking maps
    updateParkingMaps(parking, true);
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
    ParkingState parkingState = entity.getParkingState();
    Statistics stats = entity.getContext().getStatistics();
    
    // Report success
    stats.reportSuccessfulParking();
    
    // Compute and report parking utility parameters
    double c = parkingState.getCurrentParking().getCurrentPricePerHour();
    double wd = Geometry.haversineDistance(entity.getPosition(), entity.getCurrentItinerary().to);
    double st = (parkingState.getSearchEndTime() - parkingState.getSearchStartTime()) / 1000.0;
    stats.reportSearchTimeParking((long) st);
    stats.reportParkingCosts(c);
    stats.reportParkingWalkingDistance(wd);
    
    // Compute and report parking utility
    double u = parkingState.getParkingUtility().computeUtility(new ParkingParameters(c, wd, st), parkingState.getParkingPreferences());
    stats.reportSearchUtility(u);
  }
}