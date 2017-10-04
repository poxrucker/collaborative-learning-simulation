package allow.simulator.flow.activity.person;

import java.util.List;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.parking.IParkingSearchStrategy;
import allow.simulator.parking.IParkingSelectionStrategy;
import allow.simulator.parking.Parking;
import allow.simulator.statistics.Statistics;
import allow.simulator.world.Street;

public final class FindParkingSpot extends Activity<Person> {
  // Delay in seconds modeling the time between a having found a parking spot
  // and finishing parking
  private static final int DEFAULT_PARKING_DELAY = 5 * 60;

  // Maximum time parking spot searching is executed before it is reported
  // as a failure
  private static final int MAX_SEARCH_TIME = 15 * 60;
  
  // Current street to look for parking spot
  private final Street current;

  // Selection strategy to apply when there is more than one possibility in the current street
  private final IParkingSelectionStrategy selectionStrategy;
 
  // Search strategy to apply when there is no parking spot in the current street
  private final IParkingSearchStrategy searchStrategy;
  
  // Indicates if user has found a parking spot in the current street
  private boolean hasParking;
  
  // Counts down the time necessary to park
  private double parkingTime;

  public FindParkingSpot(Person entity, Street current,
      IParkingSelectionStrategy selectionStrategy,
      IParkingSearchStrategy searchStrategy) {
    super(ActivityType.FIND_PARKING_SPOT, entity);
    this.current = current;
    this.selectionStrategy = selectionStrategy;
    this.searchStrategy = searchStrategy;
  }

  @Override
  public double execute(double deltaT) {
    // Check if it is the first attempt to find a parking spot
    if (entity.getSearchStartTime() == 0)
      entity.setSearchStartTime(entity.getContext().getTime().getTimestamp());
    
    // Check if entity has already found a parking spot
    if (!hasParking()) {
      
      // Check if parking is not required at all (e.g. destination is home)
      if (!parkingRequired()) {
        // Set parking flag
        setHasParking();

        // Await shorter parking time
        setParkingTime(DEFAULT_PARKING_DELAY / 2.0);
        return 0;
      }
      // Check if there is a free parking spot in the current street
      Parking parking = selectionStrategy.selectParking(current, 0);

      if (parking == null) {
        // Check if parking threshold is exceeded
        if (maxSearchTimeExceeded()) {
          setHasParking();
          setParkingTime(0);
          reportFailure(0);
          setFinished();
          return 0;
        }
        // If there is no parking in current street, choose next street to look for spot
        List<Street> nextStreets = searchStrategy.getPathToNextPossibleParking(current);

        if (nextStreets == null) {
          setHasParking();
          setParkingTime(0);
          reportFailure(1);
          setFinished();
          return 0;
        }
        // Drive along next street and look for parking spot there
        Activity<Person> drive = new Drive(entity, nextStreets);
        entity.getFlow().addAfter(this, drive); 
        Activity<Person> park = new FindParkingSpot(entity, nextStreets.get(nextStreets.size() - 1), selectionStrategy, searchStrategy);
        entity.getFlow().addAfter(drive, park);
        setFinished();
        return 0;
      }
      
      // If parking was found, park car setting parking time
      parking.park(entity);
      entity.setCurrentParking(parking);
      entity.setSearchEndTime(entity.getContext().getTime().getTimestamp());
      setHasParking();
      setParkingTime(DEFAULT_PARKING_DELAY);
      return 0;
    }
   
    if (!parkingFinished()) {
        return park(deltaT);
    }
    
    // Parking is finished
    reportSuccess();
    setFinished();
    return deltaT;
  }

  public String toString() {
    return "FindParkingSpot " + entity;
  }

  private boolean parkingRequired() {
    return !current.getEndNode().getPosition().equals(entity.getHome());
  }

  private boolean hasParking() {
    return hasParking;
  }

  private boolean parkingFinished() {
    return parkingTime <= 0.0;
  }

  private double park(double deltaT) {
    double diff = Math.min(deltaT, parkingTime);
    parkingTime -= diff;
    return diff;
  }
  
  private boolean maxSearchTimeExceeded() {
    return (entity.getContext().getTime().getTimestamp() - entity.getSearchStartTime()) >= MAX_SEARCH_TIME * 1000;
  } 

  private void reportFailure(int reason) {
    Statistics stats = entity.getContext().getStatistics();
    stats.reportFailedParking(reason);
  }
  
  private void reportSuccess() {
    Statistics stats = entity.getContext().getStatistics();
    stats.reportSuccessfulParking();
    stats.reportSearchTimeParking((entity.getSearchEndTime() - entity.getSearchStartTime()) / 1000.0);     
  }
 
  private void setHasParking() {
    hasParking = true;
    entity.setSearchEndTime(entity.getContext().getTime().getTimestamp());
  }

  private void setParkingTime(double parkingTime) {
    this.parkingTime = parkingTime;
  }

  
}
