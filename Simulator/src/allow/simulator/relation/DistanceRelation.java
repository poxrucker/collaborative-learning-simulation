package allow.simulator.relation;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import allow.simulator.closeness.SpatialProximityMeasure;
import allow.simulator.entity.Entity;

/**
 * Represents a relation of entities based on physical distance.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public class DistanceRelation extends Relation {

	/**
	 * Distance threshold
	 */
	public static final double DISTANCE = 50.0;
		
	/**
	 * Constructor.
	 * Creates new entity relation based on physical distance.
	 * 
	 * @param entity Entity.
	 */
	public DistanceRelation(Entity entity) {
		super(Relation.Type.DISTANCE, entity);
	}

	/**
	 * Updates this relation adding entities which are new to newEntities list.
	 * newEntities is modified when calling this method.
	 * 
	 * @param newEntities List to add entities which are new in this relation.
	 */
	@Override
	public void updateRelation(List<Entity> newEntities, Set<Long> blackList) {
		// Get entities which are physically close
		Collection<Entity> closeEntities = SpatialProximityMeasure.Instance.getCloseEntities(entity, DISTANCE);

		// Add all entities which are new (i.e. in the difference of closeEntities and entities)
		for (Entity e : closeEntities) {
			
			if (!entities.containsKey(e.getId()) && !blackList.contains(e.getId())) {
				newEntities.add(e);
			}
		}
		// Finally, update entities in this relation
		entities.clear();
		
		for (Entity e : closeEntities) {
			entities.put(e.getId(), e);
		}
		return;
	}
}
