package de.dfki.parking.model;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

import allow.simulator.world.Street;

public final class ParkingIndexUtil {
  
  private static final double EARTH_CIRCUMFERENCE_IN_M = 40074 * 1000;
  private static final double LAT_M_TO_DEG = 360.0 / EARTH_CIRCUMFERENCE_IN_M;

  public static Coordinate convert(allow.simulator.util.Coordinate c) {
    return new Coordinate(c.x, c.y);
  }

  public static Envelope createQueryEnvelope(allow.simulator.util.Coordinate center, double maxWidth, GeometryFactory factory) {
    // Half maximum distance in every direction
    double halfDistance = 0.5 * maxWidth;

    // Sample four points from circle around center with radius maxDistance to obtain query envelope
    Coordinate north = new Coordinate(center.x, center.y + LAT_M_TO_DEG * halfDistance);
    Coordinate south = new Coordinate(center.x, center.y - LAT_M_TO_DEG * halfDistance);
    double circumferenceCenter = Math.cos(Math.toRadians(center.y)) * EARTH_CIRCUMFERENCE_IN_M;
    double lonMToDegCenter = 360.0 / circumferenceCenter;
    Coordinate east = new Coordinate(center.x - lonMToDegCenter * halfDistance, center.y);
    Coordinate west = new Coordinate(center.x + lonMToDegCenter * halfDistance, center.y);

    // Create point sequence to build geometry and create Envelope instance
    Coordinate[] points = new Coordinate[] { new Coordinate(north.x, north.y), new Coordinate(east.x, east.y),
        new Coordinate(south.x, south.y), new Coordinate(west.x, west.y) };
    return factory.createMultiPoint(points).getEnvelopeInternal();
  }
  
  public static boolean isRelevantStreet(Street s) {
    return !((s.getName().equals("Fußweg") || s.getName().equals("Bürgersteig") || s.getName().equals("Stufen") || s.getName().equals("Weg")
        || s.getName().equals("Gasse") || s.getName().equals("Auffahrrampe") || s.getName().equals("Fußgängertunnel")
        || s.getName().equals("Fahrradweg") || s.getName().equals("Fußgängerbrücke")) || s.getName().contains("Autostrada")
        || s.getName().equals("Via Bolzano") || s.getName().contains("Strada Statale") || s.getName().contains("Tangenziale"));
  }
}
