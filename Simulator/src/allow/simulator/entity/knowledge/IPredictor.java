package allow.simulator.entity.knowledge;

public interface IPredictor<V, E> {
	
	E predict(V input);
	
}
