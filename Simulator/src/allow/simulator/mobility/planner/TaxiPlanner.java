package allow.simulator.mobility.planner;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.mobility.data.TType;
import allow.simulator.mobility.data.TaxiStop;
import allow.simulator.mobility.data.TaxiTrip;
import allow.simulator.util.Coordinate;
import allow.simulator.world.Street;

public final class TaxiPlanner implements IPlannerService {
	// List of planner services to create car trips
	private final List<OTPPlannerService> plannerServices;
	
	// Position of taxi rank 
	private final Coordinate taxiRank;

	// Buffer to store planned trips which can be requested 
	private final Map<String, TaxiTrip> tripBuffer;

	/**
	 * Creates a new instance of a TaxiPlanner service which operates on the
	 * underlying set of planners from the given taxi rank position.
	 * 
	 * @param plannerServices Set of planners to use for creating taxi itineraries
	 * @param taxiRank Position of taxi rank where all taxis start from
	 */
	public TaxiPlanner(List<OTPPlannerService> plannerServices, Coordinate taxiRank) {
		this.plannerServices = plannerServices;
		this.taxiRank = taxiRank;
		tripBuffer = new ConcurrentHashMap<String, TaxiTrip>();
	}
	
	@Override
	public boolean requestSingleJourney(JourneyRequest request, List<Itinerary> itineraries) {
		TType modes[] = request.TransportTypes;
		boolean success = false;
		
		for (int i = 0; i < modes.length; i++) {
			Itinerary newIt = null;
			
			if (modes[i] == TType.TAXI) {
				// If mode is taxi, plan a new single hop taxi itinerary
				newIt = createTaxiItinerary(request);
	
			} else if (modes[i] == TType.SHARED_TAXI) {
				// If mode is shared taxi, plan a new multihop taxi itinerary
				newIt = createSharedTaxiItinerary(request);
			}

			if (newIt != null) {
				success = true;
				itineraries.add(newIt);
			}
		}
		return success;
	}
	
	/**
	 * Returns a planned taxi trip for a certain trip Id. 
	 * 
	 * @param tripId Trip Id to get corresponding taxi trip
	 * @return TaxiTrip which corresponds to the given trip Id, or null if
	 * no such trip exists
	 */
	public TaxiTrip getTaxiTrip(String tripId) {
		return tripBuffer.remove(tripId);
	}

	private Itinerary createTaxiItinerary(JourneyRequest req) {
		// Get planner instance
		int randomPlannerId = ThreadLocalRandom.current().nextInt(plannerServices.size());
		IPlannerService planner = plannerServices.get(randomPlannerId);			
				
		// Query legs for complete journey (base - pickup - dest - base)
		List<Coordinate> locations = new ArrayList<Coordinate>(3);
		locations.add(taxiRank);
		locations.add(req.From);
		locations.add(req.To);
		locations.add(taxiRank);
		List<Leg> legs = createTripLegs(locations, planner, req);
		
		if (legs == null)
			return null;
		
		// Create TaxiTrip and buffer it
		TaxiTrip taxiTrip = createTaxiTrip(legs);
		tripBuffer.put(taxiTrip.getTripId(), taxiTrip);
		
		// Create itinerary to return
		Leg taxiLeg = legs.get(1);
		Itinerary ret = new Itinerary();
		ret.legs.add(taxiLeg);
		ret.costs = taxiLeg.costs;
		ret.duration = (taxiLeg.endTime - taxiLeg.startTime) / 1000;
		ret.endTime = taxiLeg.endTime;
		ret.from = taxiLeg.from;
		ret.startTime = taxiLeg.startTime;
		ret.to = taxiLeg.to;
		ret.transfers = 1;
		ret.transitTime = ret.duration;
		ret.itineraryType = Itinerary.getItineraryType(ret);
		ret.waitingTime = (long) (0.001 * (legs.get(0).endTime - legs.get(0).startTime));
		ret.initialWaitingTime = ret.waitingTime;
		return ret;
	}
	
