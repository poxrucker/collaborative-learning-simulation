package de.dfki.renderer;

import java.util.Collection;

import com.vividsolutions.jts.geom.Coordinate;

import de.dfki.data.AggregatedDatasetView;
import de.dfki.data.Graph.Edge;
import de.dfki.util.GeoToScreenPositionConverter;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import processing.core.PGraphics;

public final class AggregatedDatasetRenderer extends AbstractRenderer<AggregatedDatasetView> {

  private final GeoToScreenPositionConverter conv;

  public AggregatedDatasetRenderer(GeoToScreenPositionConverter conv) {
    this.conv = conv;
  }
  
  @Override
  public void draw(AggregatedDatasetView dataSet, PGraphics g) {
    // Prepare drawing
    g.noFill();
    
    // Get visible edges
    Collection<Edge> edges = dataSet.getVisibleEdges();
    
    for (Edge edge : edges) {
      double val = (double) edge.property;
      
      if (val < 0) {
        g.strokeWeight(2);
        g.stroke(getColor(g, 70, 10, 10, 255, 0, 0, -val));

      } else if (val > 0) {
        g.strokeWeight(2);
        g.stroke(getColor(g, 10, 70, 10, 0, 255, 0, val));
      
      } else {
        g.strokeWeight(1);
        g.stroke(150, 150, 150);
      }
            
      if (edge.geometry == null)
        return;

      Coordinate[] coords = edge.geometry.getCoordinates();

      for (int j = 0; j < coords.length - 1; j++) {
        ScreenPosition pos = conv.toScreenPosition(coords[j]);
        ScreenPosition next = conv.toScreenPosition(coords[j + 1]);
        g.line(pos.x, pos.y, next.x, next.y);
      }

    }
    
  }

  private int getColor(PGraphics g, int r1, int g1, int b1, int r2, int g2, int b2, double p) {
    
    if (p <= 0)
      return g.color(r1, g1, b1);
    else if (p >= 1)
      return g.color(r2, g2, b2);
    else  {
      int red = (int) ((1-p) * r1 + p * r2);
      int green = (int) ((1-p) * g1 + p * g2);
      int blue = (int) ((1-p) * b1 + p * b2);
      return g.color(red, green, blue);
    }
  }
}
