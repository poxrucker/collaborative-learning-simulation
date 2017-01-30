package allow.simulator.flow.activity.person;

import java.util.List;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.flow.activity.MovementActivity;
import allow.simulator.knowledge.Experience;
import allow.simulator.mobility.planner.TType;
import allow.simulator.relation.Relation;
import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.world.Street;
import allow.simulator.world.StreetMap;
import allow.simulator.world.StreetSegment;

/**
 * Class representing driving Activity.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class Drive extends MovementActivity {

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
			currentSegment.addVehicle();
	}

	@Override
	public double execute(double deltaT) {
		if (currentSegment != null) currentSegment.removeVehicle();
		
		if (isFinished())
			return 0.0;
		
		// Note tStart.
		if (tStart == -1) {
			tStart = entity.getContext().getTime().getTimestamp();
		}
		Person p = (Person)entity;
		p.getRelations().addToUpdate(Relation.Type.DISTANCE);
		double rem = travel(deltaT);
		p.setPosition(getCurrentPosition());
		
		if (isFinished()) {
			
			for (Experience entry : experiences) {
				p.getExperienceBuffer().add(entry);
			}
		} else {
			currentSegment = getCurrentSegment();
			currentSegment.addVehicle();
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
				
				boolean replan = false;
				
				if (person.isInformed() && !checkedForBlockedStreets) {
					StreetMap map = (StreetMap) person.getContext().getWorld();
					replan = map.containsBlockedStreet(this.path);
					
					if (replan)
						System.out.println();
					checkedForBlockedStreets = true;
				}
				
				Street street = getCurrentStreet();

				if (replan || street.isBlocked()) {
					person.setInformed(true);
					person.getContext().getStatistics().reportDiscovery();
					person.getContext().getStatistics().reportReplaning();
					person.getFlow().clear();
					person.getFlow().addActivity(new Replan((Person) entity));
					setFinished();
					return deltaT;
				}
				
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
}
