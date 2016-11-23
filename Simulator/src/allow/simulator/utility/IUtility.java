package allow.simulator.utility;


/**
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 * @param <V>
 */
public interface IUtility<V, E extends Weights> {

	/**
	 * Computes a utility value from a given generic input.
	 * 
	 * @param input Input instance
	 * @param weights Weights to use for utility calculation
	 * @return Utility value computed from the given input instance
	 */
	double computeUtility(V input, E weights);
	
	/**
	 * Determines whether higher or lower values express higher utility.
	 * 
	 * @return True, if higher values express higher utility, false otherwise 
	 */
	boolean ascendingOrder();
	
}
