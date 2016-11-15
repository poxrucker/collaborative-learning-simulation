package allow.simulator.utility;

/**
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 * @param <V>
 * @param <E>
 */
public interface IDecisionFunction<V, E> {

	/**
	 * 
	 * @param input
	 * @return
	 */
	E reason(V input);
	
}
