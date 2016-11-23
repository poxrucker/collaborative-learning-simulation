package allow.simulator.adaptation;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import allow.simulator.entity.Entity;
import allow.simulator.entity.EntityType;
import allow.simulator.entity.Person;
import allow.simulator.entity.PublicTransportation;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.flow.activity.person.PrepareJourney;
import allow.simulator.flow.activity.person.UsePublicTransport;
import allow.simulator.mobility.data.TType;
import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.mobility.planner.JourneyPlanner;
import allow.simulator.mobility.planner.JourneyRequest;
import allow.simulator.mobility.planner.RequestId;
import allow.simulator.util.Coordinate;

public class SelfishAdaptation implements IAdaptationStrategy {

	private JourneyPlanner planner;
	
	public SelfishAdaptation(JourneyPlanner planner) {
		this.planner = planner;
	}
	
	@Override
	public void solveAdaptation(Issue issue, Ensemble ensemble) {
		
		switch (issue) {
		
		case BUS_BREAKDOWN:
			solveBusBreakdown(ensemble);
			break;
			
		case BUS_DELAY:
		case ROAD_BLOCK:
		default:
			throw new IllegalArgumentException("No solver for issue " + issue);
		
		}
	}
	
	private static final TType transitJourney[] = new TType[] { TType.TRANSIT, TType.WALK };
	private static final TType walkJourney[] = new TType[] { TType.WALK };
	private static final TType taxiJourney[] = new TType[] { TType.TAXI };

	private void solveBusBreakdown(Ensemble ensemble) {
		Collection<IEnsembleParticipant> temp = new ArrayList<IEnsembleParticipant>(ensemble.getEntities());
		PublicTransportation bus = (PublicTransportation) ensemble.getCreator();
		
		for (IEnsembleParticipant participant : temp) {
			Entity entity = (Entity) participant;
			
			if (entity.getType() == EntityType.PERSON) {
				Person person = (Person) entity;
				
				if (person.getFlow().getCurrentActivity().getType() != ActivityType.USE_PUBLIC_TRANSPORT) {
					System.out.println(person);
				}
				UsePublicTransport activity = (UsePublicTransport) person.getFlow().getCurrentActivity();
				//LocalTime departure = activity.getEarliestStartingTime().plusMinutes(2);
				LocalTime departure = person.getContext().getTime().getCurrentTime().plusMinutes(2);
				Coordinate start = person.getPosition();
				Coordinate destination = person.getCurrentItinerary().to;  // Will come from current itinerary property
				
				RequestId reqId = new RequestId();
				LocalDateTime date = person.getContext().getTime().getCurrentDateTime().toLocalDate().atTime(departure); // Will come from context

				// Create requests
				List<JourneyRequest> requests = new ArrayList<JourneyRequest>();
				requests.add(JourneyRequest.createRequest(start, destination, date, false, taxiJourney, reqId));
				// requests.add(JourneyRequest.createRequest(start, destination, date, false, transitJourney, reqId));
				requests.add(JourneyRequest.createRequest(start, destination, date, false, walkJourney, reqId));
				
				List<Itinerary> ret = new ArrayList<Itinerary>();
				Future<List<Itinerary>> future = planner.requestSingleJourney(requests, ret);
				
				try {
					future.get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
				bus.removePassenger(person);
				person.getFlow().clear();
				person.setReplanning(true);
				ret = person.getUtility().rankAlternatives(person.getPreferences(), ret);
				
				if (ret.get(0).itineraryType == TType.TAXI) {
					person.getContext().getStatistics().reportAdaptation();
				}
				person.getFlow().addActivity(new PrepareJourney(person, ret.get(0)));	
				ensemble.removeEntity(person);
			}
		}
	}
	
}
