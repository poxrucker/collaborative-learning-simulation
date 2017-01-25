package allow.simulator.flow.activity.publictransportation;

import java.time.Duration;
import java.time.LocalTime;

import allow.simulator.core.Time;
import allow.simulator.entity.Bus;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.mobility.data.BusStop;

/**
 * Represents an activity for a means of public transportation to approach a
 * stop of its trip and wait until departure time in case it is too early.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public class PickUpAndWait extends Activity {
	// Stop to approach.
	private BusStop stop;
	
	// Time stop trip departs from this stop.
	private LocalTime time;
	private int day;
	
	// Flags.
	private boolean approached;
	
	/**
	 * Constructor.
	 * Creates a new instance of the activity specifying the transportation entity,
	 * the stop to approach, and the departure time.
	 * 
	 * @param entity Public transportation entity approaching the stop.
	 * @param stop Stop to approach.
	 * @param time Time to depart form the stop.
	 */
	public PickUpAndWait(Bus entity, BusStop stop, LocalTime time) {
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
		// Bus entity
		Bus p = (Bus) entity;
				
		// If stop has not been approached yet (first time execute is called)
		// set transport to stop and return.
		if (!approached) {
			tStart = entity.getContext().getTime().getTimestamp();
			p.setCurrentStop(stop);
			p.setPosition(stop.getPosition());
			stop.addPublicTransportation(p);
			approached = true;
			return deltaT;
		}
		
		// Get current time.
		Time currentTime = p.getContext().getTime();
					
		if ((currentTime.getDays() > day || currentTime.getCurrentTime().isAfter(time))) {
			// Remove transportation from current stop.
			stop.removePublicTransportation(p);
			p.setCurrentStop(null);
			setFinished();
			
			// Update delay when departing.
			long currentDelay = Duration.between(time, currentTime.getCurrentTime()).getSeconds();
			p.setCurrentDelay(currentDelay);
			return deltaT;
		}
		return deltaT;
	}
}
