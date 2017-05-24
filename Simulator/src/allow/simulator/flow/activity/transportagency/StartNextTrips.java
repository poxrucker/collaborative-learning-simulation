package allow.simulator.flow.activity.transportagency;

import java.util.List;

import allow.simulator.entity.Bus;
import allow.simulator.entity.BusAgency;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.publictransportation.PrepareTrip;
import allow.simulator.mobility.data.PublicTransportationTrip;

public class StartNextTrips extends Activity<BusAgency> {

	public StartNextTrips(BusAgency agency) {
		super(ActivityType.SCHEDULE_NEXT_TRIPS, agency);
	}

	@Override
	public double execute(double deltaT) {
		// Get next trips from agency.
		List<PublicTransportationTrip> nextTrips = entity.getTripsToSchedule(entity.getContext().getTime().getCurrentDateTime());

		// Schedule a new bus for each trip.
		for (PublicTransportationTrip t : nextTrips) {
			// Get next free transportation vehicle.
			Bus b = entity.scheduleTrip(t);

			// Assign new trip to vehicle.
			b.getFlow().addActivity(new PrepareTrip(b, t));
		}
		// Activity is never finished.
		return deltaT;
	}

	@Override
	public boolean isFinished() {
		return false;
	}
}