package de.dfki.renderer;

import java.util.List;

import de.dfki.data.GraphView;
import de.dfki.data.Graph.Edge;
import de.dfki.data.Graph.Vertex;
import de.dfki.util.GeoToScreenPositionConverter;
import processing.core.PGraphics;

public final class GraphRenderer extends AbstractRenderer<GraphView> {
  // Indicate whether to draw graph edges
  private boolean drawEdges;
  
  // Indicates whether to draw graph vertices 
  private boolean drawVertices;
  
  // Renderer for drawing vertices if drawVertices == true
  private final SimpleVertexRenderer vertexRenderer;

  // Renderer for drawing edges if drawEdges == true
  private final SimpleEdgeRenderer edgeRenderer;
  
  public GraphRenderer(GeoToScreenPositionConverter conv, PGraphics g) {
    // Prepare vertex rendering
    vertexRenderer = new SimpleVertexRenderer(conv, g.color(100, 100, 100), 2, 1, false);
    drawVertices = false;
    
    // Prepare edge drawing
    edgeRenderer = new SimpleEdgeRenderer(conv, g.color(150, 150, 150), 1);
    drawEdges = true;
  }
  
  @Override
  public void draw(GraphView graph, PGraphics g) {
    g.pushStyle();

    if (drawEdges)
      drawEdges(graph.getVisibleEdges(), g);

    if (drawVertices)
      drawVertices(graph.getVisibleVertices(), g);
 
    g.popStyle();
  }

  public boolean drawEdges() {
    return drawEdges;
  }
  
  public boolean drawVertices() {
    return drawVertices;
  }
  
  public void setDrawVertices(boolean draw) {
    drawVertices = draw;
  }
  
  public void setDrawEdges(boolean draw) {
    drawEdges = draw;
  }
  
  public void setVertexColor(int vertexColor) {
    vertexRenderer.setColor(vertexColor);
  }
  
  public void setEdgeColor(int edgeColor) {
    edgeRenderer.setColor(edgeColor);
  }
  
  private void drawEdges(List<Edge> edges, PGraphics g) {
    g.pushStyle();
    
    for (Edge e : edges) {
      edgeRenderer.draw(e, g);
    }
    g.popStyle();
  }

  private void drawVertices(List<Vertex> vertices, PGraphics g) {
    g.pushStyle();
    
    for (Vertex v : vertices) {
      vertexRenderer.draw(v, g);
    }
    g.popStyle();
  }

}
