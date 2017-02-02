package allow.simulator.mobility.data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Route {
	// Id of this route.
	private String routeId;
	
	// Stops of this route.
	private Map<String, Stop> stops;
	
	// Trips of this route ordered chronological by day.
	private List<List<PublicTransportationTrip>> trips;
	private Map<String, PublicTransportationTrip> tripInfo;

	// Buffer to return.
	private List<PublicTransportationTrip> tripsToReturn;
	
	/**
	 * Constructor.
	 * Creates a new route with given Id and time table.
	 * 
	 * @param routeId Id of this route.
	 * @param timeTable Time table of this route.
	 */
	public Route(String routeId, List<List<PublicTransportationTrip>> trips, Map<String, PublicTransportationTrip> tripInfo,
			Map<String, Stop> stops) {
		this.routeId = routeId;
		this.stops = stops;
		this.trips = trips;
		this.tripInfo = tripInfo;
		tripsToReturn = new ArrayList<PublicTransportationTrip>(16);
	}
	
	public String getRouteId() {
		return routeId;
	}
	
	public List<PublicTransportationTrip> getNextTrip(LocalDateTime currentTime) {
		LinkedList<PublicTransportationTrip> dayTrips = null;
		tripsToReturn.clear();
		
		switch (currentTime.getDayOfWeek()) {
			case MONDAY:
				dayTrips = (LinkedList<PublicTransportationTrip>) trips.get(0);
				break;
			case TUESDAY:
				dayTrips = (LinkedList<PublicTransportationTrip>) trips.get(1);
				break;
			case WEDNESDAY:
				dayTrips = (LinkedList<PublicTransportationTrip>) trips.get(2);
				break;
			case THURSDAY:
				dayTrips = (LinkedList<PublicTransportationTrip>) trips.get(3);
				break;
			case FRIDAY:
				dayTrips = (LinkedList<PublicTransportationTrip>) trips.get(4);
				break;
			case SATURDAY:
				dayTrips = (LinkedList<PublicTransportationTrip>) trips.get(5);
				break;
			case SUNDAY:
				dayTrips = (LinkedList<PublicTransportationTrip>) trips.get(6);
				break;
		}
		
		if (dayTrips.size() == 0) {
			return tripsToReturn;
		}

		// Get starting time of next trip.
		PublicTransportationTrip nextTrip = dayTrips.peekFirst();
		int c = 0;
		while ((nextTrip != null) && (c < dayTrips.size()) && (nextTrip.getStartingTime().getHour() == currentTime.getHour()) 
				&& (nextTrip.getStartingTime().getMinute() == currentTime.getMinute())) {
			
			if (nextTrip.isValidThisDay(currentTime.toLocalDate())) {
				tripsToReturn.add(nextTrip);
			}
			dayTrips.pollFirst();
			nextTrip = dayTrips.peekFirst();
			dayTrips.addLast(nextTrip);
			c++;
		}
		return tripsToReturn;
	}
	
	public Stop getStop(String stopId) {
		return stops.get(stopId);
	}
	
	public Trip getTripInformation(String tripId) {
		return tripInfo.get(tripId);
	}
}
