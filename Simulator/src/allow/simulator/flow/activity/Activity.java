package allow.simulator.flow.activity;

import allow.simulator.entity.Entity;

/**
 * Abstract class representing an Activity to be executed by an entity in a
 * flow.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public abstract class Activity<V extends Entity> {
	// Indicates if Activity has finished
	private boolean finished;
	
	// Type of the Activity
	protected final ActivityType type;
	
	// Entity executing the activity
	protected final V entity;
	
	// Starting and ending timestamps
	protected long tStart;
	protected long tEnd;
	
	/**
	 * Constructor.
	 * 
	 * @param type Type of the Activity.
	 * @param entitiy Entity supposed to execute the Activity.
	 */
	protected Activity(ActivityType type, V entity) {
		this.type = type;
		this.entity = entity;
		finished = false;
		tStart = -1;
		tEnd = -1;
	}
	
	/**
	 * Execute the Activity, which may require more than one call of execute().
	 * If the returned time is smaller than deltaT, activity has finished before
	 * deltaT.
	 * 
	 * @param deltaT Time to execute the activity.
	 * @return Time needed to execute the activity.
	 */
	public abstract double execute(double deltaT);
	
	/**
	 * Check, if current Activity is finished.
	 * 
	 * @return True, if Activity is finished, false otherwise.
	 */
	public boolean isFinished() {
		return finished;
	}
	
	/**
	 * Marks this Activity instance as finished.
	 */
	public void setFinished() {
		finished = true;
	}
	
	/**
	 * Returns the type of the Activity.
	 * 
	 * @return Type of the Activity.
	 */
	public ActivityType getType() {
		return type;
	}
}
