package de.dfki.util;

import com.vividsolutions.jts.geom.Coordinate;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.ScreenPosition;

public final class GeoToScreenPositionConverter {

  private UnfoldingMap map;
  
  public GeoToScreenPositionConverter(UnfoldingMap map) {
    this.map = map;
  }
  
  public ScreenPosition toScreenPosition(Coordinate c) {
    return map.getScreenPosition(new Location(c.x, c.y));
  }
  
  public Coordinate toLatLon(ScreenPosition p) {
    Location l = map.getLocation(p);
    return new Coordinate(l.x, l.y);
  }
}
