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
import allow.simulator.world.Street;
import allow.simulator.world.StreetNode;
import de.dfki.parking.behavior.ParkingPossibility;
import de.dfki.parking.model.Parking;
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
    // Check if it is the first attempt to find a parking spot
    if (entity.getSearchStartTime() == 0) {
      entity.setSearchStartTime(entity.getContext().getTime().getTimestamp());
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
    ParkingPossibility possibleParking = entity.getParkingSelectionStrategy().selectParking(currentNode, dest, currentTime, arrivalTime);     

    // If parking spot candidate was found, calculate path, add Drive and FindParkingSpot activities
    if (possibleParking != null) {
      entity.parkingCandidate = possibleParking;

      // Calculate path to parking spot
      if (!possibleParking.getPosition().equals(entity.getPosition())) {
        List<Street> path = getPathToParking(possibleParking.getPosition(), false);

        // Add Drive and FindParkingSpot activities
        if (path != null && path.size() > 0) {
          Activity<Person> drive = new DriveToParkingSpot(entity, path, possibleParking);
          entity.getFlow().addAfter(this, drive);
          Activity<Person> park = new Park(entity, path.get(path.size() - 1).getEndNode());
          entity.getFlow().addAfter(drive, park);

        } else if (path == null) {
          System.out.println("No path to parking found initial");
        }

      } else {
        Activity<Person> park = new Park(entity, currentNode);
        entity.getFlow().addAfter(this, park);
      }
      
    } else {
      System.out.println("No parking found");
    }
    setFinished();
    return 0;
  }

  public String toString() {
    return "FindParkingSpot " + entity;
  }

  private boolean parkingSpotRequired() {
    return !entity.getCurrentItinerary().to.equals(entity.getHome())
        && entity.getContext().getParkingMap().containedInSpatialIndex(entity.getCurrentItinerary().to);
  }

 
  private void updateParkingMaps(Parking parking, boolean updateGlobal) {
    long time = entity.getContext().getTime().getTimestamp();
    int nSpots = parking.getNumberOfParkingSpots();
    int nFreeSpots = parking.getNumberOfFreeParkingSpots();
    double price = parking.getCurrentPricePerHour();
    
    entity.getLocalParkingKnowledge().update(parking, nSpots, nFreeSpots, price, time);

    if (updateGlobal)
        entity.getGlobalParkingKnowledge().update(parking, nSpots, nFreeSpots, price, time);  
  }

  private void reportFailure(int reason) {
    Statistics stats = entity.getContext().getStatistics();
    stats.reportFailedParking(reason);
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
