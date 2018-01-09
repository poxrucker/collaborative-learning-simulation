package de.dfki.parking.behavior.activity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.mobility.planner.JourneyRequest;
import allow.simulator.mobility.planner.RequestId;
import allow.simulator.statistics.Statistics;
import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.world.Street;
import allow.simulator.world.StreetNode;
import de.dfki.parking.model.Parking;
import de.dfki.parking.model.ParkingPossibility;
import de.dfki.parking.utility.ParkingParameters;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class FindParkingSpot extends Activity<Person> {
  // Delay in seconds modeling the time between a having found a parking spot and finishing parking
  private static final int DEFAULT_PARKING_DELAY = 0 * 60;

  // Current street to look for parking spot
  private final StreetNode currentNode;

  // Indicates if user has selected a parking spot it wants to park in
  private boolean parkingSpotSelected;

  // Selected parking spot candidate
  private Parking parkingSpotCandidate;

  // Indicates if user has found a parking spot in the current street
  private boolean parkingSpotFound;

  // Counts down the time necessary to park
  private double parkingTime;
  
  public FindParkingSpot(Person entity, StreetNode currentNode) {
    super(ActivityType.FIND_PARKING_SPOT, entity);
    this.currentNode = currentNode;
  }

  public FindParkingSpot(Person entity, StreetNode currentNode, Parking parkingCandidate) {
    super(ActivityType.FIND_PARKING_SPOT, entity);
    this.currentNode = currentNode;
    this.parkingSpotCandidate = parkingCandidate;
    parkingSpotSelected = true;
    parkingSpotFound = false;
  }

  @Override
  public double execute(double deltaT) {
    // Check if it is the first attempt to find a parking spot
    if (entity.getSearchStartTime() == 0) {
      entity.setSearchStartTime(entity.getContext().getTime().getTimestamp());
      
      /*if (parkingSpotRequired())
        entity.getContext().getGuidanceSystem().getParkingPossibility(entity.parkingRequestId);*/
    }
    // Check if entity has already selected a parking spot
    if (!parkingSpotSelected) {

      // Check if parking spot needs to be found at all (e.g. entity parks at home)
      if (!parkingSpotRequired()) {
        // Set parking spot selected
        parkingSpotSelected = true;

        // Set parking spot found
        parkingSpotFound = true;

        // Set parking time to reduced delay
        parkingTime = DEFAULT_PARKING_DELAY / 2.0;
        
        // Save end time of parking spot search
        entity.setSearchEndTime(entity.getContext().getTime().getTimestamp());
        setFinished();
        return 0;
      }

      // Otherwise, select parking spot using selection strategy
      Coordinate dest = entity.getCurrentItinerary().to;
      long currentTime = entity.getContext().getTime().getTimestamp(); 
      long arrivalTime = entity.getCurrentItinerary().endTime;
      ParkingPossibility possibleParking = entity.getParkingBehavior().getSelectionStrategy().selectParking(currentNode, dest, currentTime, arrivalTime);     
      
      // If parking spot candidate was found, calculate path, add Drive and FindParkingSpot activities
      if (possibleParking != null) {        
        // Set parking spot selected
        parkingSpotSelected = true;

        // Set parking spot candidate
        parkingSpotCandidate = possibleParking.getParking();
        
        // Calculate path to parking spot
        if (!possibleParking.getPosition().equals(entity.getPosition())) {
          List<Street> path = getPathToParking(possibleParking.getPosition(), false);

          // Add Drive and FindParkingSpot activities
          if (path != null && path.size() > 0) {
            Activity<Person> drive = new DriveToDestination(entity, path);
            entity.getFlow().addAfter(this, drive);
            Activity<Person> park = new FindParkingSpot(entity, path.get(path.size() - 1).getEndNode(), parkingSpotCandidate);
            entity.getFlow().addAfter(drive, park);
            setFinished();

          } else if (path == null) {
            // System.out.println("No path to parking found initial");
          }
        }       
        return 0;
      }
      // Select next destination to look for parking possibility
      Coordinate next = entity.getParkingBehavior().getExplorationStrategy().findNextPossibleParking(entity.getPosition(), dest, currentTime);
      
      if (next != null) {
        // Calculate path to parking spot
        List<Street> path = getPathToParking(next, true);

        // Add Drive and FindParkingSpot activities
        if (path != null && path.size() > 0) {
          Activity<Person> drive = new DriveToDestination(entity, path);
          entity.getFlow().addAfter(this, drive);
          Activity<Person> park = new FindParkingSpot(entity, path.get(path.size() - 1).getEndNode());
          entity.getFlow().addAfter(drive, park);

        } else if (path == null){
          // System.out.println("No path to parking found fallback");
        }
        setFinished();
        return 0;
      }
      // Otherwise, do fallback
      // reportFailure(1);
      setFinished();
      return deltaT;
    }

    if (parkingSpotSelected && !parkingSpotFound) {
      // If parking spot was selected and entity has arrived, check if there are
      // any free spots available
      if (parkingSpotCandidate.getNumberOfFreeParkingSpots() > 0) {
        // Set parkingFound flag
        parkingSpotFound = true;
        
        // Save end time of parking spot search
        entity.setSearchEndTime(entity.getContext().getTime().getTimestamp());
        
        // Set parking time
        parkingTime = DEFAULT_PARKING_DELAY;

        // Assign parking spot to entity
        entity.setCurrentParking(parkingSpotCandidate);

        // Assign entity to parking spot
        parkingSpotCandidate.park(entity);
        
        // Update parking maps
        updateParkingState(parkingSpotCandidate, true);
        return 0;
      } 
      // Otherwise, update knowledge and reset parking spot selected     
      updateParkingState(parkingSpotCandidate, false);
      parkingSpotSelected = false;
      return 0;
      
    } 
    if (parkingSpotSelected && parkingSpotFound && !parkingFinished()) {
      // Do parking until delay is zero
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

  private boolean parkingSpotRequired() {
    return !entity.getCurrentItinerary().to.equals(entity.getHome())
        && entity.getContext().getParkingMap().containedInSpatialIndex(entity.getCurrentItinerary().to);
  }

  private double park(double deltaT) {
    double diff = Math.min(deltaT, parkingTime);
    parkingTime -= diff;
    return diff;
  }

  private boolean parkingFinished() {
    return parkingTime <= 0.0;
  }

  private void updateParkingState(Parking parking, boolean parked) {
    long time = entity.getContext().getTime().getTimestamp();
    int nSpots = parking.getNumberOfParkingSpots();
    int nFreeSpots = parking.getNumberOfFreeParkingSpots();
    double price = parking.getCurrentPricePerHour();
    entity.getParkingBehavior().getUpdateStrategy().update(parking, nSpots, nFreeSpots, price, time, parked);
  }

  private void reportFailure(int reason) {
    Statistics stats = entity.getContext().getStatistics();
    stats.reportFailedParking(reason);
  }

  private void reportSuccess() {
    Statistics stats = entity.getContext().getStatistics();
    stats.reportSuccessfulParking();
    long searchTime = (long)((entity.getSearchEndTime() - entity.getSearchStartTime()) / 1000.0);
    stats.reportSearchTimeParking(searchTime);

    double c = parkingSpotCandidate.getCurrentPricePerHour();
    double wd = Geometry.haversineDistance(entity.getPosition(), entity.getCurrentItinerary().to);
    double st = (double)searchTime;
    double u = entity.getParkingUtility().computeUtility(new ParkingParameters(c, wd, st), entity.getParkingPreferences());
    stats.reportSearchUtility(u);
    stats.reportParkingCosts(c);
    stats.reportParkingWalkingDistance(wd);
  }

  private List<Street> getPathToParking(Coordinate to, boolean fallback) {
    
    if (to.equals(entity.getPosition()))
        return new ObjectArrayList<>(0);
    
    Coordinate from = entity.getPosition();
    LocalDateTime date = entity.getContext().getTime().getCurrentDateTime();
    JourneyRequest req = JourneyRequest.createDriveRequest(from, to, date, false, new RequestId(), "");
    List<JourneyRequest> temp = Arrays.asList(new JourneyRequest[] { req });

    try {
      List<Itinerary> it = entity.getContext().getJourneyPlanner().requestSingleJourney(temp, new ArrayList<Itinerary>(1)).get();

      if (it.size() == 0) {
//        StreetMap map = (StreetMap)entity.getContext().getWorld();
//        List<StreetNode> first = map.getStreetNodeFromPosition(from.y + "," + from.x);
//        List<StreetNode> second = map.getStreetNodeFromPosition(to.y + "," + to.x);
//        System.out.println("No " + (fallback ? "fallback " : " ") + "journey from " + first + " ("  + from + ") to " + second + "(" + to + ")");
        return null;
      }

      return it.get(0).legs.get(0).streets;

    } catch (InterruptedException | ExecutionException e) {
      return null;
    }

  }
}
