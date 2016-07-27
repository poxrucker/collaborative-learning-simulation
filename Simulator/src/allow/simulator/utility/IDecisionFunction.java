package allow.simulator.utility;

public interface IDecisionFunction<V, E> {

	E reason(V arguments);
	
}
