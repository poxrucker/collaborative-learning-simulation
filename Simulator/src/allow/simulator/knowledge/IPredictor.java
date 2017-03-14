package allow.simulator.knowledge;

import allow.simulator.core.Context;

public interface IPredictor<V, E> {
	
	E predict(V input, Context context);
	
}
