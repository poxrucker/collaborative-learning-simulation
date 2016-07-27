package allow.simulator.core;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import allow.simulator.entity.Entity;

public class EntityManager {
	// Id enumerator
	private final AtomicLong id;
	
	// Collection of entities grouped by their respective types
	private final Map<String, Map<Long, Entity>> entities;
	
	public EntityManager() {
		entities = new HashMap<String, Map<Long, Entity>>();
		id = new AtomicLong(0);
	}
	
	public long getNextId() {
		return id.getAndIncrement();
	}
	
	public void addEntity(Entity entity) {
		Long2ObjectOpenHashMap<Entity> temp = (Long2ObjectOpenHashMap<Entity>) entities.get(entity.getType());
		
		if (temp == null) {
			temp = new Long2ObjectOpenHashMap<Entity>();
			entities.put(entity.getType(), temp);
		}
		
		if (temp.containsKey(entity.getId()))
			throw new IllegalArgumentException("Error: Entity with id " + entity.getId() + " already exists.");
		
		temp.put(entity.getId(), entity);
		id.set(Math.max(entity.getId() + 1, id.get()));
	}
	
	public Collection<String> getEntityTypes() {
		return Collections.unmodifiableCollection(entities.keySet());
	}
	
	public Collection<Entity> getEntitiesOfType(String type) {
		Map<Long, Entity> temp = entities.get(type);
		return (temp != null) ? Collections.unmodifiableCollection(temp.values()) : Collections.emptySet();
	}
}
