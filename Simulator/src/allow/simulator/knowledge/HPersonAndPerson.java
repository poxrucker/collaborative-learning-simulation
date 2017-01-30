package allow.simulator.knowledge;

import allow.simulator.entity.Entity;
import allow.simulator.entity.Person;

public class HPersonAndPerson extends ExchangeHandler {

	@Override
	public boolean exchange(Entity entity1, Entity entity2) {

		if ((entity1 instanceof Person) && (entity2 instanceof Person)) {
			// return entity1.getKnowledge().exchangeKnowledge(entity2);
			Person p1 = (Person)entity1;
			Person p2 = (Person)entity2;
				
			if (!isValidProvider(p1))
				return false;
			
			if (!isValidReceiver(p2))
				return false;
			
			p2.setInformed(true);
			return true;
		}
		return (next != null) ? next.exchange(entity1, entity2) : false;
	}
	
	private static boolean isValidProvider(Person p) {
		return p.isInformed() && p.isSharing() && p.hasUsedCar();
	}
	
	private static boolean isValidReceiver(Person r) {
		return r.isReceiving() && r.hasCar();
	}
}
