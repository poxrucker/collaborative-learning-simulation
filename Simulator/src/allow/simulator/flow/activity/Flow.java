package allow.simulator.flow.activity;

import allow.simulator.entity.Entity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class Flow<V extends Activity<? extends Entity>> {
	// Manages the actual flow of activities.
	private final ObjectArrayList<V> flow;
	
	// Currently executed activity.
	private V currentActivity;
	
	/**
	 * Creates new instance with empty flow.
	 */
	public Flow() {
		flow = new ObjectArrayList<V>();
		currentActivity = null;
	}
	
	/**
	 * Execute current activity. If activity finishes, the function updates 
	 * the flow's current activity. Returns the executed activity.
	 * 
	 * @return The executed activity.
	 */
	public V executeActivity(double executionTime) {
		// If current activity is null, return.
		if (currentActivity == null)
			return null;
		
		V temp = null;

		while (currentActivity != null && executionTime > 0.0) {
			// Execute current activity.
			double deltaT = currentActivity.execute(executionTime);

			if (currentActivity == null) {
				executionTime = 0.0;
				
			} else if (currentActivity.isFinished()) {
				updateCurrentActivity();
				temp = currentActivity;
			}
			executionTime -= deltaT;
		}
		return temp;
	}
	
	/**
	 * Check if flow is currently idle, i.e. there is no activity to execute.
	 * 
	 * @return True, if there is no activity to execute.
	 */
	public boolean isIdle() {
		return (currentActivity == null) && flow.isEmpty();
	}

	/**
	 * Returns the currently executed activity.
	 * 
	 * @return The currently executed activity.
	 */
	public V getCurrentActivity() {
		return currentActivity;
	}

	/**
	 * Add an activity to the end if the flow.
	 * 
	 * @param a The Activity to add.
	 */
	public void addActivity(V a) {
		flow.add(a);
		updateCurrentActivity();
	}
	
	public void addAfter(V other, V a) {
	  int index = flow.indexOf(other);
	  flow.add(index + 1, a);
	  updateCurrentActivity();
	}
	
	public void clear() {
		flow.clear();
		currentActivity = null;
	}
	
	private void updateCurrentActivity() {
		// If current activity is null or has finished, update current activity.
		if ((currentActivity == null) || currentActivity.isFinished()) {
		  currentActivity = flow.isEmpty() ? null : flow.remove(0);
		} 
	}
	
}
