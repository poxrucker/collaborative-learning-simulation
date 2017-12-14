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
import allow.simulator.world.StreetSegment;

/**
 * Represents a cycling Activity.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class Cycle extends MovementActivity<Person> {
	
	/**
	 * Creates new instance of the cycling Activity.
	 * 
	 * @param person The person moving.
	 * @param path The path to cycle.
	 */
	public Cycle(Person entity, List<Street> path) {
		super(ActivityType.CYCLE, entity, path);
	}
	
	@Override
	public double execute(double deltaT) {		
		if (isFinished()) 
			return 0.0;
	
		// Note tStart.
		if (tStart == 0) {
			tStart = entity.getContext().getTime().getTimestamp();
		
      // Prepare entity state.
      entity.setPosition(getStartPoint());
		}

		// Register for knowledge exchange.
		entity.getRelations().addToUpdate(Relation.Type.DISTANCE);
		double rem = travel(deltaT);
		entity.setPosition(getCurrentPosition());

		if (isFinished()) {
			
			for (Experience ex : experiences) {
				entity.getExperienceBuffer().add(ex);
			}
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
		
		while (deltaT < travelTime && !isFinished()) {
			// Get current state.
			StreetSegment s = getCurrentSegment();
			double v = s.getCyclingSpeed();
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
							street.getLength() * 0.000005,
							TType.BICYCLE, 
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
	
	@Override
	public String toString() {
		return "Cycle " + entity;
	}
}
