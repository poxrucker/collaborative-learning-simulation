package allow.simulator.exchange;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import allow.simulator.closeness.BusProximityMeasure;
import allow.simulator.entity.Entity;

/**
 * Represents a relation of entities based on being in the same bus.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class BusRelation extends Relation {

	/**
	 * Creates new entity relation based on being in the same bus.
	 * 
	 * @param entity Entity
	 */
	public BusRelation(Entity entity) {
		super(entity);
		
	}

	@Override
	public void updateRelation(List<Entity> newEntities, Set<Long> blackList) {
		// Get current activity of entity.
		Collection<Entity> busEntities = BusProximityMeasure.Instance.getCloseEntities(entity, 0);
		
		// Add all entities which are new (i.e. in the difference of closeEntities and entities).
		for (Entity e : busEntities) {
					
			if (!entities.containsKey(e.getId()))
				newEntities.add(e);
		}
		// Finally, update entities in this relation.
		entities.clear();
		
		for (Entity e : busEntities) {
			entities.put(e.getId(), e);
		}
	}
}