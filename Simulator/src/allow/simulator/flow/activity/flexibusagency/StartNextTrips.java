package allow.simulator.flow.activity.flexibusagency;

import allow.simulator.entity.Entity;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;

public class StartNextTrips extends Activity {

	public StartNextTrips(Entity entity) {
		super(ActivityType.SCHEDULE_NEXT_FLEXIBUS_TRIPS, entity);
	}

	@Override
	public double execute(double deltaT) {

		return deltaT;
	}

}
