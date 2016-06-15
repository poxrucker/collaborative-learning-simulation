package allow.simulator.adaptation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import allow.simulator.entity.Entity;

public final class Ensemble {
	// Entity which created the ensemble (responsible for terminating it afterwards)
	private Entity creator;
	
	// Entities participating in the ensemble including the creator entity
	private Map<Long, Entity> participants;
	
	/**
	 * Creates a new instance of an ensemble structure with the specified
	 * entity set as creator which is responsible to terminate the ensemble
	 * in the end.
	 * 
	 * @param creator Entity which creates the ensemble
	 */
	Ensemble(Entity creator) {
		this.creator = creator;
		participants = new HashMap<Long, Entity>();
	}
	
	/**
	 * Adds a new entity to the ensemble.
	 * 
	 * @param entity Entity to add to the ensemble
	 */
	public void addEntity(Entity entity) {
		participants.put(entity.getId(), entity);
	}
	
	/**
	 * Removes the given entity from the ensemble. If the entity is not part
	 * of the ensemble, calling this method has no effect.
	 * 
	 * @param entity Entity to remove from the ensemble
	 * @return True if entity was removed, false if entity is not part of the
	 * ensemble
	 */
	public boolean removeEntity(Entity entity) {
		return (participants.remove(entity.getId()) != null);
	}
	
	/**
	 * Returns the entity which has created the ensemble.
	 * 
	 * @return Entity which has created the ensemble
	 */
	public Entity getCreator() {
		return creator;
	}
	
	/**
	 * Returns the set of participants of the ensemble including the creator.
	 * 
	 * @return Set of participants of the ensemble including the creator
	 */
	public Collection<Entity> getEntities() {
		return Collections.unmodifiableCollection(participants.values());
	}
	
}
