package allow.simulator.world.overlay;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import allow.simulator.core.Context;
import allow.simulator.entity.Entity;
import allow.simulator.entity.EntityTypes;
import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;

public class RasterOverlay implements IOverlay {

	private final double[] areaBounds;
	private final double spacingX;
	private final double spacingY;
	private final double spacingXInM;
	private final double spacingYInM;
	private final int nRows;
	private final int nCols;
	private final ReferenceOpenHashSet<Entity>[] raster;
	
	@SuppressWarnings("unchecked")
 	public RasterOverlay(double[] areaBounds, int nRows, int nCols) {
		this.areaBounds = areaBounds;
		this.nRows = nRows;
		this.nCols = nCols;
		spacingX = (areaBounds[1] - areaBounds[0]) / (nCols - 1);
		spacingY = (areaBounds[3] - areaBounds[2]) / (nRows - 1);
		final Coordinate bottomLeft = new Coordinate(areaBounds[0], areaBounds[2]);
		spacingXInM = Geometry.haversineDistance(bottomLeft, new Coordinate(areaBounds[1], areaBounds[2])) / (nCols - 1);
		spacingYInM = Geometry.haversineDistance(bottomLeft, new Coordinate(areaBounds[0], areaBounds[3])) / (nRows - 1);
		raster = (ReferenceOpenHashSet<Entity>[]) new ReferenceOpenHashSet[nRows * nCols];
		
		for (int i = 0; i < raster.length; i++) {
			raster[i] = new ReferenceOpenHashSet<Entity>();
		}
	}
	
	@Override
	public boolean update(Context context) {
		resetRaster();
		Collection<Entity> persons = context.getEntityManager().getEntitiesOfType(EntityTypes.PERSON);
		mapToGrid(persons);
		return true;
	}
	
	public Collection<Entity> getCloseEntities(Coordinate pos, double distance) {
		if (!isWithinAreaBounds(pos))
			return Collections.emptyList();
		
		final int row = nRows - 1 - (int) Math.round((pos.y - areaBounds[2]) / spacingY);
		final int col = (int) Math.round((pos.x - areaBounds[0]) / spacingX);
		
		final int nNeighborCellsX = (int) (distance / spacingXInM);
		final int nNeighborCellsY = (int) (distance / spacingYInM);
		final int minRow = Math.max(0, row - nNeighborCellsY);
		final int minCol = Math.max(0, col - nNeighborCellsX);
		final int maxRow = Math.min(nRows, row + nNeighborCellsY);
		final int maxCol = Math.min(nCols, col + nNeighborCellsX);
		Collection<Entity> ret = new ArrayList<Entity>();

		for (int i = minRow; i < maxRow + 1; i++) {
			final int offset = i * nCols;
			
			for (int j = minCol; j < maxCol + 1; j++) {
				ReferenceOpenHashSet<Entity> entities = raster[offset + j];
				
				for (Entity entity : entities) {
					if (Geometry.haversineDistance(pos, entity.getPosition()) > distance)
						continue;
					
					ret.add(entity);
				}
			}
		}
		return ret;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		for (int i = 0; i < nRows; i++) {
			final int offset = i * nCols;
			
			for (int j = 0; j < nCols; j++) {
				buffer.append(raster[offset + j].size() + " ");
			}
			buffer.append("\n");
		}
		return buffer.toString();
	}
	
	private void mapToGrid(Collection<Entity> entities) {
		
		for (Entity entity : entities) {
			
			if (!isWithinAreaBounds(entity.getPosition()))
				continue;
			int row = nRows - 1 - (int) Math.round((entity.getPosition().y - areaBounds[2]) / spacingY);
			int col = (int) Math.round((entity.getPosition().x - areaBounds[0]) / spacingX);
			raster[row * nCols + col].add(entity);
		}
	}
	
	private boolean isWithinAreaBounds(Coordinate c) {
		return (c.x >= areaBounds[0]) && (c.x <= areaBounds[1]) 
				&& (c.y >= areaBounds[2]) && (c.y <= areaBounds[3]);
	}
	
	private void resetRaster() {
		
		for (int i = 0; i < raster.length; i++) {
			raster[i].clear();
		}
	}
}
