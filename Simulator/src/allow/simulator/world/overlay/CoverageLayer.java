package allow.simulator.world.overlay;

import java.util.Collection;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import allow.simulator.core.Context;
import allow.simulator.entity.Entity;
import allow.simulator.entity.EntityTypes;
import allow.simulator.entity.Person;
import allow.simulator.util.Coordinate;
import allow.simulator.world.Street;

public final class CoverageLayer implements IOverlay {

  private final double[] areaBounds;
  private final double spacingX;
  private final double spacingY;
  private final int nRows;
  private final int nCols;
  private final CoverageStats[] raster;

  public CoverageLayer(double[] areaBounds, int nRows, int nCols, Collection<Street> streets) {
    this.areaBounds = areaBounds;
    this.nRows = nRows;
    this.nCols = nCols;
    spacingX = (areaBounds[1] - areaBounds[0]) / (nCols - 1);
    spacingY = (areaBounds[3] - areaBounds[2]) / (nRows - 1);

    int nCells = nRows * nCols;
    raster = new CoverageStats[nCells];
    
    for (int i = 0; i < nCells; i++) {
      raster[i] = new CoverageStats();
    }
  }

  @Override
  public boolean update(Context context) {
    // Get all person entities from context
    Collection<Entity> persons = context.getEntityManager().getEntitiesOfType(EntityTypes.PERSON);
    
    // Update 
    updateGrid(persons, context.getTime().getTimestamp());
    return true;
  }

  private void updateGrid(Collection<Entity> entities, long timestamp) {

    for (Entity entity : entities) {
      
      if (!isWithinAreaBounds(entity.getPosition()))
        continue;
      
      Person person = (Person) entity;
      
      if (!person.isParticipating())
        continue;
      
      int row = nRows - 1 - (int) Math.round((entity.getPosition().y - areaBounds[2]) / spacingY);
      int col = (int) Math.round((entity.getPosition().x - areaBounds[0]) / spacingX);
      CoverageStats stats = raster[row * nCols + col];
      stats.update(timestamp);
    }
  }

  private boolean isWithinAreaBounds(Coordinate c) {
    return (c.x >= areaBounds[0]) && (c.x <= areaBounds[1]) && (c.y >= areaBounds[2]) && (c.y <= areaBounds[3]);
  }

  private static final class CoverageStats {
    // Visitation statistics of a cell
    private SummaryStatistics deltaTs;
    
    // Timestamp of last visit
    private long lastVisit;

    private CoverageStats() {
      deltaTs = new SummaryStatistics();
      lastVisit = -1;
    }
    
    private void update(long currentTime) {
      
      if (lastVisit == -1) {
        // If cell has not been visited so far, update last visit timestamp
        lastVisit = currentTime;
        return;
      }
      // Difference between current time and last visit timestamp
      long deltaT = currentTime - lastVisit;
      deltaTs.addValue(deltaT);
      lastVisit = currentTime;
    }
    
     
  }

}
