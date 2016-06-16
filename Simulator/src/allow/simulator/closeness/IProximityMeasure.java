package allow.simulator.closeness;

import java.util.Collection;

import allow.simulator.entity.Entity;

public interface IProximityMeasure {

	/**
	 * Returns all entities which are close to the given entity with respect to
	 * the implemented distance measure.
	 * 
	 * @param entity Entity for which nearby entities should be determined
	 * @param maxDistance Maximum distance between given entity and potentially
	 * close entities
	 * @return Collection of entities which are close to the given entity with
	 * respect to the implemented proximity measure
	 */
	Collection<Entity> getCloseEntities(Entity entity, double maxDistance);
	
}
