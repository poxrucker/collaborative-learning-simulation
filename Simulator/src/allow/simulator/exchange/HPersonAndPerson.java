package allow.simulator.exchange;

import allow.simulator.entity.Entity;
import allow.simulator.entity.Person;

public class HPersonAndPerson extends ExchangeHandler {

	// private static final double PREFERENCE_CHANGE_STEP_NARROW = 0.005;
	// private static final double PREFERENCE_CHANGE_STEP_WIDE = 0.0025;

	@Override
	public boolean exchange(Entity entity1, Entity entity2) {

		if ((entity1 instanceof Person) && (entity2 instanceof Person)) {
			return entity1.getKnowledge().exchangeKnowledge(entity2);

			/*Preferences p1 = ((Person) entity1).getPreferences();
			Preferences p2 = ((Person) entity2).getPreferences();

			if (p1.getNCarPreferenceChanges() > p1.getNBusPreferenceChanges()) {
				if (p1.getNCarPreferenceChanges() > 0) {
					int diffCarChanges = Math.max(p2.getNCarPreferenceChanges() - p1.getNCarPreferenceChanges(), 0);

					if (diffCarChanges >= 2) {
						p1.adjustCarPreference(p1.getCarPreference()
								- PREFERENCE_CHANGE_STEP_WIDE * diffCarChanges);

					} else if (diffCarChanges > 0) {
						p1.adjustCarPreference(p1.getCarPreference()
								- PREFERENCE_CHANGE_STEP_NARROW
								* ((diffCarChanges > 0) ? diffCarChanges : 1));

					}
				}
			} else {
				if (p1.getNBusPreferenceChanges() > 0) {
					int diffBusChanges = Math.max(p2.getNBusPreferenceChanges() - p1.getNBusPreferenceChanges(), 0);

					if (diffBusChanges > 2) {
						p1.adjustBusPreference(p1.getBusPreference() - PREFERENCE_CHANGE_STEP_WIDE * diffBusChanges);

					} else if (diffBusChanges > 0) {
						p1.adjustBusPreference(p1.getBusPreference()
								- PREFERENCE_CHANGE_STEP_NARROW
								* ((diffBusChanges > 0) ? diffBusChanges : 1));

					}
				}
			}*/
		}
		return (next != null) ? next.exchange(entity1, entity2) : false;
	}
}
