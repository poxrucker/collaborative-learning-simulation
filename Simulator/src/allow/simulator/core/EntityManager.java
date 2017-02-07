package allow.simulator.core;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import allow.simulator.entity.Entity;

public class EntityManager {
	// Id enumerator
	private final AtomicInteger id;
	
	// Collection of entities grouped by their respective types
	private final Map<String, Map<Integer, Entity>> entities;
	
	public EntityManager() {
		entities = new HashMap<String, Map<Integer, Entity>>();
		id = new AtomicInteger(0);
	}
	
	public int getNextId() {
		return id.getAndIncrement();
	}
	
	public void addEntity(Entity entity) {
		Int2ObjectOpenHashMap<Entity> temp = (Int2ObjectOpenHashMap<Entity>) entities.get(entity.getType());
		
		if (temp == null) {
			temp = new Int2ObjectOpenHashMap<Entity>();
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
		Map<Integer, Entity> temp = entities.get(type);
		return (temp != null) ? Collections.unmodifiableCollection(temp.values()) : Collections.emptySet();
	}
}
