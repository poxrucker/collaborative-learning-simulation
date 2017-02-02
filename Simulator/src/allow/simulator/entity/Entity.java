package allow.simulator.entity;

import java.util.Observable;

import allow.simulator.core.Context;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.Flow;
import allow.simulator.knowledge.EvoKnowledge;
import allow.simulator.relation.RelationGraph;
import allow.simulator.util.Coordinate;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Abstract class representing an entity which can be used in the simulation.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public abstract class Entity extends Observable {
	// Id of the entity
	protected final long id;
		
	// Simulation context
	@JsonIgnore 
	protected Context context;
	
	// Knowledge of the entity
	@JsonIgnore
	protected final EvoKnowledge knowledge;
		
	// Relations of the entity
	@JsonIgnore
	protected final RelationGraph relations;
	
	// Flow of activities to execute
	@JsonIgnore
	protected final Flow flow;
	
	// Position of an entity
	@JsonIgnore
	protected Coordinate position;
	
	/**
	 * Creates a new entity with in a given simulation context. Knowledge and
	 * relations are newly initialized.
	 * 
	 * @param id Id used for identification.
	 * @param type Type of the entity.
	 * @param utility Utility function for decision making.
	 * @param prefs Preferences required for utility function.
	 * @param context Simulation context the entity is used in.
	 */
	public Entity(long id, Context context) {
		this.id = id;
		position = new Coordinate(-1, -1);
		knowledge = new EvoKnowledge(this);
		relations = new RelationGraph(this);
		this.context = context;
		flow = new Flow();
		setPosition(position);
	}

	/**
	 * Creates a new entity without a given simulation context. To use entity
	 * in simulation, context must be set with setContext(). Knowledge and 
	 * relations are newly initialized.
	 * 
	 * @param id Id used for identification.
	 * @param type Type of the entity.
	 * @param utility Utility function for decision making.
	 * @param prefs Preferences required for utility function.
	 */
	protected Entity(long id) {
		this(id, null);
	}
	
	/**
	 * Returns Id of the entity.
	 * 
	 * @return Id of entity.
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Returns type of the entity.
	 * 
	 * @return Type of the entity.
	 */
	public abstract String getType();
	
	/**
	 * Get current position of entity.
	 * 
	 * @return Current position of entity.
	 */
	public Coordinate getPosition() {
		return new Coordinate(position.x, position.y);
	}
		
	/**
	 * Set the current position of the entity.
	 * 
	 * @param newPosition The new position of the entity.
	 */
	public void setPosition(Coordinate newPosition) {
		position.x = newPosition.x;
		position.y = newPosition.y;
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Returns flow of activities of the entity.
	 * 
	 * @return Flow of activities.
	 */
	@JsonIgnore
	public Flow getFlow() {
		return flow;
	}
	
	/**
	 * Returns knowledge instance of this entity.
	 * 
	 * @return Knowledge instance.
	 */
	@JsonIgnore
	public EvoKnowledge getKnowledge() {
		return knowledge;
	}
	
	/**
	 * Returns the context of this entity. In case entity has not been assigned
	 * to a specific simulation context.
	 *  
	 * @return Context of this entity or null in case entity has not been
	 *         assigned to a specific simulation context.
	 */
	@JsonIgnore
	public Context getContext() {
		return context;
	}
	
	/**
	 * Specifies the context the entity is used in.
	 * 
	 * @param context Context the entity is used in.
	 */
	public void setContext(Context context) {
		this.context = context;
	}
	
	/**
	 * Returns the relations of this entity.
	 * 
	 * @return Relations of this entity.
	 */
	@JsonIgnore
	public RelationGraph getRelations() {
		return relations;
	}
	
	/**
	 * Initiate knowledge exchange with other entities.
	 */
	public void exchangeKnowledge() {
		knowledge.exchangeKnowledge();
	}
	
	/**
	 * Executes the next activities in the activity queue of the entity and
	 * returns the type of the last executed activity. In case there is no
	 * activity to execute, null is returned.
	 * 
	 * @return Type of executed activity 
	 */
	public Activity execute() {
		
		if (flow.isIdle())
			return null;
		
		Activity executedActivity = flow.getCurrentActivity();
		flow.executeActivity(context.getTime().getDeltaT());
		return executedActivity;
	}
	
	/**
	 * Checks, if entity is an actively moving entity.
	 * 
	 * @return True, if entity is an actively moving agent, false otherwise.
	 */
	@JsonIgnore
	public abstract boolean isActive();
	
	@Override
	public String toString() {
		return "[Entity" + id + "]";
	}
}
