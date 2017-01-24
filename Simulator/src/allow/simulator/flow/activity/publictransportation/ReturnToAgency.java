package allow.simulator.flow.activity.publictransportation;

import allow.simulator.entity.Person;
import allow.simulator.entity.Bus;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;

/**
 * Represents an activity to go back to a transport agency if trip is finished.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public class ReturnToAgency extends Activity {
		
	public ReturnToAgency(Bus entity) {
		// Constructor of super class.
		super(ActivityType.RETURN_TO_AGENCY, entity);
	}

	@Override
	public double execute(double deltaT) {
		// Note tStart.
		if (tStart == -1) {
			tStart = entity.getContext().getTime().getTimestamp();
		}
				
		// Transportation entity.
		Bus p = (Bus) entity;
				
		// Register relations update.
		// p.getRelations().addToUpdate(Relation.Type.BUS);
		
		if (p.getPassengers().size() > 0) {
			System.out.println("Warning: Passengers still on public transportation " + p.toString() + " of trip " + p.getCurrentTrip().getTripId());

			for (int i = 0; i < p.getPassengers().size(); i++) {
				Person pers = p.getPassengers().get(i);
				System.out.println("  " + pers.toString() + " " + pers.getFlow().getCurrentActivity().toString());
			}
			//throw new IllegalStateException("Error: " + p + " returning to agency still has passengers");
		}
		// Finish trip at agency.
		p.getTransportationAgency().finishTrip(p.getCurrentTrip(), p);
		
		// Reset state and return to agency.
		p.setCurrentStop(null);
		p.setCurrentTrip(null);
		setFinished();
		return deltaT;
	}
}