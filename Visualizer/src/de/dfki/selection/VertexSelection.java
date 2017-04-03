package de.dfki.selection;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import de.dfki.data.Graph.Edge;
import de.dfki.data.Graph.Vertex;
import de.dfki.util.Util;
import de.dfki.visualization.GraphDisplayAdapter;
import de.fhpotsdam.unfolding.geo.Location;

public class VertexSelection {

  private Vertex cursorVertex;
  private Edge cursorEdge;
  
  private Vertex selectedVertex;
  private List<Edge> selectedEdges;
  
  private final GraphDisplayAdapter graph;
  
  public VertexSelection(GraphDisplayAdapter graph) {
    this.graph = graph;
    selectedEdges = new ArrayList<Edge>();
  }
  
  public void updateCursorState(Location l) {
    // Create mouse envelope
    Envelope mouseEnv = new Envelope(new Coordinate(l.x, l.y));
    mouseEnv.expandBy(0.0001);
    
    // Determine close vertex
    cursorVertex = getClosestVertex(mouseEnv);

    if (cursorVertex == null || selectedVertex == null) {
      cursorEdge = null;
      return;
    }
    cursorEdge = graph.getGraph().edges.get(selectedVertex.label + ";;" + cursorVertex.label);
  }
  
  public void updateSelectionState() {
    if (cursorVertex == null) {
      selectedEdges.clear();
      selectedVertex = null;
      return;

    } else if (selectedVertex == null) {
      selectedVertex = cursorVertex;

    } else if (cursorVertex == selectedVertex) {
      selectedVertex = null;

    } else {
      Edge candidate = graph.getGraph().edges.get(selectedVertex.label + ";;" + cursorVertex.label);
      selectedVertex = cursorVertex;

      if (candidate == null || selectedEdges.contains(candidate))
        return;

      selectedEdges.add(candidate);
    }
  }
  
  public Vertex getSelectedVertex() {
    return selectedVertex;
  }
  
  public Vertex getCursorVertex() {
    return cursorVertex;
  }
  
  public Edge getCursorEdge() {
    return cursorEdge;
  }
  
  public List<Edge> getSelectedEdges() {
    return selectedEdges;
  }
  
  public void clearSelectedEdges() {
    selectedEdges.clear();
  }
  
  private Vertex getClosestVertex(Envelope mouseEnv) {
    List<Vertex> closeVertices = graph.getVerticesInEnv(mouseEnv);

    if (closeVertices.size() == 0)
      return null;

    if (closeVertices.size() == 1)
      return closeVertices.get(0);

    double minDist = Double.MAX_VALUE;
    Vertex closestVertex = null;

    for (Vertex v : closeVertices) {
      double dist = Util.euclideanDistance(mouseEnv.centre(), v.position);

      if (dist < minDist) {
        closestVertex = v;
        minDist = dist;
      }
    }
    return closestVertex;
  }
}
