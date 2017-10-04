package allow.simulator.parking;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import allow.simulator.parking.ParkingMap.ParkingMapEntry;
import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public final class ParkingGrid {

  private final double[] areaBounds;
  private final double spacingX;
  private final double spacingY;
  private final double spacingXInM;
  private final double spacingYInM;
  private final int nRows;
  private final int nCols;
  private final Set<ParkingMapEntry>[] raster;

  @SuppressWarnings("unchecked")
  public ParkingGrid(double[] areaBounds, int nRows, int nCols) {
    this.areaBounds = areaBounds;
    this.nRows = nRows;
    this.nCols = nCols;
    spacingX = (areaBounds[1] - areaBounds[0]) / (nCols - 1);
    spacingY = (areaBounds[3] - areaBounds[2]) / (nRows - 1);
    final Coordinate bottomLeft = new Coordinate(areaBounds[0], areaBounds[2]);
    spacingXInM = Geometry.haversineDistance(bottomLeft, new Coordinate(areaBounds[1], areaBounds[2])) / (nCols - 1);
    spacingYInM = Geometry.haversineDistance(bottomLeft, new Coordinate(areaBounds[0], areaBounds[3])) / (nRows - 1);

    raster = (Set<ParkingMapEntry>[]) new ObjectOpenHashSet[nRows * nCols];
  }

  public boolean insert(Coordinate pos, ParkingMapEntry value) {

    if (!isWithinAreaBounds(pos))
      return false;
    
    int row = nRows - 1 - (int) Math.round((pos.y - areaBounds[2]) / spacingY);
    int col = (int) Math.round((pos.x - areaBounds[0]) / spacingX);
    return getRaster(row * nCols + col).add(value);
  }

  public Collection<ParkingMapEntry> query(Coordinate pos, double distance) {
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
    Collection<ParkingMapEntry> ret = new ObjectArrayList<>();

    for (int i = minRow; i < maxRow + 1; i++) {
      final int offset = i * nCols;

      for (int j = minCol; j < maxCol + 1; j++) {
        Set<ParkingMapEntry> entries = getRaster(offset + j);

        for (ParkingMapEntry entry : entries) {
          
          if (Geometry.haversineDistance(pos, entry.getPosition()) > distance)
            continue;

          ret.add(entry);
        }
      }
    }
    return ret;
  }

  private ObjectOpenHashSet<ParkingMapEntry> getRaster(int index) {
    
    if (raster[index] == null)
      raster[index] = new ObjectOpenHashSet<>();

    return (ObjectOpenHashSet<ParkingMapEntry>) raster[index];
  }
  
  private boolean isWithinAreaBounds(Coordinate c) {
    return (c.x >= areaBounds[0]) && (c.x <= areaBounds[1]) && (c.y >= areaBounds[2]) && (c.y <= areaBounds[3]);
  }

}
