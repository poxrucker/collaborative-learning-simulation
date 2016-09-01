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
	// Costs per meter for using a taxi
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
		ret.transitTime = ret.duration;
		ret.itineraryType = TType.TAXI;
		return ret;
	}
	
	private Itinerary createSharedTaxiItinerary(JourneyRequest req) {
		// Get planner instance
		int randomPlannerId = ThreadLocalRandom.current().nextInt(plannerServices.size());
		IPlannerService planner = plannerServices.get(randomPlannerId);	
		
		// Plan walking legs form starting points to pickup point
		final List<Leg> walkingLegs = createWalkingLegs(req, planner);
		
		if (walkingLegs == null)
			return null;
		
		// Plan taxi legs from pickup point to destinations
		final List<Leg> taxilegs = createTripLegsTS(req, planner);

		if (taxilegs == null)
			return null;

		// Determine earliest starting time for taxi trip as maximum timestamp
		// of earliest departure of taxi at pickup point and latest arrival
		// time of passangers at pickup point
		long earliestStartingTime = taxilegs.get(0).endTime;
		
		// Check if one of walking legs arrives later and reset to latest time
		if (walkingLegs.size() > 0) {
			
			for (Leg l : walkingLegs) {

				if (l.endTime > earliestStartingTime)
					earliestStartingTime = l.endTime;
			}
			// Add an additional 60 seconds to starting time
			earliestStartingTime += 60 * 1000;
			updateTaxiLegs(taxilegs, earliestStartingTime - taxilegs.get(0).endTime);
		}
		
		// Create TaxiTrip using the determined offset and add it to buffer
		TaxiTrip taxiTrip = createTaxiTrip(taxilegs);
		tripBuffer.put(taxiTrip.getTripId(), taxiTrip);

		// Summarize costs
		final List<Leg> tripLegs = taxilegs.subList(1, taxilegs.size() - 1);
		final double totalCosts = calculateCosts(tripLegs);
		
		// Create return itinerary
		Itinerary ret = new Itinerary();
		List<Itinerary> subItineraries = new ArrayList<Itinerary>(req.Destinations.size());	
		String stopIdFrom = taxilegs.get(0).stopIdTo;
		long durationAcc = 0;
		double distAcc = 0.0;
		final Coordinate startingPos = taxilegs.get(0).to;
		
		for (Leg temp : tripLegs) {		
			// Update accumulated values
			durationAcc += ((temp.endTime - temp.startTime) / 1000);
			distAcc += temp.distance;

			for (int j = 0; j < req.Destinations.size(); j++) {
				// Check if itinerary needs to be created involving the current leg
				final Coordinate currentDest = req.Destinations.get(j);
				
				if (!currentDest.equals(temp.to))
					continue;
				
				// Get walking leg
				final Coordinate currentStart = req.StartingPoints.get(j);
				Leg walkingLeg = null;

				for (Leg l : walkingLegs) {
					
					if (l.to.equals(currentStart))
						walkingLeg = l;
				}
				Itinerary subIt = new Itinerary();

				// Create walking leg if necessary
				if (walkingLeg != null) {
					subIt.legs.add(new Leg(walkingLeg));
				}				
				// Create new taxi leg and update with accumulated values
				Leg leg = new Leg(temp);
				leg.costs = totalCosts / req.Destinations.size();
				leg.distance = distAcc;
				leg.startTime = earliestStartingTime;
				leg.from = startingPos;
				leg.mode = TType.TAXI;
				leg.stopIdFrom = stopIdFrom;
				subIt.legs.add(leg);

				// Now create itinerary 
				Leg first = subIt.legs.get(0);
				Leg second = subIt.legs.get(subIt.legs.size() - 1);
				subIt.from = first.from;
				subIt.to = second.to;
				subIt.startTime = first.startTime;
				subIt.endTime = second.endTime;
				
				if (first != second) {
					subIt.initialWaitingTime = 0;
					subIt.waitingTime = second.startTime - first.endTime;
					subIt.walkDistance = first.distance;
					subIt.walkTime = (first.startTime - first.endTime) / 1000;
					
				} else {
					subIt.initialWaitingTime = (taxilegs.get(0).endTime - taxilegs.get(0).startTime) / 1000;
					subIt.waitingTime = 0;
				}
				subIt.transitTime = durationAcc;
				subIt.duration = subIt.transitTime + subIt.waitingTime;
				subIt.transfers = 1;
				subIt.reqId = req.ReqId;
				subIt.reqNumber = req.ReqNumber;
				subIt.itineraryType = TType.TAXI;
				subIt.costs = leg.costs;
				subItineraries.add(subIt);
			}
		}
		//ret.initialWaitingTime = initialWaitingTime;
		//ret.subItineraries = subItineraries;
		ret.costs = totalCosts;
		ret.duration = durationAcc;
		ret.startTime = subItineraries.get(0).startTime;
		ret.endTime = subItineraries.get(subItineraries.size() - 1).endTime;
		ret.from = subItineraries.get(0).from;
		ret.to = subItineraries.get(subItineraries.size() - 1).to;
		ret.transitTime = ret.duration;
		ret.reqId = req.ReqId;
		ret.legs = null;
		ret.reqNumber =req.ReqNumber;
		ret.itineraryType = TType.SHARED_TAXI;
		ret.subItineraries = reorderSubItineraries(req.StartingPoints, req.Destinations, subItineraries);
		return ret;
	}
	
	private List<Itinerary> reorderSubItineraries(List<Coordinate> start, List<Coordinate> dest, List<Itinerary> subIt) {
		List<Integer> indices = new ArrayList<Integer>();
		
		for (Itinerary it : subIt) {
			boolean added = false;
			
			for (int i = 0; i < start.size(); i++) {
				final Coordinate s = start.get(i);
				final Coordinate d = dest.get(i);
				
				if (!indices.contains(i) && it.from.equals(s) && it.to.equals(d)) {
					indices.add(i);
					added = true;
					break;
				}
			}
			
			if (!added)
				throw new IllegalStateException("Could not map itinerary.");
		}
		List<Itinerary> temp = new ArrayList<Itinerary>(subIt.size());

		for (int i = 0; i < subIt.size(); i++) {
			temp.add(null);
		}
		
		for (int i = 0; i < subIt.size(); i++) {
			temp.set(indices.get(i), subIt.get(i));
		}
		return temp;
	}
	
	private void updateTaxiLegs(List<Leg> taxiLegs, long offset) {
		
		for (int i = 0; i < taxiLegs.size(); i++) {
			final Leg leg = taxiLegs.get(i);
			leg.startTime += offset;
			leg.endTime += offset;
		}
		
	}
	
	private double calculateCosts(List<Leg> legs) {
		double totalCosts = 0.0;
		
		for (Leg l : legs) {
			totalCosts += COST_PER_METER * l.distance;
		}
		return totalCosts;
	}
	
	private List<Leg> createWalkingLegs(JourneyRequest req, IPlannerService planner) {
		// Get unique destinations from req
		List<Coordinate> uniqueSPoint = new ArrayList<Coordinate>(req.StartingPoints.size());

		for (Coordinate c : req.StartingPoints) {

			if (uniqueSPoint.contains(c))
				continue;
			uniqueSPoint.add(c);
		}
		List<Leg> ret = new ArrayList<Leg>(uniqueSPoint.size());
		
		for (Coordinate c : uniqueSPoint) {
			
			if (c.equals(req.From))
				continue;
			
			Leg leg = createLeg(c, req.From, TType.WALK, planner, req);
			
			if (leg == null)
				return null;
			ret.add(leg);
		}
		return ret;
	}
	
	private List<Leg> createTripLegsTS(JourneyRequest req, IPlannerService planner) {
		// Get unique destinations from req
		List<Coordinate> uniqueDestinations = new ArrayList<Coordinate>(req.Destinations.size());
		
		for (Coordinate c : req.Destinations) {
			
			if (uniqueDestinations.contains(c))
				continue;
			uniqueDestinations.add(c);
		}
		// Buffer containing all destinations to visit - contains fixed points
		// as well as points whose order will be determined in the loop below
		final List<Coordinate> buffer = new ArrayList<Coordinate>(6);
		buffer.add(taxiRank);
		buffer.add(req.From);
		
		for (Coordinate c : uniqueDestinations) {		
			buffer.add(c);
		}
		buffer.add(taxiRank);
		
		// Creates the best itinerary based on a given optimization criteria
		// (currently lowest time affecting passengers - without time to return
		// to the taxi rank)
		PermutationIterator<Coordinate> it = new PermutationIterator<Coordinate>(uniqueDestinations);
		double minTime = Double.MAX_VALUE;
		List<Leg> legs = null;
		
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
				legs = temp;
			}
		}
		return legs;
	}
	
	private List<Leg> createTripLegs(List<Coordinate> locations, IPlannerService planner, JourneyRequest req) {
		if (locations.size() == 0)
			return null;
		
		List<Leg> ret = new ArrayList<Leg>(locations.size() - 1);
		
		for (int i = 0; i < locations.size() - 1; i++) {
			Leg l = createLeg(locations.get(i), locations.get(i + 1), TType.CAR, planner, req);
			
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
	
	private Leg createLeg(Coordinate from, Coordinate to, TType type, IPlannerService planner, JourneyRequest req2) {
		RequestId reqId = new RequestId();
		LocalTime t = req2.ArrivalTime != null ? req2.ArrivalTime : req2.DepartureTime;
		JourneyRequest req = JourneyRequest.createRequest(from, to, LocalDateTime.of(req2.Date, t), false, new TType[] { TType.CAR }, reqId);
		List<Itinerary> temp = new ArrayList<Itinerary>();
		planner.requestSingleJourney(req, temp);
		Itinerary candidateIt = null;
		
		for (Itinerary it : temp) {
			
			if ((it.itineraryType == type) && (it.legs.size() == 1)) {
				candidateIt = it;
				break;
			}
		}
				
		if (candidateIt == null)
			return null;
		Leg l = candidateIt.legs.get(0);
		l.from = from;
		l.to = to;
		return l;
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
