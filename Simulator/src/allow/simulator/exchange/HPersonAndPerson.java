package allow.simulator.exchange;

import java.util.concurrent.ExecutionException;

import allow.simulator.entity.Entity;
import allow.simulator.entity.Person;

public final class HPersonAndPerson extends ExchangeHandler {

	@Override
	public boolean exchange(Entity entity1, Entity entity2) {

		if ((entity1 instanceof Person) && (entity2 instanceof Person)) {
			try {
				return entity1.getKnowledge().exchangeKnowledge(entity2).get();
				
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		return (next != null) ? next.exchange(entity1, entity2) : false;
	}
}
