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
import allow.simulator.world.StreetMap;

/**
 * Class representing an Activity to request a journey.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class PlanJourney extends Activity<Person> {
	// The starting coordinate of the journey
	private final Coordinate start;
	
	// The destination of the journey
	private final Coordinate destination;
	
	// Future containing the results of queries to the planner
	private Future<List<Itinerary>> requestFuture;
	
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
		
		if (requestFuture == null) {
			RequestId reqId = new RequestId();
			List<JourneyRequest> requests = new ArrayList<JourneyRequest>();
			LocalDateTime date = entity.getContext().getTime().getCurrentDateTime();
			// String plannerId = entity.isInformed() ? entity.getContext().getSimulationParameters().Scenario : "";
			// Car requests are now sent out in any case. If a person does not
			// own a private car or person left the car at home, a taxi request
			// is emulated
			if (entity.getProfile() != Profile.CHILD) {
				
				if ((!entity.hasCar() || (!entity.isAtHome() && !entity.hasUsedCar()))) {
					//requests.add(JourneyRequest.createRequest(start, destination, date, false, taxiJourney, reqId));
				
				} else {
					requests.add(JourneyRequest.createDriveRequest(start, destination, date, false, reqId, ""));
					requests.add(JourneyRequest.createDriveRequest(start, destination, date, false, reqId, entity.getContext().getSimulationParameters().Scenario));
				}
			}
			
			if (!entity.hasUsedCar()) {
				requests.add(JourneyRequest.createTransitRequest(start, destination, date, false, reqId, ""));
				requests.add(JourneyRequest.createWalkRequest(start, destination, date, false, reqId, ""));
			
				// if (person.hasBike())
				//	requests.add(createRequest(start, destination, date, time, bikeJourney, person, reqId, reqNumber++));
			
				// if (person.useFlexiBus())
				//	requests.add(createRequest(start, destination, date, time, flexiBusJourney, person, reqId, reqNumber++));
			}
			requestFuture = entity.getContext().getJourneyPlanner().requestSingleJourney(requests, new ArrayList<Itinerary>(requests.size()));
			return deltaT;
			
		} else {
			List<Itinerary> it = null;
			
			try {
				it = requestFuture.get();
				
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			
			if ((it == null) || (it.size() == 0))  {
				// In case no trips were found, finish and set entity to destination
				setFinished();
				entity.getFlow().addActivity(new CorrectPosition(entity, destination));
				return 0.0;
			}
			
			if (affectedByRoadBlock(it)) {
				
				if (entity.isInformed()) {
					entity.setOriginalTravelTime(it.get(1).duration);
					entity.setOriginalTripDistance(it.get(1).legs.get(0).distance);
					it.remove(0);

					if (!entity.isReplanning())
						entity.getContext().getStatistics().reportInformedPlanningAffected();

				} else {
					entity.setOriginalTravelTime(it.get(0).duration);
					entity.setOriginalTripDistance(it.get(0).legs.get(0).distance);
					it.remove(1);
					
					if (!entity.isReplanning())
						entity.getContext().getStatistics().reportPlanningAffected();
				}
				
			} else if ((it.size() > 1) && (it.get(0).itineraryType == TType.CAR) && (it.get(1).itineraryType == TType.CAR)) {
				it.remove(1);
				
			} else if ((it.size() > 1) && (it.get(0).itineraryType == TType.CAR) && (it.get(1).itineraryType != TType.CAR)) {
				it.remove(0);
			}
			
			if (it.size() == 0)  {
				// In case no trips were found, finish and set entity to destination
				setFinished();
				entity.getFlow().addActivity(new CorrectPosition(entity, destination));
				return 0.0;
			}
			
			if (!entity.isReplanning()) {
				
				if (entity.isInformed())
					entity.getContext().getStatistics().reportInformedPlanning();
				else
					entity.getContext().getStatistics().reportPlanning();
			}
			// In case response was received, rank alternatives.
			entity.getFlow().addActivity(new RankAlternatives(entity, it));
			setFinished();
			return 0.0;
		}
	}
	
	private boolean affectedByRoadBlock(List<Itinerary> it) {
		
		if (it.size() <= 1)
			return false;
		
		StreetMap map = (StreetMap)entity.getContext().getWorld();
		
		if ((it.get(0).itineraryType == TType.CAR) && (it.get(1).itineraryType != TType.CAR))
			return false;
		
		if ((it.get(0).itineraryType != TType.CAR) || (it.get(1).itineraryType != TType.CAR))
			return false;
		
		return map.containsBlockedStreet(it.get(0).legs.get(0).streets);
	}
	
	@Override
	public String toString() {
		return "[PlanJourney " + entity + "]";
	}
}