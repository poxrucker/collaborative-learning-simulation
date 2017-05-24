package allow.simulator.flow.activity.publictransportation;

import java.time.Duration;
import java.time.LocalTime;

import allow.simulator.core.Time;
import allow.simulator.entity.Bus;
import allow.simulator.flow.activity.Activity;
import allow.simulator.mobility.data.Stop;

/**
 * Represents an activity for a means of public transportation to approach a
 * stop of its trip and wait until departure time in case it is too early.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class PickUpAndWait extends Activity<Bus> {
	// Stop to approach
	private Stop stop;
	
	// Time stop trip departs from this stop
	private LocalTime time;
	private int day;
	
	// Flags
	private boolean approached;
	
	/**
	 * Creates a new instance of the activity specifying the transportation entity,
	 * the stop to approach, and the departure time.
	 * 
	 * @param entity Public transportation entity approaching the stop.
	 * @param stop Stop to approach.
	 * @param time Time to depart form the stop.
	 */
	public PickUpAndWait(Bus entity, Stop stop, LocalTime time) {
		// Constructor of super class.
		super(ActivityType.PICKUP_AND_WAIT, entity);
		
		// Stop.
		this.stop = stop;
		approached = false;
		
		// Day the stop is approached.
		this.time = time;// .plusSeconds(30);
		day = entity.getContext().getTime().getDays();
	}

	@Override
	public double execute(double deltaT) {		
		// If stop has not been approached yet (first time execute is called)
		// set transport to stop and return
		if (!approached) {
			entity.setCurrentStop(stop);
			entity.setPosition(stop.getPosition());
			stop.addTransportationEntity(entity);
			approached = true;
			return deltaT;
		}
		
		// Get current time
		Time currentTime = entity.getContext().getTime();
					
		if ((currentTime.getDays() > day || currentTime.getCurrentTime().isAfter(time))) {
			// Remove transportation from current stop
			stop.removeTransportationEntity(entity);
			entity.setCurrentStop(null);
			setFinished();
			
			// Update delay when departing
			long currentDelay = Duration.between(time, currentTime.getCurrentTime()).getSeconds();
			entity.setCurrentDelay(currentDelay);
			return deltaT;
		}
		return deltaT;
	}
}
