package allow.simulator.mobility.planner;

/**
 * Means of transportation supported in the simulation
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public enum TType {
	
	/**
	 * Walking
	 */
	WALK,
	
	/**
	 * Driving
	 */
	CAR,
	
	/**
	 * Going by bus
	 */
	BUS,
	
	/**
	 * Going by train
	 */
	RAIL,
	
	/**
	 * Going by cable car
	 */
	CABLE_CAR,
	
	/**
	 * Going by bike
	 */
	BICYCLE,
	
	/**
	 * Going by shared bike
	 */
	SHARED_BICYCLE,
	
	/**
	 * Using general public transport
	 */
	TRANSIT,
	
	/**
	 * Using a normal taxi
	 */
	TAXI,
	
	/**
	 * Sharing a taxi 
	 */
	SHARED_TAXI
}
