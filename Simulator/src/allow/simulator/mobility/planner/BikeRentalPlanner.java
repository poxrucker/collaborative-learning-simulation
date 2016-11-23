package allow.simulator.mobility.planner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.util.Coordinate;

public class BikeRentalPlanner implements IPlannerService {

	private List<OTPPlannerService> plannerServices;
	private Coordinate bikeRentalStation;
	
	public BikeRentalPlanner(List<OTPPlannerService> plannerServices, Coordinate bikeRentalStation) {
		this.plannerServices = plannerServices;
		this.bikeRentalStation = bikeRentalStation;
	}

	@Override
	public boolean requestSingleJourney(JourneyRequest request, List<Itinerary> itineraries) {
		TType modes[] = request.TransportTypes;
		boolean success = false;
		
		for (int i = 0; i < modes.length; i++) {
			Itinerary newIt = null;
			
			if (modes[i] == TType.SHARED_BICYCLE)
				newIt = createBikeRentalItinerary(request);
						
			if (newIt != null) {
				success = true;
				itineraries.add(newIt);
			}
		}
		return success;
	}

	private Itinerary createBikeRentalItinerary(JourneyRequest req) {
		// Get planner instance
		int randomPlannerId = ThreadLocalRandom.current().nextInt(plannerServices.size());
		IPlannerService planner = plannerServices.get(randomPlannerId);		
				
		// Request walking leg from starting point to bike rental station
		Leg walkingLeg = createWalkingLeg(req.From, bikeRentalStation, planner, req);
		
		if (walkingLeg == null)
			return null;
				
		// Request cycling leg from bike rental station to destination
		Itinerary bikeIt = createBikeItinerary(bikeRentalStation, req.To,planner, req, walkingLeg.endTime);
	
		if (bikeIt == null)
			return null;
		
		// Compose itinerary
		Itinerary ret = new Itinerary();
		ret.legs.add(walkingLeg);
		ret.legs.addAll(bikeIt.legs);
		ret.costs = walkingLeg.costs + bikeIt.costs;
		ret.startTime = walkingLeg.startTime;
		ret.endTime = bikeIt.endTime;
		ret.duration = (ret.endTime - ret.startTime) / 1000;
		ret.from = req.From;
		ret.to = req.To;
		ret.itineraryType = Itinerary.getItineraryType(ret);
		ret.reqId = req.ReqId;
		ret.reqNumber = req.ReqNumber;
		ret.walkDistance = walkingLeg.distance + bikeIt.walkDistance;
		ret.walkTime = (walkingLeg.endTime - walkingLeg.startTime) / 1000 + bikeIt.walkTime;
		ret.itineraryType = Itinerary.getItineraryType(ret);
		return ret;
	}
	
	private Leg createWalkingLeg(Coordinate from, Coordinate to, IPlannerService planner, JourneyRequest req2) {
		RequestId reqId = new RequestId();
		JourneyRequest req = JourneyRequest.createRequest(from, to, LocalDateTime.of(req2.Date, req2.DepartureTime), false, new TType[] { TType.WALK }, reqId);
		List<Itinerary> temp = new ArrayList<Itinerary>();
		planner.requestSingleJourney(req, temp);
		Itinerary candidateIt = null;
		
		for (Itinerary it : temp) {
			
			if (it.itineraryType == TType.WALK) {
				candidateIt = it;
				break;
			}
		}
				
		if (candidateIt == null)
			return null;		
		return candidateIt.legs.get(0);
	}
	
	private Itinerary createBikeItinerary(Coordinate from, Coordinate to, IPlannerService planner, JourneyRequest req2, long startTime) {
		RequestId reqId = new RequestId();
		JourneyRequest req = JourneyRequest.createRequest(from, to, LocalDateTime.of(req2.Date, req2.ArrivalTime), false, new TType[] { TType.BICYCLE }, reqId);
		List<Itinerary> temp = new ArrayList<Itinerary>();
		planner.requestSingleJourney(req, temp);
		Itinerary candidateIt = null;
		
		for (Itinerary it : temp) {
			
			if (it.itineraryType == TType.WALK) {
				candidateIt = it;
				break;
			}
		}
				
		if (candidateIt == null)
			return null;
		
		List<Leg> legs = candidateIt.legs;
		
		for (int i = 0; i < legs.size(); i++) {
			Leg leg = legs.get(i);
			long duration = leg.endTime - leg.startTime;
			
			if (i == 0) {
				leg.startTime = startTime;
				
			} else {
				leg.startTime = legs.get(i - 1).endTime;
			}
			leg.endTime = (leg.startTime + duration);
		}
		candidateIt.startTime = legs.get(0).startTime;
		candidateIt.endTime = legs.get(legs.size() - 1).endTime;
		return candidateIt;
	}
}
