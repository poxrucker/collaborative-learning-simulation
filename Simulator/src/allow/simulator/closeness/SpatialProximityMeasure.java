package allow.simulator.closeness;

import java.util.Collection;

import allow.simulator.core.Simulator;
import allow.simulator.entity.Entity;
import allow.simulator.world.overlay.RasterOverlay;

public final class SpatialProximityMeasure implements IProximityMeasure {
	/**
	 * Static instance of SpatialProximityMeasure
	 */
	public static final IProximityMeasure Instance = new SpatialProximityMeasure();
	
	private SpatialProximityMeasure() { }
	
	@Override
	public Collection<Entity> getCloseEntities(Entity entity, double maxDist) {
		RasterOverlay overlay = (RasterOverlay) entity.getContext().getWorld().getOverlay(Simulator.OVERLAY_RASTER);
		return overlay.getCloseEntities(entity.getPosition(), maxDist);
	}

}
