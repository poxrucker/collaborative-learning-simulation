package de.dfki.parking.spatial;

import java.util.Collection;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

final class JTSUtil {
  // Used for calculation of rectangular query envelope 
  private static final double EARTH_CIRCUMFERENCE_IN_M = 40074 * 1000;
  private static final double LAT_M_TO_DEG = 360.0 / EARTH_CIRCUMFERENCE_IN_M;

  // Used for constructing JTS geometries
  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
  
  /**
   * Creates a rectangular envelope with given edge length and centered around
   * the given center coordinate.
   * 
   * @param center Center coordinate of rectangle
   * @param edgeLength Edge length of rectangle
   * @return Rectangular envelope with given specifications
   */
  public static Envelope createQueryEnvelope(allow.simulator.util.Coordinate center, double edgeLength) {
    // Half edge length in every direction
    // double halfDistance = 0.5 * edgeLength;

    // Sample four points from circle around center with radius maxDistance to obtain query envelope
    Coordinate north = new Coordinate(center.x, center.y + LAT_M_TO_DEG * edgeLength);
    Coordinate south = new Coordinate(center.x, center.y - LAT_M_TO_DEG * edgeLength);
    double circumferenceCenter = Math.cos(Math.toRadians(center.y)) * EARTH_CIRCUMFERENCE_IN_M;
    double lonMToDegCenter = 360.0 / circumferenceCenter;
    Coordinate east = new Coordinate(center.x - lonMToDegCenter * edgeLength, center.y);
    Coordinate west = new Coordinate(center.x + lonMToDegCenter * edgeLength, center.y);

    // Create point sequence to build geometry and create Envelope instance
    Coordinate[] points = new Coordinate[] { new Coordinate(north.x, north.y), new Coordinate(east.x, east.y),
        new Coordinate(south.x, south.y), new Coordinate(west.x, west.y) };
    return GEOMETRY_FACTORY.createMultiPoint(points).getEnvelopeInternal();
  }
  
  /**
   * Computes the convex hull of the given set of points.
   * 
   * @param coordinates Points to compute convex hull from
   * @return Convex hull geometry of given points
   */
   public static Geometry getConvexHull(Collection<allow.simulator.util.Coordinate> coordinates) {
    ObjectArrayList<Coordinate> conv = new ObjectArrayList<>();

    for (allow.simulator.util.Coordinate c : coordinates) {
      conv.add(JTSUtil.convert(c));
    }
    Coordinate[] temp = conv.toArray(new Coordinate[conv.size()]);
    ConvexHull hull = new ConvexHull(temp, GEOMETRY_FACTORY);   
    return hull.getConvexHull();
  }
  
  /**
   * Creates a point geometry from the given coordinate.
   * 
   * @param c
   * @return Point geometry
   */
  public static Geometry createPoint(allow.simulator.util.Coordinate c) {
    return GEOMETRY_FACTORY.createPoint(convert(c));
  }
  
  /**
   * Creates a multi-point geometry from the given coordinate.
   * 
   * @param c
   * @return Point geometry
   */
  public static Geometry createMultiPoint(Collection<allow.simulator.util.Coordinate> coordinates) {
    ObjectArrayList<Coordinate> conv = new ObjectArrayList<>();

    for (allow.simulator.util.Coordinate c : coordinates) {
      conv.add(JTSUtil.convert(c));
    }
    Coordinate[] temp = conv.toArray(new Coordinate[conv.size()]);
    return GEOMETRY_FACTORY.createMultiPoint(temp);
  }
  
  private static Coordinate convert(allow.simulator.util.Coordinate c) {
    return new Coordinate(c.x, c.y);
  }

  /*public static boolean isRelevantStreet(Street s) {
    return !((s.getName().equals("Fußweg") || s.getName().equals("Bürgersteig") || s.getName().equals("Stufen") || s.getName().equals("Weg")
        || s.getName().equals("Gasse") || s.getName().equals("Auffahrrampe") || s.getName().equals("Fußgängertunnel")
        || s.getName().equals("Fahrradweg") || s.getName().equals("Fußgängerbrücke")) || s.getName().contains("Autostrada")
        || s.getName().equals("Via Bolzano") || s.getName().contains("Strada Statale") || s.getName().contains("Tangenziale"));
  }*/
}
