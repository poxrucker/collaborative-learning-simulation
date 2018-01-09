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
import allow.simulator.world.Street;
import allow.simulator.world.StreetSegment;
import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.index.ParkingIndexEntry;
import de.dfki.parking.model.Parking;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Class representing driving Activity.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class DriveToDestination extends MovementActivity<Person> {

  /**
   * Creates new instance of the driving Activity.
   * 
   * @param person The person moving.
   * @param path The path to drive.
   */
  public DriveToDestination(Person entity, List<Street> path) {
    super(ActivityType.DRIVE, entity, path);
  }
  
  /**
   * Creates new instance of the driving Activity.
   * 
   * @param person The person moving.
   * @param path The path to drive.
   */
  public DriveToDestination(Person entity, List<Street> path, double distanceToStop) {
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
          updateParkingPossibilities(street);
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
    return "DriveToDestination " + entity;
  }

  private void updateParkingPossibilities(Street street) {
    // Find all parking possibilities
    Collection<Parking> parkingPossibilities = findParkingPossibilities(street);
    
    // Update knowledge for parking possibilities
    updateParkingKnowledge(parkingPossibilities);
  }

  private Collection<Parking> findParkingPossibilities(Street street) {
    List<Parking> parkingPossibilities = new ObjectArrayList<Parking>();
    parkingPossibilities.addAll(findStreetParkingPossibilities(street));
    parkingPossibilities.addAll(findGarageParkingPossibilities(street));
    return parkingPossibilities;
  }
  
  private void updateParkingKnowledge(Collection<Parking> parkingPossibilities) {
    long time = entity.getContext().getTime().getTimestamp();
    
    for (Parking parking : parkingPossibilities) {
      int nSpots = parking.getNumberOfParkingSpots();
      int nFreeSpots = parking.getNumberOfFreeParkingSpots();
      double price = parking.getCurrentPricePerHour();
      entity.getParkingBehavior().getUpdateStrategy().update(parking, nSpots, nFreeSpots, price, time, false);
    }
  }
  
  private Collection<Parking> findStreetParkingPossibilities(Street street) {
    ParkingIndex parkingMap = entity.getContext().getParkingMap();
    Collection<ParkingIndexEntry> entries = parkingMap.getParkingInStreet(street.getEndNode());
    return (entries != null) ? toParkingCollection(entries) : Collections.emptyList();
  }

  private Collection<Parking> findGarageParkingPossibilities(Street street) {
    ParkingIndex parkingMap = entity.getContext().getParkingMap();
    Collection<ParkingIndexEntry> entries = parkingMap.getParkingAtNode(street.getEndNode());   
    return (entries != null) ? toParkingCollection(entries): Collections.emptyList();
  }
  
  private static Collection<Parking> toParkingCollection(Collection<ParkingIndexEntry> entryCollection) {
    Collection<Parking> ret = new ObjectArrayList<>(entryCollection.size());
    
    for (ParkingIndexEntry entry : entryCollection) {
      ret.add(entry.getParking());
    }
    return ret;
  }
}