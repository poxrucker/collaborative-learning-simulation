package allow.simulator.flow.activity.person;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.parking.Parking;
import allow.simulator.parking.ParkingMap;
import allow.simulator.statistics.Statistics;
import allow.simulator.world.Street;
import allow.simulator.world.StreetMap;
import allow.simulator.world.StreetNode;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public final class FindParkingSpot extends Activity<Person> {

  private static final int DEFAULT_PARKING_DELAY = 7 * 60;

  // Street to look for parking spot
  private final Street current;

  // Time to wait
  private boolean hasParking;
  private double parkingTime;

  public FindParkingSpot(Person entity, Street street) {
    super(ActivityType.FIND_PARKING_SPOT, entity);
    this.current = street;
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

      // If parking is required, find one in current street
      Parking parking = findParking();

      if (parking == null) {
        // Check if parking threshold is exceeded
        if (maxSearchTimeExceeded()) {
          setHasParking();
          setParkingTime(0);
          reportStatistics(false);
          setFinished();
          return 0;
        }
        // If there is no parking in current street, choose next street to look for spot
        entity.getVisitedStreets().add(current);
        List<Street> nextStreets = getPathToNextStreet(current);

        if (nextStreets == null) {
          setHasParking();
          setParkingTime(0);
          reportStatistics(false);
          setFinished();
          return 0;
        }
        // Drive along next street and look for parking spot there
        Activity<Person> drive = new Drive(entity, nextStreets);
        entity.getFlow().addAfter(this, drive);
        entity.getFlow().addAfter(drive, new FindParkingSpot(entity, nextStreets.get(nextStreets.size() - 1)));
        setFinished();
        return 0;
      }
      
      // If parking was found, park car setting parking time
      parking.park(entity);
      entity.setCurrentParking(parking);
      setHasParking();
      setParkingTime(DEFAULT_PARKING_DELAY);
      return 0;
    }
   
    if (!parkingFinished()) {
        return park(deltaT);
    }
    
    // Parking is finished
    reportStatistics(true);
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
    return (entity.getContext().getTime().getTimestamp() - entity.getSearchStartTime()) >= 15 * 60 * 1000;
  } 
  
  private void reportStatistics(boolean success) {
    Statistics stats = entity.getContext().getStatistics();
    
    if (success) {
      stats.reportSuccessfulParking();
      stats.reportSearchTimeParking((entity.getContext().getTime().getTimestamp() - entity.getSearchStartTime()) / 1000.0);
      
    } else {
      stats.reportFailedParking();
    }
      
  }
  
  private Parking findParking() {
    // ParkingMap instance
    ParkingMap parkingMap = entity.getContext().getParkingMap();

    // Check if there is a free parking spot in the current street
    List<Parking> parkings = parkingMap.getParkingInStreet(current.getName());

    if (parkings == null) 
      return null;

    // Choose suitable parking if available
    Parking parking = chooseParking(parkings);
    return parking;
  }

  private Parking chooseParking(List<Parking> parkings) {

    for (Parking parking : parkings) {

      if (parking.hasFreeParkingSpot())
        return parking;
    }
    return null;
  }

  private List<Street> getPathToNextStreet(Street currentStreet) {
    // Get all outgoing streets starting at current intersection
    Set<StreetNode> visitedNodes = getVisitedNodes(entity.getVisitedStreets());
    List<List<Street>> paths = getInitialPaths();
    
    if (paths == null)
      return null;
    
    List<List<Street>> candidates = null;

    while ((candidates = getCandidatePaths(paths, visitedNodes)).size() == 0) {
      paths = updatePaths(paths);
    }
    return chooseRandomPath(candidates);
  }

  private List<List<Street>> getInitialPaths() {
    // Get all streets starting at the end of the current street from StreetMap
    // instance
    StreetMap map = (StreetMap) entity.getContext().getWorld();
    List<Street> outgoingStreets = new ObjectArrayList<>(map.getOutgoingEdges(current.getEndNode()));

    // Create a separate path for each candidate street
    List<List<Street>> initialCandidates = new ObjectArrayList<>(outgoingStreets.size());

    for (Street street : outgoingStreets) {
      
      if (!isValidStreet(street))
        continue;
      
      initialCandidates.add(new ObjectArrayList<>(new Street[] { current, street }));
    }
    return initialCandidates;
  }

  private List<List<Street>> getCandidatePaths(List<List<Street>> paths, Set<StreetNode> visitedNodes) {
    // Get all destination nodes from initial candidates
    List<List<Street>> candidates = new ObjectArrayList<>(paths.size());

    for (List<Street> path : paths) {
      // Check if last street end on node which has already been visited
      Street last = path.get(path.size() - 1);

      if (visitedNodes.contains(last.getEndNode()))
        continue;

      candidates.add(path);
    }
    return candidates;
  }

  private List<List<Street>> updatePaths(List<List<Street>> paths) {
    // Get all streets starting at the end of each path from StreetMap instance
    StreetMap map = (StreetMap) entity.getContext().getWorld();
    List<List<Street>> ret = new ObjectArrayList<>();

    for (List<Street> path : paths) {
      Street last = path.get(path.size() - 1);
      List<Street> outgoingStreets = new ObjectArrayList<>(map.getOutgoingEdges(last.getEndNode()));

      for (Street street : outgoingStreets) {
        
        if (!isValidStreet(street))
          continue;
          
        List<Street> newPath = new ObjectArrayList<>(path.size() + 1);
        newPath.addAll(path);
        newPath.add(street);
        ret.add(newPath);
      }
    }
    return ret;
  }

  private boolean isValidStreet(Street street) {
    return !(street.getName().equals("Fußweg")
        || street.getName().equals("Bürgersteig")
        || street.getName().equals("Stufen")
        || street.getName().equals("Weg") || street.getName().equals("Gasse")
        || street.getName().equals("Fußgängertunnel")
        || street.getName().equals("Fahrradweg")
        || street.getName().equals("Fußgängerbrücke"));
  }
  private Set<StreetNode> getVisitedNodes(List<Street> path) {
    Set<StreetNode> nodes = new ObjectOpenHashSet<>();
    nodes.add(path.get(0).getStartingNode());

    for (Street street : path) {
      nodes.add(street.getEndNode());
    }
    return nodes;
  }

  private void setHasParking() {
    hasParking = true;
  }

  private void setParkingTime(double parkingTime) {
    this.parkingTime = parkingTime;
  }

  private List<Street> chooseRandomPath(List<List<Street>> streets) {
    return streets.get(ThreadLocalRandom.current().nextInt(streets.size()));
  }
}
