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
	public static final double COST_PER_METER = 0.001;
	
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
		ret.initialWaitingTime = (legs.get(0).endTime - legs.get(0).startTime) / 1000;
		ret.waitingTime = 0;
		ret.duration = ((taxiLeg.endTime - taxiLeg.startTime) / 1000 + ret.waitingTime);
		ret.endTime = taxiLeg.endTime;
		ret.from = taxiLeg.from;
		ret.startTime = taxiLeg.startTime;
		ret.to = taxiLeg.to;
		ret.transfers = 1;
		ret.transitTime = ret.duration;
		ret.itineraryType = TType.TAXI;
		return ret;
	}
	
	private Itinerary createSharedTaxiItinerary(JourneyRequest req) {
		// Get planner instance
		int randomPlannerId = ThreadLocalRandom.current().nextInt(plannerServices.size());
		IPlannerService planner = plannerServices.get(randomPlannerId);					
		List<Leg> legs = createTripLegsTS(req, planner);

		if (legs == null)
			return null;

		// Create TaxiTrip and buffer it
		TaxiTrip taxiTrip = createTaxiTrip(legs);
		tripBuffer.put(taxiTrip.getTripId(), taxiTrip);

		List<Street> streets = new ArrayList<Street>();
		double totalCosts = 0.0;
		for (int i = 1; i < legs.size() - 1; i++) {
			Leg temp = legs.get(i);
			streets.addAll(temp.streets);
			totalCosts += COST_PER_METER * temp.distance;
		}
		
		Itinerary ret = new Itinerary();
		List<Itinerary> subItineraries = new ArrayList<Itinerary>(req.Destinations.size());
		
		long initialWaitingTime = (legs.get(0).endTime - legs.get(0).startTime) / 1000;
		long startingTime = legs.get(0).endTime;
		String stopIdFrom = legs.get(0).stopIdTo;
		long durationAcc = 0;
		double distAcc = 0.0;
		int streetsOffset = 0;
		Coordinate startingPos = legs.get(0).to;
		
		// Create itineraries to return
		for (int i = 1; i < legs.size() - 1; i++) {
			// Get current leg and update accumulated values
			Leg temp = legs.get(i);
			durationAcc += ((temp.endTime - temp.startTime) / 1000);
			distAcc += temp.distance;
			int streetsStart = streetsOffset;
			streetsOffset += temp.streets.size();
			
			// Create new leg and update with accumulated values
			Leg leg = new Leg();
			leg.agencyId = temp.agencyId;
			leg.routeId = temp.routeId;
			leg.tripId = temp.tripId;
			leg.costs = totalCosts / req.Destinations.size();
			leg.distance = distAcc;
			leg.startTime = startingTime;
			leg.endTime = temp.endTime;
			leg.from = startingPos;
			leg.to = temp.to;
			leg.mode = TType.TAXI;
			leg.stopIdFrom = stopIdFrom;
			leg.stopIdTo = temp.stopIdTo;
			leg.streets = streets.subList(streetsStart, streetsOffset);
			
			Itinerary subIt = new Itinerary();
			subIt.legs.add(leg);
			subIt.from = leg.from;
			subIt.to = leg.to;
			subIt.startTime = leg.startTime;
			subIt.endTime = leg.endTime;
			subIt.initialWaitingTime = initialWaitingTime;
			subIt.waitingTime = 0;
			subIt.transitTime = durationAcc;
			subIt.duration = subIt.transitTime + subIt.waitingTime;
			subIt.transfers = 1;
			subIt.reqId = req.ReqId;
			subIt.reqNumber = req.ReqNumber;
			subIt.itineraryType = TType.TAXI;
			subIt.costs = leg.costs;
			subItineraries.add(subIt);
		}
//		durationAcc += initialWaitingTime;
//		Leg last = legs.get(legs.size() - 1);
//		durationAcc += (last.endTime - last.startTime) / 1000;
		ret.initialWaitingTime = initialWaitingTime;
		ret.subItineraries = subItineraries;
		ret.costs = totalCosts;
		ret.duration = durationAcc;
		ret.startTime = subItineraries.get(0).startTime;
		ret.endTime = subItineraries.get(subItineraries.size() - 1).endTime;
		ret.from = subItineraries.get(0).from;
		ret.to = subItineraries.get(subItineraries.size() - 1).to;
		ret.transfers = 1;
		ret.transitTime = ret.duration;
		ret.reqId = req.ReqId;
		ret.legs = null;
		ret.reqNumber =req.ReqNumber;
		ret.itineraryType = TType.SHARED_TAXI;
		return ret;
	}
	
	private List<Leg> createTripLegsTS(JourneyRequest req, IPlannerService planner) {
		// Buffer containing all destinations to visit - contains fixed points
		// as well as points whose order will be determined in the loop below
		final List<Coordinate> buffer = new ArrayList<Coordinate>(6);
		buffer.add(taxiRank);
		buffer.add(req.From);
		
		for (Coordinate c : req.Destinations) {
			buffer.add(c);
		}
		buffer.add(taxiRank);
		
		// Creates the best itinerary based on a given optimization criteria
		// (currently lowest time affecting passengers - without time to return
		// to the taxi rank)
		PermutationIterator<Coordinate> it = new PermutationIterator<Coordinate>(req.Destinations);
		double minTime = Double.MAX_VALUE;
		List<Leg> ret = null;
		
		while (it.hasNext()) {
			List<Coordinate> c = it.next();
			
			for (int i = 0; i < c.size(); i++) {
				buffer.set(2 + i, c.get(i));
			}
			List<Leg> temp = createTripLegs(buffer, planner, req);
			
			if (temp == null)
				continue;
			
			double time = 0.0;
			for (int i = 0; i < temp.size() - 1; i++) {
				Leg l = temp.get(i);
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
			traces.add(new ArrayList<Street>(l.streets));
		}
		return new TaxiTrip(tripLegs.get(1).tripId, stops, stopTimes, traces);
	}	
}
