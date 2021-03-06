package allow.simulator.flow.activity.person;

import java.time.LocalTime;

import allow.simulator.entity.Person;
import allow.simulator.entity.Taxi;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.mobility.data.TaxiStop;
import allow.simulator.relation.Relation;

/**
 * Represents an activity to use public transport (bus).
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class UseTaxi extends Activity {
	// The stops to get in and out.
	private TaxiStop in;
	private TaxiStop out;

	// The bus a person entered.
	private Taxi taxi;

	// Earliest starting time of the activity.
	private LocalTime earliestStartingTime;

	// Utility state variables.
	private boolean reachedStop;
	private boolean enteredTaxi;
	private boolean leftTaxi;

	/**
	 * Creates new activity to use a taxi given start and end stop.
	 * 
	 * @param person
	 *            Person to execute the activity
	 * @param start
	 *            Starting stop
	 * @param dest
	 *            Destination stop
	 * @param departure
	 *            Time when taxi is expected to depart from stop
	 */
	public UseTaxi(Person person, TaxiStop start, TaxiStop dest,
			Taxi taxi, LocalTime departure) {
		super(ActivityType.USE_PUBLIC_TRANSPORT, person);
		earliestStartingTime = departure;
		this.taxi = taxi;
		in = start;
		out = dest;
	}

	/**
	 * Executes one step of this activity.
	 */
	@Override
	public double execute(double deltaT) {
		// Register relations update.
		entity.getRelations().addToUpdate(Relation.Type.DISTANCE);

		// Get entity.
		Person person = (Person) entity;
		
		if (!reachedStop) {
			// If person has not reached stop yet, set position, add person to waiting passengers, and set flag.
			person.setPosition(in.getPosition());
			in.addWaitingPerson(person);
			reachedStop = true;
			return 0.0;
			
		} else if (!enteredTaxi) {
			// If person has not entered the correct means yet, check in stop for waiting vehicles.
			if (in.hasWaitingTaxi()) {
				Taxi temp = in.getTaxi();
				
				if (taxi != temp)
					throw new IllegalStateException();
				taxi.addPassenger(person);
				in.removeWaitingPerson(person);
				enteredTaxi = true;
			}
			
		} else if (enteredTaxi && !leftTaxi) {
			// If person entered taxi, update position.
			if ((taxi.getCurrentStop() != null) && (taxi.getCurrentStop().getStopId().equals(out.getStopId()))) {
				taxi.removePassenger(person);
				leftTaxi = true;
				setFinished();
			}
		}
		return deltaT;
	}

	public String toString() {
		return "UseTaxi " + entity.toString() + " from "
				+ in.getStopId() + " to " + out.getStopId()
				+ "earliest starting: " + earliestStartingTime.toString();
	}
}
