package de.dfki.parking.spatial;

import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.strtree.STRtree;

import allow.simulator.util.Coordinate;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class UnmodifiableSpatialIndex<T extends ILocatable> {
  // Spatial index
  private final STRtree index;
  
  // Convex hull of data covered by index
  private final Geometry hull;
  
  public UnmodifiableSpatialIndex(Collection<T> items) {
    // Initialize index
    this.index = new STRtree();    
    ObjectArrayList<Coordinate> refPositions = new ObjectArrayList<>();
    
    for (T item : items) {
      index.insert(JTSUtil.createPoint(item.getReferencePosition()).getEnvelopeInternal(), item);
      refPositions.add(item.getReferencePosition());
    }
    index.build();  
    this.hull = JTSUtil.getConvexHull(refPositions);
  }
  
  public boolean contains(Coordinate pos) {
    return hull.contains(JTSUtil.createPoint(pos));
  }
  
  public List<T> queryInRange(Coordinate position, double maxDistance) {
    // Create a query envelope centered around position
    Envelope queryEnv = JTSUtil.createQueryEnvelope(position, maxDistance);

    // Query spatial index and filter results by distance
    List<T> results = new ObjectArrayList<>();
    index.query(queryEnv, new DistanceFilter<T>(results, position, maxDistance));
    return results;
  }
}