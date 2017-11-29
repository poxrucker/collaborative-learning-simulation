package de.dfki.parking.spatial;

import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.quadtree.Quadtree;

import allow.simulator.util.Coordinate;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class ModifiableSpatialIndex<T extends ILocatable> {
  // Spatial index
  private final Quadtree index;
  
  // Convex hull of data covered by index
  private Geometry hull;
  
  public ModifiableSpatialIndex() {
    this.index = new Quadtree();
    this.hull = null;
  }
  
  public boolean contains(Coordinate pos) {
    return (hull != null) && hull.contains(JTSUtil.createPoint(pos));
  }
  
  public List<T> queryInRange(Coordinate position, double maxDistance) {
    // Create a query envelope centered around position
    Envelope queryEnv = JTSUtil.createQueryEnvelope(position, maxDistance);

    // Query spatial index and filter results by distance
    List<T> results = new ObjectArrayList<>();
    index.query(queryEnv, new DistanceFilter<T>(results, position, maxDistance));
    return results;
  }
  
  public void insert(T item) {
    // Add to index
    index.insert(JTSUtil.createPoint(item.getReferencePosition()).getEnvelopeInternal(), item);
    
    // Update hull
    ObjectArrayList<Coordinate> refPositions = new ObjectArrayList<>();
    
    for (Object temp : index.queryAll()) {
      @SuppressWarnings("unchecked")
      T t = (T) temp;
      refPositions.add(t.getReferencePosition());
    }
    hull = JTSUtil.getConvexHull(refPositions);
  }
}