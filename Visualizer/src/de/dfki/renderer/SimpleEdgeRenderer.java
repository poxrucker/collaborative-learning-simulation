package de.dfki.renderer;

import com.vividsolutions.jts.geom.Coordinate;

import de.dfki.data.Graph.Edge;
import de.dfki.visualization.GeoToScreenPositionConverter;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import processing.core.PGraphics;

public final class SimpleEdgeRenderer extends AbstractRenderer<Edge> {

  private final GeoToScreenPositionConverter conv;
  private int color;
  private int strokeWeight;
  
  public SimpleEdgeRenderer(GeoToScreenPositionConverter conv, int edgeColor, int strokeWeight) {
    this.conv = conv;
    this.color = edgeColor;
    this.strokeWeight = strokeWeight;
  }
  
  public void setColor(int edgeColor) {
    this.color = edgeColor;
  }
  
  @Override
  public void draw(Edge edge, PGraphics g) {
    // Prepare drawing
    g.noFill();
    g.stroke(color);
    g.strokeWeight(strokeWeight);
    
    if (edge.geometry == null)
      return;

    Coordinate[] coords = edge.geometry.getCoordinates();

    for (int i = 0; i < coords.length - 1; i++) {
      ScreenPosition pos = conv.toScreenPosition(coords[i]);
      ScreenPosition next = conv.toScreenPosition(coords[i + 1]);
      g.line(pos.x, pos.y, next.x, next.y);
    }
  }

}
