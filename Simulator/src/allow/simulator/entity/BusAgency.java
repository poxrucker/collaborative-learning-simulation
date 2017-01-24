package allow.simulator.entity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import allow.simulator.core.Context;
import allow.simulator.flow.activity.transportagency.StartNextTrips;
import allow.simulator.mobility.data.PublicTransportationTrip;
import allow.simulator.mobility.data.Route;
import allow.simulator.mobility.data.Trip;

public final class BusAgency extends TransportationAgency {
	// Routes managed by this public transportation agency
	private final Map<String, Route> routes;
		
	// Buffer for next trips to schedule
	private final List<PublicTransportationTrip> nextTrips;
	
	// "Live" information about current trips and vehicles executing trips
	protected final Map<String, TransportationEntity> currentlyUsedVehicles;
	
	public BusAgency(long id, Context context, String agencyId) {
		super(id, context, agencyId);
		routes = new HashMap<String, Route>();
		nextTrips = new LinkedList<PublicTransportationTrip>();
		
		// Start scheduling next trips.
		flow.addActivity(new StartNextTrips(this));
		currentlyUsedVehicles = new HashMap<String, TransportationEntity>();
	}

	/**
	 * Adds a new route to the agency.
	 * 
	 * @param newRoute New route to be served by the agency.
	 */
	public void addRoute(Route newRoute) {
		routes.put(newRoute.getRouteId(), newRoute);
	}
	
	/**
	 * Adds a new public transportation entity to the agency.
	 * 
	 * @param transportation Public transportation entity to be added to the agency
	 */
	public void addPublicTransportation(Bus transportation) {
		vehicles.add(transportation);
	}
	
	/**
	 * Returns the route having the specified Id.
	 * 
	 * @param routeId Id of the route.
	 * @return Route having the Id routeId or null of there is no such route.
	 */
	public Route getRoute(String routeId) {
		return routes.get(routeId);
	}
	
	public List<PublicTransportationTrip> getTripsToSchedule(LocalDateTime currentTime) {
		// Clear list of next trips.
		nextTrips.clear();

		// Create list of next trips to start.
		for (Route route : routes.values()) {
			// Get next trip for every route.
			List<PublicTransportationTrip> t = route.getNextTrip(currentTime);
			
			// If trip is not null (i.e. route has a trip starting at current time), add it to the list.
			if (t != null) {
				nextTrips.addAll(t);
			}
		}
		return nextTrips;
	}
	
	public Bus scheduleTrip(Trip trip) {
		// Poll next free transportation entity
		Bus transportationEntity = (Bus) vehicles.poll();
		
		if (transportationEntity == null)
			throw new IllegalStateException("Error: No vehicle left to schedule trip " + trip.getTripId());
		currentlyUsedVehicles.put(trip.getTripId(), transportationEntity);
		return transportationEntity;
	}
	
	public void finishTrip(Trip trip, TransportationEntity vehicle) {
		currentlyUsedVehicles.remove(trip.getTripId());
		vehicles.add(vehicle);
	}
	
	public Bus getVehicleOfTrip(String tripId) {
		return (Bus) currentlyUsedVehicles.get(tripId);
	}
	
	@Override
	public String toString() {
		return "[PublicTransportationAgency" + id + "]";
	}

	@Override
	public String getType() {
		return EntityTypes.PUBLIC_TRANSPORT_AGENCY;
	}
	
}
