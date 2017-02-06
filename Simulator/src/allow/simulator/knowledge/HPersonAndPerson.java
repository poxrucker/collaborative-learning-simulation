package allow.simulator.knowledge;

import allow.simulator.core.SimulationParameter;
import allow.simulator.entity.Entity;
import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;

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
	
	private boolean isValidProvider(Person p) {
		return p.isInformed() && p.isSharing() && usesParticipatingModality(p);
	}
	
	private boolean isValidReceiver(Person r) {
		return r.isReceiving() && usesParticipatingModality(r);
	}
	
	private boolean usesParticipatingModality(Person p) {
		SimulationParameter param = p.getContext().getSimulationParameters();
		Activity a = p.getFlow().getCurrentActivity();
		
		if (a == null)
			return false;
		
		return (param.Car && (a.getType() == ActivityType.DRIVE))
				|| (param.Bus && (a.getType() == ActivityType.USE_PUBLIC_TRANSPORT))
				|| (param.Walk && (a.getType() == ActivityType.WALK))
				|| (param.Bike && (a.getType() == ActivityType.WALK));
	}
}
