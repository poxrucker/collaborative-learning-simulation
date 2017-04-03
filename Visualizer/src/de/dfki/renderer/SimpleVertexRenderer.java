package de.dfki.renderer;

import de.dfki.data.Graph.Vertex;
import de.dfki.visualization.GeoToScreenPositionConverter;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import processing.core.PGraphics;

public final class SimpleVertexRenderer extends AbstractRenderer<Vertex> {
  // Converts map to screen coordinates and vice versa
  private final GeoToScreenPositionConverter conv;
  
  // Vertex drawing size
  private int size;
  
  // Vertex drawing color
  private int color;
  
  // Stroke width
  private int strokeWeight;
  
  // Determines whether to show label
  private boolean showLabel;
  
  /**
   * Creates a new instance of a SimpleVertexRenderer drawing vertices
   * using the given color, size, and stroke weight. If showLabels is true,
   * the renderer displays the label of the vertex next to it.
   * @param conv Converter instance mapping map to screen positions
   * @param vertexColor Color to draw vertices
   * @param vertexSize Size to draw vertices
   * @param strokeWeight Stroke width for drawing
   * @param showLabel True if vertex label should be shown, false otherwise
   */
  public SimpleVertexRenderer(GeoToScreenPositionConverter conv, int vertexColor, int vertexSize,
      int strokeWeight, boolean showLabel) {
    this.conv = conv;
    this.size = vertexSize;
    this.color = vertexColor;
    this.strokeWeight = strokeWeight;
    this.showLabel = showLabel;
  }
  
  @Override
  public void draw(Vertex vertex, PGraphics g) {
    // Preparing drawing settings
    g.fill(color);
    g.stroke(color);
    g.strokeWeight(strokeWeight);

    // Draw vertex
    ScreenPosition pos = conv.toScreenPosition(vertex.position);
    g.ellipse(pos.x, pos.y, size, size);
    
    // Show label depending on flag
    if (showLabel) {
      g.noFill();
      g.text(vertex.label, pos.x + 5, pos.y - 5);
    }
  }
}
