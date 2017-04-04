package allow.simulator.mobility.planner;

import java.util.List;

/**
 * Interface of a journey planner service providing a set of possible travel
 * plans for a given request.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public interface IPlannerService {
	
	/**
	 * Requests a single journey from the planner specifying the parameters in
	 * the journey request. The returned itineraries are added to the itineraries
	 * parameter directly. The function returns a reference to the itineraries
	 * parameter in case querying was successful, or null if there was an error
	 * during planning. 
	 * 
	 * @param request Request containing the parameters of the journey.
	 * @param itineraries List to add returned journeys to.
	 * @return Reference to itineraries parameter if planning was successful,
	 * null otherwise.
	 */
	public boolean requestJourney(JourneyRequest request, List<Itinerary> itineraries);
	
}
