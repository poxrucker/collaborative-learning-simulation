package allow.simulator.closeness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import allow.simulator.core.AllowSimulationModel;
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
		RasterOverlay overlay = (RasterOverlay) entity.getContext().getWorld().getOverlay(AllowSimulationModel.OVERLAY_RASTER);
		Collection<Entity> close = overlay.getCloseEntities(entity.getPosition(), maxDist);
		
		if (close.size() == 0)
			return Collections.emptyList();
		
		Collection<Entity> ret = new ArrayList<Entity>();
		
		for (Entity e : close) {
			
			if (!e.isActive() || e.getId() == entity.getId())
				continue;
			
			ret.add(e);
		}
		return ret;
	}
}
