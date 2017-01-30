package allow.simulator.knowledge;

import allow.simulator.entity.Entity;
import allow.simulator.entity.Person;
import allow.simulator.entity.Bus;

/**
 * 
 * 
 * @author Andi
 *
 */
public class HPersonAndBus extends ExchangeHandler {

	@Override
	public boolean exchange(Entity entity1, Entity entity2) {
		
		if ((entity1 instanceof Person) && (entity2 instanceof Bus)) {
			return false;
		}
		return (next != null) ? next.exchange(entity1, entity2) : false;
	}
}
