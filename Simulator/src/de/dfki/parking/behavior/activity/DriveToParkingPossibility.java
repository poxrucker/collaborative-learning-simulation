package de.dfki.parking.behavior.activity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import allow.simulator.entity.Person;
import allow.simulator.exchange.Relation;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.flow.activity.MovementActivity;
import allow.simulator.knowledge.Experience;
import allow.simulator.mobility.planner.TType;
import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.util.Pair;
import allow.simulator.world.Street;
import allow.simulator.world.StreetSegment;
import de.dfki.parking.behavior.ParkingPossibility;
import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.index.ParkingIndexEntry;
import de.dfki.parking.utility.ParkingParameters;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Class representing driving Activity.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class DriveToParkingPossibility extends MovementActivity<Person> {

  /**
   * Creates new instance of the driving Activity.
   * 
   * @param person The person moving.
   * @param path The path to drive.
   */
  public DriveToParkingPossibility(Person entity, List<Street> path) {
    super(ActivityType.DRIVE, entity, path);
  }

  @Override
  public double execute(double deltaT) {
    // Note tStart
    if (tStart == 0) {
      tStart = entity.getContext().getTime().getTimestamp();
      
      // Prepare entity state
      entity.setPosition(getStartPoint());

      if (!path.isEmpty())
        currentSegment.addVehicle(entity);
    }
    
    if (currentSegment != null)
      currentSegment.removeVehicle();

    if (isFinished())
      return 0.0;

    entity.getRelations().addToUpdate(Relation.Type.DISTANCE);
    double rem = travel(deltaT);
    entity.setPosition(getCurrentPosition());

    if (isFinished()) {

      for (Experience entry : experiences) {
        entity.getExperienceBuffer().add(entry);
      }
      entity.getFlow().addAfter(this, new Park(entity, getCurrentStreet().getEndNode()));
      
    } else {
      currentSegment = getCurrentSegment();
      currentSegment.addVehicle(entity);
    }
    return rem;
  }

  private double travel(double travelTime) {
    double deltaT = 0.0;

    while (deltaT < travelTime && !isFinished()) {
      // Get current state.
      StreetSegment s = getCurrentSegment();
      double v = s.getDrivingSpeed(); // * entity.getContext().getWeather().getCurrentState().getSpeedReductionFactor();
      Coordinate p = getCurrentPosition();

      // Compute distance to next segment (i.e. end of current segment).
      double distToNextSeg = Geometry.haversineDistance(p, s.getEndPoint());

      // Compute distance to travel within deltaT seconds.
      double distToTravel = (travelTime - deltaT) * v;

      if (distToTravel >= distToNextSeg) {
        // If distance to travel is bigger than distance to next segment, a new log entry needs to be created.
        double tNextSegment = distToNextSeg / v;
        streetTravelTime += tNextSegment;

        distOnSeg = 0.0;
        segmentIndex++;

        Street street = getCurrentStreet();

        if (segmentIndex == street.getNumberOfSubSegments()) {
          double sumTravelTime = streetTravelTime; // + tNextSegment;
          tEnd = tStart + (long) sumTravelTime;

          Experience newEx = new Experience(street, sumTravelTime, street.getLength() * 0.00035, TType.CAR, tStart, tEnd, s.getNumberOfVehicles(), 0,
              null, entity.getContext().getWeather().getCurrentState());
          experiences.add(newEx);
          streetTravelTime = 0.0;
          distOnStreet = 0.0;
          streetIndex++;
          segmentIndex = 0;
          tStart = tEnd;

          // Parking spot model: If the end of a street is reached update parking map(s)
          if (updateParkingPossibilities(street)) {
            entity.getFlow().addAfter(this, new Park(entity, street.getEndNode()));
            setFinished();
          }
        }
        deltaT += tNextSegment;

      } else {
        // If distance to next segment is bigger than distance to travel,
        // update time on segment, travelled distance, and reset deltaT.
        streetTravelTime += (travelTime - deltaT);
        distOnSeg += distToTravel;
        distOnStreet += distToTravel;
        deltaT += (travelTime - deltaT);
      }
      if (experiences.size() == path.size())
        setFinished();
    }
    return deltaT;
  }

  public String toString() {
    return "DriveToParkingPossibility " + entity;
  }

  private boolean updateParkingPossibilities(Street street) {
    // Find all parking possibilities
    Collection<ParkingIndexEntry> parkingPossibilities = findParkingPossibilities(street);
    
    // Update knowledge for parking possibilities
    updateParkingKnowledge(parkingPossibilities);
    
    // Check if one of the parking possibilities has higher utility than currently selected parking possibility
    return updateCurrentParkingPossibility(parkingPossibilities, street);
  }

  private Collection<ParkingIndexEntry> findParkingPossibilities(Street street) {
    List<ParkingIndexEntry> parkingPossibilities = new ObjectArrayList<ParkingIndexEntry>();
    parkingPossibilities.addAll(findStreetParkingPossibilities(street));
    parkingPossibilities.addAll(findGarageParkingPossibilities(street));
    return parkingPossibilities;
  }
  
  private void updateParkingKnowledge(Collection<ParkingIndexEntry> parkingPossibilities) {
    long time = entity.getContext().getTime().getTimestamp();
    
    for (ParkingIndexEntry parking : parkingPossibilities) {
      int nSpots = parking.getParking().getNumberOfParkingSpots();
      int nFreeSpots = parking.getParking().getNumberOfFreeParkingSpots();
      double price = parking.getParking().getCurrentPricePerHour();

      entity.getLocalParkingKnowledge().update(parking.getParking(), nSpots, nFreeSpots, price, time);

      if (entity.hasSensorCar())
        entity.getGlobalParkingKnowledge().update(parking.getParking(), nSpots, nFreeSpots, price, time);
    }
  }
  
  private boolean updateCurrentParkingPossibility(Collection<ParkingIndexEntry> parkingPossibilities, Street street) {
    List<ParkingPossibility> temp = rank(parkingPossibilities, entity.getPosition(), entity.getCurrentItinerary().to);
    
    if (temp.size() == 0)
      return false; // If no possibility is available, return
    
    if ((entity.parkingCandidate != null) && temp.get(0).getParking().getId() == entity.parkingCandidate.getParking().getId())
      return false;
    
    if ((entity.parkingCandidate != null) && temp.get(0).getEstimatedUtility() < entity.parkingCandidate.getEstimatedUtility())
      return false; // If utility of first ranked possibility is worse than current candidate, return
    
    // Otherwise, update parking candidate and add Park activity
    entity.parkingCandidate = temp.get(0);
    return true;
  }
  
  private Collection<ParkingIndexEntry> findStreetParkingPossibilities(Street street) {
    ParkingIndex parkingMap = entity.getContext().getParkingMap();
    Collection<ParkingIndexEntry> entries = parkingMap.getParkingInStreet(street.getEndNode());

    if (entries == null)
      return Collections.emptyList();

    return entries;
  }

  private Collection<ParkingIndexEntry> findGarageParkingPossibilities(Street street) {
    ParkingIndex parkingMap = entity.getContext().getParkingMap();
    Collection<ParkingIndexEntry> entries = parkingMap.getParkingAtNode(street.getEndNode());
    
    if (entries == null)
      return Collections.emptyList();

    return entries;
  }
  
  private List<ParkingPossibility> rank(Collection<ParkingIndexEntry> parkings, Coordinate currentPosition, Coordinate destination) {
    List<Pair<ParkingIndexEntry, Double>> temp = new ObjectArrayList<>();

    for (ParkingIndexEntry parking : parkings) {
      double c = parking.getParking().getCurrentPricePerHour();
      double wd = Geometry.haversineDistance(parking.getReferencePosition(), destination);
      double st = (Geometry.haversineDistance(parking.getReferencePosition(), currentPosition) / 3.0);
      temp.add(new Pair<>(parking, entity.getParkingUtility().computeUtility(new ParkingParameters(c, wd, st), entity.getParkingPreferences())));
    }
    temp.sort((t1, t2) -> (int) (t2.second - t1.second));

    if (temp.size() > 0 && temp.get(0).second == 0.0)
      return new ObjectArrayList<>(0);
    
    List<ParkingPossibility> ret = new ObjectArrayList<>(temp.size());

    for (Pair<ParkingIndexEntry, Double> p : temp) {
      // Remove current position from list of possible positions
      // List<Coordinate> positions = new ObjectArrayList<>(p.first.getParkingIndexEntry().getAllAccessPositions());
      
      // Remove current position from list of possible positions
      // while (positions.remove(currentPosition)) {}
      // Coordinate t = positions.get(ThreadLocalRandom.current().nextInt(positions.size()));
      ret.add(new ParkingPossibility(p.first.getParking(), new Coordinate(currentPosition.x, currentPosition.y), p.second));
    }
    return ret;
  }
}