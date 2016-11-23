package allow.simulator.mobility.planner;

/**
 * Route optimization criteria for OpenTripPlanner
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public enum RType {
	/**
	 * Fastest route
	 */
	QUICK,
	
	/**
	 * Most environmental friendly route
	 */
	GREENWAYS,
	
	/**
	 * Most safe route
	 */
	SAFE
}
