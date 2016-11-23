package allow.simulator.entity.knowledge;

public interface IExchangeStrategy<V extends Knowledge> {

	boolean exchangeKnowledge(V k1, V k2);
	
}
