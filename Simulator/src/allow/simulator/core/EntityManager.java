package allow.simulator.core;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import allow.simulator.entity.Entity;
import allow.simulator.entity.EntityType;

public class EntityManager {
	// Collection of entities grouped by their respective types
	private final Map<EntityType, Map<Long, Entity>> entities;
	
	public EntityManager() {
		entities = new EnumMap<EntityType, Map<Long, Entity>>(EntityType.class);
	}
	
	public void addEntity(Entity entity) {
		Long2ObjectOpenHashMap<Entity> temp = (Long2ObjectOpenHashMap<Entity>) entities.get(entity.getType());
		
		if (temp == null) {
			temp = new Long2ObjectOpenHashMap<Entity>();
			entities.put(entity.getType(), temp);
		}
		
		temp.put(entity.getId(), entity);
	}
	
	public Collection<Entity> getEntitiesOfType(EntityType type) {
		Map<Long, Entity> temp = entities.get(type);
		return (temp != null) ? Collections.unmodifiableCollection(temp.values()) : Collections.emptySet();
	}
}
