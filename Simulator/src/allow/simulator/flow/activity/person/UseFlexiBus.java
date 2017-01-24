package allow.simulator.flow.activity.person;

import java.time.LocalTime;

import allow.simulator.entity.Entity;
import allow.simulator.entity.Bus;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.mobility.data.BusStop;
import allow.simulator.mobility.data.Trip;

public class UseFlexiBus extends Activity {
	// The stops to get in and out.
	private BusStop in;
	private BusStop out;
	private Trip trip;
		
	// The bus a person entered.
	private Bus b;
		
	// Earliest starting time of the activity.
	private LocalTime earliestStartingTime;
		
	public UseFlexiBus(Entity entity, BusStop start, BusStop dest, Trip trip, LocalTime departure) {
		super(ActivityType.USE_FLEXIBUS, entity);
		this.in = start;
		this.out = dest;
		this.trip = trip;
		earliestStartingTime = departure;
	}

	@Override
	public double execute(double deltaT) {
		return 0;
	}

}
