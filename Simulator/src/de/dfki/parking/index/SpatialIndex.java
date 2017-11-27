package de.dfki.parking.index;

import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.strtree.STRtree;

import allow.simulator.util.Coordinate;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

final class SpatialIndex {
  // Spatial index
  private final STRtree index;
  
  // Convex hull of data covered by index
  private Geometry hull;
  
  private SpatialIndex(STRtree index, Geometry hull) {
    this.index = index;
    this.hull = hull;
  }
  
  public boolean contains(Coordinate pos) {
    return hull.contains(JTSUtil.createPoint(pos));
  }
  
  public List<ParkingIndexEntry> findEntriesWithMaxDistance(Coordinate position, double maxDistance) {
    // Create a query envelope centered around position
    Envelope queryEnv = JTSUtil.createQueryEnvelope(position, maxDistance);

    // Query spatial index and filter results by distance
    List<ParkingIndexEntry> results = new ObjectArrayList<>();
    index.query(queryEnv, new DistanceFilter(results, position, maxDistance));
    return results;
  }
  
  public static SpatialIndex build(Collection<ParkingIndexEntry> entries) {
    // Spatial index to be created
    STRtree index = new STRtree();
    
    // List of reference positions for convex hull computation
    ObjectArrayList<Coordinate> refPositions = new ObjectArrayList<>();

    for (ParkingIndexEntry entry : entries) {
      index.insert(JTSUtil.createPoint(entry.getReferencePosition()).getEnvelopeInternal(), entry);
      
      if (entry.getParking().getNumberOfParkingSpots() == 0)
        continue;
      
      refPositions.add(entry.getReferencePosition());
    }
    index.build(); 
    return new SpatialIndex(index, JTSUtil.getConvexHull(refPositions));
  }
}