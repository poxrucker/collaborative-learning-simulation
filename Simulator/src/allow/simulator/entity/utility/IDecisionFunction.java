package allow.simulator.entity.utility;

public interface IDecisionFunction<V, E> {

	E reason(V arguments);
	
}
