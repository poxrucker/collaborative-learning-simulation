package allow.simulator.world.overlay;

public interface IOverlay {

	/**
	 * Updates the state of the overlay and returns true, if there have been
	 * changes, false otherwise.
	 * 
	 * @return True, if state changed, false otherwise
	 */
	boolean update();
	
}
