package allow.simulator.flow.activity.publictransportation;

import java.util.List;

import allow.simulator.entity.Entity;
import allow.simulator.entity.PublicTransportation;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.flow.activity.MovementActivity;
import allow.simulator.knowledge.Experience;
import allow.simulator.knowledge.TravelExperience;
import allow.simulator.mobility.planner.TType;
import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.world.Street;
import allow.simulator.world.StreetSegment;

/**
 * Represents an activity to drive to a next stop of a trip of a means of
 * public transportation.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public class DriveToNextStop extends MovementActivity {

	private double fillingLevel;
	
	public DriveToNextStop(PublicTransportation entity, List<Street> path) {
		// Constructor of super class.
		super(ActivityType.DRIVE_TO_NEXT_STOP, entity, path);
		
		if (!path.isEmpty()) {
			currentSegment.addVehicle();
		}
	}

	@Override
	public double execute(double deltaT) {
		if (currentSegment != null) currentSegment.removeVehicle();

		if (isFinished()) {
			return 0;
		}
		// Note tStart.
		if (tStart == -1) {
			tStart = entity.getContext().getTime().getTimestamp();
			PublicTransportation t = (PublicTransportation) entity;
			fillingLevel = ((double) t.getPassengers().size()) / t.getCapacity();
		}
				
		// Transportation entity.
		PublicTransportation p = (PublicTransportation) entity;
				
		// Register relations update.
		//p.getRelations().addToUpdate(Relation.Type.BUS);		
		//p.getRelations().addToUpdate(Relation.Type.DISTANCE);
		
		// Move public transportation and passengers.
		double rem = travel(deltaT);
		p.setPosition(getCurrentPosition());
		
		for (Entity pass : p.getPassengers()) {
			pass.setPosition(p.getPosition());
		}
				
		if (isFinished()) {
					
			for (Experience ex : experiences) {
				p.getKnowledge().collect(ex);
				
				for (Entity pass : p.getPassengers()) {
					pass.getKnowledge().collect(ex);
				}
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
		
		while (deltaT < travelTime && !isFinished()) {
			// Get current state.
			StreetSegment s = getCurrentSegment();
			double v = s.getBusDrivingSpeed();
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
					
					Experience newEx = new TravelExperience(street,
							sumTravelTime,
							street.getLength() * 0.0008,
							TType.BUS, 
							tStart,
							tEnd,
							s.getNumberOfVehicles(),
							fillingLevel,
							((PublicTransportation) entity).getCurrentTrip().getTripId(),
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
}
