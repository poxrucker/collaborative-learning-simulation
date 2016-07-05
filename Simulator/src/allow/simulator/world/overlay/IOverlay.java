package allow.simulator.world.overlay;

import allow.simulator.core.Context;

public interface IOverlay {

	/**
	 * Updates the state of the overlay and returns true, if there have been
	 * changes, false otherwise.
	 * 
	 * @return True, if state changed, false otherwise
	 */
	boolean update(Context context);
	
}
