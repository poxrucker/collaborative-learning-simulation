package de.dfki.parking.activity;

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
import allow.simulator.util.Coordinate;
import allow.simulator.world.Street;
import allow.simulator.world.StreetNode;
import de.dfki.parking.model.ParkingPossibility;
import de.dfki.parking.model.ParkingState;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class SelectParkingSpot extends Activity<Person> {
  // Current street to look for parking spot
  private final StreetNode currentNode;
  
  public SelectParkingSpot(Person entity, StreetNode currentNode) {
    super(ActivityType.FIND_PARKING_SPOT, entity);
    this.currentNode = currentNode;
  }

  @Override
  public double execute(double deltaT) {
    ParkingState parkingState = entity.getParkingState();
    
    // Check if it is the first attempt to find a parking spot
    if (parkingState.getSearchStartTime() == 0) {
      parkingState.setSearchStartTime(entity.getContext().getTime().getTimestamp());
    }
    
    // Check if parking spot needs to be found at all (e.g. entity parks at home)
    if (!parkingSpotRequired()) {
      setFinished();
      return 0;
    }

    // Otherwise, select parking spot using selection strategy
    Coordinate dest = entity.getCurrentItinerary().to;
    long currentTime = entity.getContext().getTime().getTimestamp();
    long arrivalTime = entity.getCurrentItinerary().endTime;
    ParkingPossibility possibleParking = entity.getParkingBehavior().getSelectionStrategy().selectParking(currentNode, dest, currentTime, arrivalTime);     

    if (possibleParking != null) {
      parkingState.setParkingCandidate(possibleParking);

      // Calculate path to parking spot
      if (!possibleParking.getPosition().equals(entity.getPosition())) {
        List<Street> path = getPathToParking(possibleParking.getPosition(), false);

        if (path != null && path.size() > 0) {
          Activity<Person> drive = new DriveToDestination(entity, path);
          entity.getFlow().addAfter(this, drive);
          entity.getFlow().addAfter(drive, new Park(entity, path.get(path.size() - 1).getEndNode()));

        } else if (path == null) {
          // System.out.println("No path to parking found initial");
        }

      } else {
        entity.getFlow().addAfter(this, new Park(entity, currentNode));
      }
      setFinished();
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
        Activity<Person> park = new SelectParkingSpot(entity, path.get(path.size() - 1).getEndNode());
        entity.getFlow().addAfter(drive, park);

      } else if (path == null){
        // System.out.println("No path to parking found fallback");
      }
      setFinished();
      return 0;
    }
    // Otherwise, do fallback
    entity.getContext().getStatistics().reportFailedParking();
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

  private List<Street> getPathToParking(Coordinate to, boolean fallback) {
    
    if (to.equals(entity.getPosition()))
        return new ObjectArrayList<>(0);
    
    Coordinate from = entity.getPosition();
    LocalDateTime date = entity.getContext().getTime().getCurrentDateTime();
    JourneyRequest req = JourneyRequest.createDriveRequest(from, to, date, false, new RequestId(), "");
    List<JourneyRequest> temp = Arrays.asList(new JourneyRequest[] { req });

    try {
      List<Itinerary> it = entity.getContext().getJourneyPlanner().requestSingleJourney(temp, new ArrayList<Itinerary>(1)).get();

      if (it.size() == 0)
        return null;

      return it.get(0).legs.get(0).streets;

    } catch (InterruptedException | ExecutionException e) {
      return null;
    }
  }
}