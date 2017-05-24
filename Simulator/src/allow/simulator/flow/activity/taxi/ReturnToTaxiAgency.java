package allow.simulator.flow.activity.taxi;

import allow.simulator.entity.Person;
import allow.simulator.entity.Taxi;
import allow.simulator.entity.TaxiAgency;
import allow.simulator.flow.activity.Activity;

public final class ReturnToTaxiAgency extends Activity<Taxi> {
	
	public ReturnToTaxiAgency(Taxi taxi) {
		super(ActivityType.RETURN_TO_TAXI_AGENCY, taxi);
	}

	@Override
	public double execute(double deltaT) {

		if (entity.getPassengers().size() > 0) {
			System.out.println("Warning: Passengers still on taxi "
							+ entity
							+ " of trip "
							+ entity.getCurrentTrip().getTripId());

			for (int i = 0; i < entity.getPassengers().size(); i++) {
				Person pers = entity.getPassengers().get(i);
				System.out.println("  " + pers.toString() + " " + pers.getFlow().getCurrentActivity().toString());
			}
		}
		// Finish trip at agency
		TaxiAgency agency = (TaxiAgency) entity.getTransportationAgency();
		agency.finishTrip(entity.getCurrentTrip().getTripId(), entity);

		// Reset state and return to agency
		entity.setCurrentStop(null);
		entity.setCurrentTrip(null);
		setFinished();
		return deltaT;
	}
}