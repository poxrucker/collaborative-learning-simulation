package allow.simulator.flow.activity.person;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import allow.simulator.entity.Person;
import allow.simulator.entity.Profile;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.mobility.planner.JourneyRequest;
import allow.simulator.mobility.planner.RequestId;
import allow.simulator.mobility.planner.TType;
import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.utility.Preferences;

/**
 * Class representing an Activity to request a journey.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class PlanJourney extends Activity {
	// Predefined set of means of transportation to be used in a journey.	
	private static final TType transitJourney[] = new TType[] { TType.TRANSIT, TType.WALK };
	private static final TType walkJourney[] = new TType[] { TType.WALK };
	private static final TType carJourney[] = new TType[] { TType.CAR, TType.WALK };
	private static final TType taxiJourney[] = new TType[] { TType.TAXI };
	
	// The start coordinate of the journey.
	private final Coordinate start;
	
	// The destination of the journey.
	private final Coordinate destination;
	private Future<List<Itinerary>> requestFuture;
	private int stepsWaited;
	
	/**
	 * Creates a new Activity to request a journey.
	 * 
	 * @param person The person executing the journey request.
	 */
	public PlanJourney(Person person, Coordinate start, Coordinate destination) {
		super(ActivityType.PLAN_JOURNEY, person);
		this.start = start;
		this.destination = destination;
	}
			
	@Override
	public double execute(double deltaT) {	
		// Person entity
		Person person = (Person) entity;
		
		// Update preferences
		double dist = Geometry.haversineDistance(start, destination);
		Preferences prefs = person.getRankingFunction().getPreferences();
		prefs.setTmax(1500);
		prefs.setCmax(2.5);
		prefs.setWmax(Math.min(dist, 1000));
		
		if (requestFuture == null) {
			RequestId reqId = new RequestId();
			List<JourneyRequest> requests = new ArrayList<JourneyRequest>();
			LocalDateTime date = person.getContext().getTime().getCurrentDateTime();
			
			// Car requests are now sent out in any case. If a person does not
			// own a private car or person left the car at home, a taxi request
			// is emulated
			if (person.getProfile() != Profile.CHILD) {
				
				if ((!person.hasCar() || (!person.isAtHome() && !person.hasUsedCar()))) {
					//requests.add(JourneyRequest.createRequest(start, destination, date, false, taxiJourney, reqId));
				
				} else {
					requests.add(JourneyRequest.createRequest(start, destination, date, false, carJourney, reqId, person.isInformed() ? "construction" : ""));
				}
			}
			
			if (!person.hasUsedCar()) {
				requests.add(JourneyRequest.createRequest(start, destination, date, false, transitJourney, reqId));
				requests.add(JourneyRequest.createRequest(start, destination, date, false, walkJourney, reqId));
			
				// if (person.hasBike())
				//	requests.add(createRequest(start, destination, date, time, bikeJourney, person, reqId, reqNumber++));
			
				// if (person.useFlexiBus())
				//	requests.add(createRequest(start, destination, date, time, flexiBusJourney, person, reqId, reqNumber++));
			}
			requestFuture = person.getContext().getJourneyPlanner().requestSingleJourney(requests, person.getBuffer());
			return deltaT;
			
		} else if (!requestFuture.isDone() && (stepsWaited < 0)) {
			stepsWaited++;
			return deltaT;
			
		} else {
			List<Itinerary> it = null;
			
			try {
				it = requestFuture.get();
				
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			
			if (it == null || it.size() == 0)  {
				// In case no trips were found, reset buffer.
				setFinished();
				person.setPosition(destination);
				return 0.0;
			}
			// In case response was received, rank alternatives.
			person.getFlow().addActivity(new RankAlternatives(person, it));
			setFinished();
			return 0.0;
		}
	}
	
	@Override
	public String toString() {
		return "[PlanJourney " + entity + "]";
	}
}