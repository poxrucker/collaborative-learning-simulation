package allow.simulator.knowledge;

public interface IPredictor<V, E> {
	
	E predict(V input);
	
}
