package allow.simulator.flow.activity.taxi;

import java.time.Duration;
import java.time.LocalTime;

import allow.simulator.entity.Taxi;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.mobility.data.Stop;

public class PickupOrDrop extends Activity {
	// The stop to pickup or drop off a passenger
	private Stop stop;
	private LocalTime stopTime;
	
	// Flags.
	private boolean approached;

	public PickupOrDrop(Taxi taxi, Stop stop, LocalTime stopTime) {
		super(ActivityType.PICK_UP_OR_DROP, taxi);
		this.stop = stop;
		this.stopTime = stopTime;
	}

	@Override
	public double execute(double deltaT) {
		// Transportation entity.
		Taxi taxi = (Taxi) entity;

		// If stop has not been approached yet (first time execute is called)
		// set transport to stop and return.
		if (!approached) {
			tStart = entity.getContext().getTime().getTimestamp();
			taxi.setCurrentStop(stop);
			taxi.setPosition(stop.getPosition());
			stop.addTransportationEntity(taxi);
			approached = true;
			return deltaT;
		}
		// Otherwise, remove taxi from stop
		stop.removeTransportationEntity(taxi);
		taxi.setCurrentStop(null);
		
		// Update delay when departing.
		long currentDelay = Duration.between(stopTime, taxi.getContext().getTime().getCurrentTime()).getSeconds();
		taxi.setCurrentDelay(currentDelay);
		setFinished();
		return 0;
	}

}
