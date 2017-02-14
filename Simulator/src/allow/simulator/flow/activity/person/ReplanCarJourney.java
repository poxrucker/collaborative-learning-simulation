package allow.simulator.flow.activity.person;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.mobility.planner.JourneyRequest;
import allow.simulator.mobility.planner.RequestId;
import allow.simulator.util.Coordinate;

/**
 * Class representing an Activity to request a journey.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class ReplanCarJourney extends Activity<Person> {
	// The starting coordinate of the journey
	private final Coordinate start;
	
	// The destination of the journey
	private final Coordinate destination;
	
	// Indicates that the replaning is initiated as the entity was informed
	// by another entity though information exchange
	private final boolean informedByOthers;
		
	// Future containing the results of queries to the planner
	private Future<List<Itinerary>> requestFuture;
	
	/**
	 * Creates a new Activity to replan a car journey.
	 * 
	 * @param person The person executing the journey request.
	 */
	public ReplanCarJourney(Person person, Coordinate start, Coordinate destination, boolean informedByOthers) {
		super(ActivityType.PLAN_JOURNEY, person);
		this.start = start;
		this.destination = destination;
		this.informedByOthers = informedByOthers;
	}
			
	@Override
	public double execute(double deltaT) {	
		
		if (requestFuture == null) {
			entity.setReplanning(true);

			if (informedByOthers)
				entity.getContext().getStatistics().reportIntermediateReplaning();
			else 
				entity.getContext().getStatistics().reportConstructionSiteReplaning();
			
			RequestId reqId = new RequestId();
			List<JourneyRequest> requests = new ArrayList<JourneyRequest>();
			LocalDateTime date = entity.getContext().getTime().getCurrentDateTime();
			String plannerId = entity.getContext().getSimulationParameters().Scenario;
			requests.add(JourneyRequest.createDriveRequest(start, destination, date, false, reqId, plannerId));
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
				entity.setPosition(destination);
				return 0.0;
			}
			
			// In case response was received, rank alternatives
			entity.getFlow().addActivity(new PrepareJourney(entity, it.get(0)));
			setFinished();
			return 0.0;
		}
	}
	
	@Override
	public String toString() {
		return "[ReplanCarJourney " + entity + "]";
	}
}