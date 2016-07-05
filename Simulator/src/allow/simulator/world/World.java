package allow.simulator.world;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.Arrays;

import allow.simulator.core.Context;
import allow.simulator.util.Pair;
import allow.simulator.world.overlay.IOverlay;

/**
 * Abstract class representing a simulated world.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public abstract class World {
	// Dimensions of the world (minX, minY, maxX, maxY)
	protected final double[] dimensions;
	
	// List of overlays to the world
	protected final ObjectList<Pair<String, IOverlay>> overlays;
	
	/**
	 * Creates a new instance initializing dimensions and overlay management.
	 */
	protected World() {
		overlays = new ObjectArrayList<Pair<String,IOverlay>>();
		dimensions = new double[4];
	}
	
	public double[] getDimensions() {
		return Arrays.copyOf(dimensions, dimensions.length);
	}
	
	public boolean addOverlay(IOverlay overlay, String overlayId) {
		return checkIdentifier(overlayId) ? overlays.add(new Pair<String, IOverlay>(overlayId, overlay)) : false;
	}
	
	public IOverlay getOverlay(String overlayId) {
		
		for (Pair<String, IOverlay> overlay : overlays) {
			
			if (overlay.first.equals(overlayId))
				return overlay.second;
		}
		return null;
	}

	public boolean removeOverlay(String overlayId) {
		int index = -1;
		
		for (Pair<String, IOverlay> overlay : overlays) {
			
			if (overlay.first.equals(overlayId))
				break;
			index++;
		}
		return (index != -1) ? (overlays.remove(index) != null) : false;
	}
	
	public boolean update(Context context) {
		boolean changed = false;
		
		for (Pair<String, IOverlay> overlay : overlays) {
			changed |= overlay.second.update(context);
		}
		return changed;
	}
	
	private boolean checkIdentifier(String identifier) {
		
		for (Pair<String, IOverlay> overlay : overlays) {
			
			if (overlay.first.equals(identifier))
				return false;
		}
		return true;
	}
}
