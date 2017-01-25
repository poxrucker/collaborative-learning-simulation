package allow.simulator.relation;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.List;
import java.util.Set;

import allow.simulator.entity.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Represents a relation among entities.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public abstract class Relation {
	
	public enum Type {
		/**
		 * Entities are physically close to each other.
		 */
		DISTANCE,
	
		/**
		 * Entities are on the same bus.
		 */
		BUS
	}
	
	// Type of this relation.
	protected Type type;
	
	// Entity.
	@JsonBackReference
	protected Entity entity;
		
	// Entities in this relation.
	protected Long2ObjectOpenHashMap<Entity> entities;
	
	protected Relation(Entity entity) {
		this.entity = entity;
		entities = new Long2ObjectOpenHashMap<Entity>();
	}
	
	public abstract void updateRelation(List<Entity> newEntities, Set<Long> blackList);
	
	public void reset() {
		entities.clear();
	}
	
}
