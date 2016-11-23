package allow.simulator.knowledge;

public interface ITrainable<V, E> {
	
	E learn(V input);
	
}
