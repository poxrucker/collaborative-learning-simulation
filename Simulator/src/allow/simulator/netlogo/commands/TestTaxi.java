package allow.simulator.netlogo.commands;

import java.util.ArrayList;
import java.util.List;

import org.nlogo.agent.Agent;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;

import allow.simulator.entity.Person;
import allow.simulator.entity.TravelEvent;
import allow.simulator.mobility.data.TType;
import allow.simulator.mobility.planner.IPlannerService;
import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.mobility.planner.JourneyRequest;
import allow.simulator.mobility.planner.RequestId;
import allow.simulator.netlogo.agent.IAgentAdapter;

public class TestTaxi extends DefaultCommand {

	@Override
	public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {
		Agent a = (Agent) context.getAgent();
		
		if (a instanceof IAgentAdapter) {
			IAgentAdapter p = (IAgentAdapter) a;
			Person person = (Person) p.getEntity();
			IPlannerService taxiPlanner = person.getContext().getBikeRentalPlannerService();
			List<TravelEvent> t = person.getDailyRoutine().getDailyRoutine(1);
			JourneyRequest req = JourneyRequest.createRequest(t.get(0).getStartingPoint(), t.get(0).getDestination(), person.getContext().getTime().getCurrentDateTime(),
					false, false, new TType[] { TType.SHARED_BICYCLE }, new RequestId());
			List<Itinerary> temp = new ArrayList<Itinerary>();
			taxiPlanner.requestSingleJourney(req, temp);
			
		} else {
			throw new ExtensionException("Error: Calling agent must be of breed Person");
		}
		
	}
}
