package allow.simulator.entity.knowledge;

public interface ITrainable<V, E> {
	
	E train(V input);
	
}
