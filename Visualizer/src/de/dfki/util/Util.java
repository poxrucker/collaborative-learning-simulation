package de.dfki.util;

import com.vividsolutions.jts.geom.Coordinate;

import de.dfki.data.Graph.Edge;

public final class Util {

  public static double distToEdge(Coordinate c, Edge e) {
    double minDist = Double.MAX_VALUE;
    Coordinate[] coordinates = e.geometry.getCoordinates();

    for (int i = 0; i < coordinates.length - 1; i++) {
      Coordinate proj = projectToLine(coordinates[i], coordinates[i + 1], c);
      double dist = euclideanDistance(c, proj);

      if (dist < minDist)
        minDist = dist;
    }
    return minDist;
  }

  public static Coordinate projectToLine(Coordinate start, Coordinate end, Coordinate c) {
    Coordinate a = new Coordinate(end.x - start.x, end.y - start.y);
    Coordinate b = new Coordinate(c.x - start.x, c.y - start.y);

    double norm_a_square = a.x * a.x + a.y * a.y;
    double r = (a.x * b.x + a.y * b.y) / norm_a_square;

    if (r < 0.0) {
      return start;

    } else if (r >= 0.0 && r < 1.0) {
      return new Coordinate(start.x + r * a.x, start.y + r * a.y);

    } else {
      return end;
    }
  }

  public static double euclideanDistance(Coordinate first, Coordinate second) {
    double diffX = first.x - second.x;
    double diffY = first.y - second.y;
    return Math.sqrt(diffX * diffX + diffY * diffY);
  }

}
