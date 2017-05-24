package allow.simulator.flow.activity.publictransportation;

import allow.simulator.entity.Bus;
import allow.simulator.entity.BusAgency;
import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;

/**
 * Represents an activity to go back to a transport agency if trip is finished.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public class ReturnToAgency extends Activity<Bus> {
		
	public ReturnToAgency(Bus entity) {
		super(ActivityType.RETURN_TO_AGENCY, entity);
	}

	@Override
	public double execute(double deltaT) {

		if (entity.getPassengers().size() > 0) {
			System.out.println("Warning: Passengers still on public transportation " + entity + " of trip " + entity.getCurrentTrip().getTripId());

			for (int i = 0; i < entity.getPassengers().size(); i++) {
				Person pers = entity.getPassengers().get(i);
				System.out.println("  " + pers.toString() + " " + pers.getFlow().getCurrentActivity().toString());
			}
			//throw new IllegalStateException("Error: " + p + " returning to agency still has passengers");
		}
		// Finish trip at agency
		BusAgency agency = (BusAgency) entity.getTransportationAgency();
		agency.finishTrip(entity.getCurrentTrip(), entity);
		
		// Reset state and return to agency.
		entity.setCurrentStop(null);
		entity.setCurrentTrip(null);
		setFinished();
		return deltaT;
	}
}