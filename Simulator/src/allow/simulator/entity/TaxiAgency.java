package allow.simulator.entity;

import java.util.HashMap;
import java.util.Map;

import allow.simulator.core.Context;
import allow.simulator.flow.activity.taxi.PrepareTaxiTrip;
import allow.simulator.mobility.data.TaxiStop;
import allow.simulator.mobility.data.TaxiTrip;
import allow.simulator.mobility.planner.TaxiPlanner;
import allow.simulator.util.Coordinate;

public final class TaxiAgency extends TransportationAgency {
	// Collection of trips which have been requested and are being executed
	private final Map<String, TaxiTrip> currentTrips;
	
	// "Live" information about current trips and vehicles executing trips
	private final Map<String, Taxi> currentlyUsedVehicles;
	
	// Taxi stop mapping for active taxi trips
	private final Map<String, TaxiStop> taxiStops;
	
	public TaxiAgency(long id, Context context, String agencyId) {
		super(id, EntityTypes.TAXI_AGENCY, context, agencyId);
		position = new Coordinate(11.119714, 46.071988);
		currentTrips = new HashMap<String, TaxiTrip>();
		currentlyUsedVehicles = new HashMap<String, Taxi>();
		taxiStops = new HashMap<String, TaxiStop>();
	}

	@Override
	public boolean isActive() {
		return false;
	}

	public Taxi scheduleTrip(TaxiTrip taxiTrip) {
		// Poll next free transportation entity
		Taxi taxi = (Taxi) vehicles.poll();
		
		if (taxi == null)
			throw new IllegalStateException("Error: No taxi left to schedule trip " + taxiTrip.getTripId());
		currentlyUsedVehicles.put(taxiTrip.getTripId(), taxi);
		currentTrips.put(taxiTrip.getTripId(), taxiTrip);

		for (TaxiStop stop : taxiTrip.getTaxiStops()) {
			taxiStops.put(stop.getStopId(), stop);
		}
		return taxi;
	}
	
	public void finishTrip(String tripId, Taxi taxi) {
		currentlyUsedVehicles.remove(tripId);
		TaxiTrip trip = currentTrips.remove(tripId);
		
		for (TaxiStop stop : trip.getTaxiStops()) {
			taxiStops.remove(stop.getStopId());
		}
		vehicles.add(taxi);
	}
	
	public Taxi call(String tripId) {
		// If trip has already been scheduled (shared taxi) return assigend taxi instance
		if (currentTrips.containsKey(tripId))
			return currentlyUsedVehicles.get(tripId);
		
		TaxiPlanner service = context.getJourneyPlanner().getTaxiPlannerService();
		TaxiTrip trip = service.getTaxiTrip(tripId);	
		Taxi t = scheduleTrip(trip);
		t.getFlow().addActivity(new PrepareTaxiTrip(t, trip));	
		return t;
	}
	
	public TaxiStop getTaxiStop(String stopId) {
		return taxiStops.get(stopId);
	}
	
	public void cancel(String tripId) {
		TaxiPlanner service = context.getJourneyPlanner().getTaxiPlannerService();
		service.getTaxiTrip(tripId);
	}
	
	public TaxiTrip getTripInformation(String tripId) {
		return currentTrips.get(tripId);
	}
	
	/**
	 * Adds a taxi entity to the agency
	 * 
	 * @param transportation Taxi entity to be added to the agency
	 */
	public void addTaxi(Taxi transportation) {
		vehicles.add(transportation);
	}
	
	@Override
	public String toString() {
		return "[Taxi" + id + "]";
	}

	
}