	private Itinerary createSharedTaxiItinerary(JourneyRequest req) {
		// Get planner instance
		int randomPlannerId = ThreadLocalRandom.current().nextInt(plannerServices.size());
		IPlannerService planner = plannerServices.get(randomPlannerId);					
		List<Leg> legs = createTripLegsTS(req.Destinations, planner);

		if (legs == null)
			return null;

		// Create TaxiTrip and buffer it
		TaxiTrip taxiTrip = createTaxiTrip(legs);
		tripBuffer.put(taxiTrip.getTripId(), taxiTrip);

		// Create itinerary to return
		Leg taxiLeg = legs.get(1);
		Itinerary ret = new Itinerary();
		ret.legs.add(taxiLeg);
		ret.costs = taxiLeg.costs;
		ret.duration = (taxiLeg.endTime - taxiLeg.startTime) / 1000;
		ret.endTime = taxiLeg.endTime;
		ret.from = taxiLeg.from;
		ret.startTime = taxiLeg.startTime;
		ret.to = taxiLeg.to;
		ret.transfers = 1;
		ret.transitTime = ret.duration;
		return ret;
	}
	
	private List<Leg> createTripLegsTS(List<Coordinate> base, IPlannerService planner) {
		PermutationIterator<Coordinate> it = new PermutationIterator<Coordinate>(base);
		double minTime = Double.MAX_VALUE;
		List<Leg> ret = null;
		
		while (it.hasNext()) {
			List<Coordinate> c = it.next();
			List<Leg> temp = createTripLegs(c, planner, null);
			
			double time = 0.0;
			for (Leg l : temp) {
				time += (l.endTime - l.startTime);
			}
			
			if (time < minTime) {
				minTime = time;
				ret = temp;
			}
		}
		return ret;
	}
	
	private List<Leg> createTripLegs(List<Coordinate> locations, IPlannerService planner, JourneyRequest req) {
		if (locations.size() == 0)
			return null;
		
		List<Leg> ret = new ArrayList<Leg>(locations.size() - 1);
		
		for (int i = 0; i < locations.size() - 1; i++) {
			Leg l = createLeg(locations.get(i), locations.get(i + 1), planner, req);
			
			if (l == null) 
				return null;
			ret.add(l);
		}
		String agencyId = "taxiagency";
		String tripId = agencyId + "_trip_" + String.valueOf(ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE));
		ret.get(0).stopIdFrom = tripId + "_0";
		
		for (int i = 0; i < ret.size(); i++) {
			Leg current = ret.get(i);
			current.agencyId = agencyId;
			current.routeId = agencyId + "_route";
			current.tripId = tripId;
			current.stopIdTo = tripId + "_" + (i + 1);
			current.costs = current.distance * 0.001;
			current.mode = TType.TAXI;
			if (i == 0)
				continue;
			Leg previous = ret.get(i - 1);
			long currentDuration = current.endTime - current.startTime;
			current.startTime = previous.endTime;
			current.endTime = current.startTime + currentDuration;
			current.stopIdFrom = previous.stopIdTo;
		}
		return ret;
	}
	
	private Leg createLeg(Coordinate from, Coordinate to, IPlannerService planner, JourneyRequest req2) {
		RequestId reqId = new RequestId();
		LocalTime t = req2.ArrivalTime != null ? req2.ArrivalTime : req2.DepartureTime;
		JourneyRequest req = JourneyRequest.createRequest(from, to, LocalDateTime.of(req2.Date, t), false, new TType[] { TType.CAR }, reqId);
		List<Itinerary> temp = new ArrayList<Itinerary>();
		planner.requestSingleJourney(req, temp);
		Itinerary candidateIt = null;
		
		for (Itinerary it : temp) {
			
			if ((it.itineraryType == TType.CAR) && (it.legs.size() == 1)) {
				candidateIt = it;
				break;
			}
		}
				
		if (candidateIt == null)
			return null;			
		return candidateIt.legs.get(0);
	}
	
	private TaxiTrip createTaxiTrip(List<Leg> tripLegs) {
		final int nLegs = tripLegs.size();
		List<TaxiStop> stops = new ArrayList<TaxiStop>(nLegs);
		List<LocalTime> stopTimes = new ArrayList<LocalTime>(nLegs);
		List<List<Street>> traces = new ArrayList<List<Street>>(nLegs);
		
		for (Leg l : tripLegs) {
			stops.add(new TaxiStop(l.stopIdTo, l.to));	
			stopTimes.add(LocalDateTime.ofInstant(Instant.ofEpochMilli(l.endTime), ZoneId.of("UTC+2")).toLocalTime());
			traces.add(l.streets);
		}
		return new TaxiTrip(tripLegs.get(1).tripId, stops, stopTimes, traces);
	}	
}
