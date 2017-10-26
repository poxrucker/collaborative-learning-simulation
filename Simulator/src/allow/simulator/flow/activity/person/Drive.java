package allow.simulator.flow.activity.person;

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
import allow.simulator.world.StreetMap;
import allow.simulator.world.StreetSegment;
import de.dfki.parking.model.ParkingIndex;
import de.dfki.parking.model.ParkingIndex.ParkingIndexEntry;

/**
 * Class representing driving Activity.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class Drive extends MovementActivity<Person> {

	private boolean checkedForBlockedStreets;
	
	/**
	 * Creates new instance of the driving Activity.
	 * 
	 * @param person The person moving.
	 * @param path The path to drive.
	 */
	public Drive(Person entity, List<Street> path) {
		super(ActivityType.DRIVE, entity, path);
		
		if (!path.isEmpty())
			currentSegment.addVehicle(entity);
	}

	@Override
	public double execute(double deltaT) {
		if (currentSegment != null) 
			currentSegment.removeVehicle();
		
		if (isFinished())
			return 0.0;
		
		// Note tStart
		if (tStart == -1) {
			tStart = entity.getContext().getTime().getTimestamp();
		}
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
	
	/**
	 * 
	 * 
	 * @param travelTime Time interval for travelling.
	 * @return Time used to travel which may be less than travelTime,
	 * if journey finishes before travelTime is over.
	 */
	private double travel(double travelTime) {
		double deltaT = 0.0;
		
		Person person = (Person) entity;
	
		while (deltaT < travelTime && !isFinished()) {
			// Get current state.
			StreetSegment s = getCurrentSegment();
	    s.registerVehicle(person);

			if (person.getContext().getRoiStreets().contains(getCurrentStreet()) && person.isParticipating()) {
			  long time = person.getContext().getTime().getTimestamp();
			  person.getContext().getStatistics().reportVisitedLink(time, s);
			  
			  StreetSegment rev = getReverseSegment(getCurrentStreet(), s);
			  
			  if (rev != null)
		       person.getContext().getStatistics().reportVisitedLink(time, rev);
			}
			double v = s.getDrivingSpeed(); // * entity.getContext().getWeather().getCurrentState().getSpeedReductionFactor();
			Coordinate p = getCurrentPosition();
			
			// Compute distance to next segment (i.e. end of current segment).
			double distToNextSeg = Geometry.haversineDistance(p, s.getEndPoint());

			// Compute distance to travel within deltaT seconds.
			double distToTravel = (travelTime - deltaT) * v;
					
			if (distToTravel >= distToNextSeg) {
				// If distance to travel is bigger than distance to next segment,
				// a new log entry needs to be created.
				double tNextSegment = distToNextSeg / v;
				streetTravelTime += tNextSegment;

				distOnSeg = 0.0;
				segmentIndex++;
				
				Street street = getCurrentStreet();

				if (segmentIndex == street.getNumberOfSubSegments()) {
					double sumTravelTime = streetTravelTime; // + tNextSegment;
					tEnd = tStart + (long) sumTravelTime;
					
					Experience newEx = new Experience(street,
							sumTravelTime,
							street.getLength() * 0.00035,
							TType.CAR, 
							tStart,
							tEnd,
							s.getNumberOfVehicles(),
							0,
							null,
							entity.getContext().getWeather().getCurrentState());
					experiences.add(newEx);
					streetTravelTime = 0.0;
					distOnStreet = 0.0;
					streetIndex++;
					segmentIndex = 0;
					tStart = tEnd;
					
					// Parking spot model: If the end of a street is reached update parking map(s)
					updateParkingMap(street);
					
					// Construction site checks
					if (experiences.size() < path.size()) {
					  
					  if (checkForBlockedStreets())
					    return deltaT;
				  
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
		return "Drive " + entity;
	}
	
	private void updateParkingMap(Street street) {
	  // Count number of free parking spots
	  ParkingIndex parkingMap = entity.getContext().getParkingMap();
	  List<ParkingIndexEntry> parkings = parkingMap.getParkingsInStreet(street);
	  
	  if (parkings == null)
	    return;
	  
	  long time = entity.getContext().getTime().getTimestamp();

	  for (ParkingIndexEntry parking : parkings) {
	    int nSpots = parking.getParking().getNumberOfParkingSpots();
	    int nFreeSpots = parking.getParking().getNumberOfFreeParkingSpots();
	    
	    entity.getLocalParkingKnowledge().update(parking.getParking(), nSpots, nFreeSpots, time);

	    if (entity.hasSensorCar())
	      entity.getGlobalParkingKnowledge().update(parking.getParking(), nSpots, nFreeSpots, time);
	  }  
	}
	
	private boolean checkForBlockedStreets() {
    boolean intermediateReplaning = false;
  
    if (entity.isInformed() && !checkedForBlockedStreets) {
      StreetMap map = (StreetMap) entity.getContext().getWorld();
      intermediateReplaning = map.containsBlockedStreet(path);
      checkedForBlockedStreets = true;
    }
  
    Street nextStreet = getCurrentStreet();
  
    if (nextStreet.isBlocked())
      entity.setInformed(true);

    if (intermediateReplaning || nextStreet.isBlocked()) {
      entity.getFlow().clear();
      entity.getFlow().addActivity(new ReplanCarJourney(entity, nextStreet.getStartingNode().getPosition(), entity.getCurrentItinerary().to, !nextStreet.isBlocked()));
      setFinished();
      return true;
    }
    return false;
	}
	
	private StreetSegment getReverseSegment(Street street, StreetSegment seg) {
    StreetMap map = (StreetMap) entity.getContext().getWorld();
    Street rev = map.getStreetReduced(street.getEndNode(), street.getStartingNode());
    
    if (rev == null)
      return null;
    
    for (StreetSegment temp : rev.getSubSegments()) {
      
      if (temp.getStartingNode().equals(seg.getEndingNode()) 
          && temp.getEndingNode().equals(seg.getStartingNode()))
          return temp;
    }
    return null;
	}
}
