package de.dfki.renderer;

import com.vividsolutions.jts.geom.Coordinate;

import de.dfki.data.Graph.Edge;
import de.dfki.util.GeoToScreenPositionConverter;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import processing.core.PGraphics;
import processing.core.PVector;

public final class DirectedEdgeRenderer extends AbstractRenderer<Edge> {
  
  private final GeoToScreenPositionConverter conv;
  private int color;
  private int strokeWeight;
  private float arrowSize;
  private boolean showVertexLabels;
  private boolean showEdgeLabel;
  
  public DirectedEdgeRenderer(GeoToScreenPositionConverter conv, int edgeColor, int strokeWeight,
      float arrowSize, boolean showVertexLabels, boolean showEdgeLabel) {
    this.conv = conv;
    this.color = edgeColor;
    this.strokeWeight = strokeWeight;
    this.arrowSize = arrowSize;
    this.showVertexLabels = showVertexLabels;
    this.showEdgeLabel = showEdgeLabel;
  }
  
  @Override
  public void draw(Edge edge, PGraphics g) {
    // Prepare drawing
    g.noFill();
    g.strokeWeight(strokeWeight);
    g.stroke(color);
    drawDirectedEdge(g, edge);

    if (showVertexLabels) {
      ScreenPosition s = conv.toScreenPosition(edge.start.position);
      ScreenPosition e = conv.toScreenPosition(edge.end.position);

      ScreenPosition lower, upper;
      String lowerText, upperText;

      if (s.y > e.y) {
        lower = e;
        upper = s;
        lowerText = edge.end.label;
        upperText = edge.start.label;

      } else {
        lower = s;
        upper = e;
        lowerText = edge.start.label;
        upperText = edge.end.label;
      }

      PVector d = new PVector(upper.x - lower.x, upper.y - lower.y);
      d.normalize();
      d.mult(12.0f);
      PVector d1 = new PVector(d.y, -d.x);
      lower.add(d1.x, -d1.y, 0);
      upper.add(d1.x, d1.y, 0);
      g.text(lowerText, lower.x, lower.y);
      g.text(upperText, upper.x, upper.y);
    }
    
    if (showEdgeLabel) {
      ScreenPosition t = conv.toScreenPosition(new Coordinate(0.5 * (edge.end.position.x - edge.start.position.x) + edge.start.position.x, 
          0.5 * (edge.end.position.y - edge.start.position.y) + edge.start.position.y));
      g.text(edge.label, t.x + 2, t.y + 2);
    }
  }
  
  private void drawDirectedEdge(PGraphics g, Edge edge) {
    float headx0 = -1.0f, heady0 = -1.0f, headx1 = -1.0f, heady1 = -1.0f;
    float tailx0 = -1.0f, taily0 = -1.0f, tailx1 = -1.0f, taily1 = -1.0f;
    Coordinate[] coords = edge.geometry.getCoordinates();

    for (int i = 0; i < coords.length - 1; i++) {
      ScreenPosition pos = conv.toScreenPosition(coords[i]);
      ScreenPosition next = conv.toScreenPosition(coords[i + 1]);
      g.line(pos.x, pos.y, next.x, next.y);

      if (i == 0) {
        headx0 = pos.x;
        heady0 = pos.y;
        headx1 = next.x;
        heady1 = next.y;
      }

      if (i == coords.length - 2) {
        tailx0 = next.x;
        taily0 = next.y;
        tailx1 = pos.x;
        taily1 = pos.y;
      }
    }
    g.fill(color);
    drawArrow(g, tailx0, taily0, tailx1, taily1, arrowSize);

    if (!edge.directed)
      drawArrow(g, headx0, heady0, headx1, heady1, arrowSize);
  }
  
  private void drawArrow(PGraphics g, float x0, float y0, float x1, float y1, float size) {
    PVector d = new PVector(x1 - x0, y1 - y0);
    d.normalize();
    float coeff = 1.5f;
    float angle = (float) Math.atan2(d.y, d.x);

    g.pushMatrix();
    g.translate(x0, y0);
    g.rotate(angle + PGraphics.PI);
    g.strokeCap(PGraphics.ROUND);
    g.triangle(-size * coeff, -size, -size * coeff, size, 0, 0);
    g.popMatrix();
  }
}
