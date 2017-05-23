package allow.simulator.exchange;

import allow.simulator.entity.Entity;
import allow.simulator.entity.Person;

public class HPersonAndPerson extends ExchangeHandler {

	@Override
	public boolean exchange(Entity entity1, Entity entity2) {

		if ((entity1 instanceof Person) && (entity2 instanceof Person)) {
			// return entity1.getKnowledge().exchangeKnowledge(entity2);
			Person p1 = (Person)entity1;
			Person p2 = (Person)entity2;
			boolean ret1 = exchange(p1, p2);
			boolean ret2 = exchange(p2, p1);
			return ret1 || ret2;
		}
		return (next != null) ? next.exchange(entity1, entity2) : false;
	}
	
	private static boolean exchange(Person provider, Person receiver) {
		if (!isValidProvider(provider))
			return false;
		
		if (!isValidReceiver(receiver))
			return false;
		
		receiver.setInformed(true);
		return true;
	}
	
	private static boolean isValidProvider(Person p) {
		return p.isInformed() && p.isSharing() && executesRelevantActivity(p);
	}
	
	private static boolean isValidReceiver(Person r) {
		return r.isReceiving() && executesRelevantActivity(r);
	}
	
	private static boolean executesRelevantActivity(Person p) {
		return false;
	}
}
