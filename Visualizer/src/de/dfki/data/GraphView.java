package de.dfki.data;

import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;

import de.dfki.data.Graph.Edge;
import de.dfki.data.Graph.Vertex;

public final class GraphView {

  private Graph graph;
  private Envelope currentEnvelope;
  private STRtree vertexIndex;
  private STRtree edgeIndex;

  public GraphView(Graph graph) {
    this.graph = graph;
    Graph undirected = Graph.toUndirectedGraph(graph);
    buildSpatialIndex(undirected);
  }
  
  private synchronized void buildSpatialIndex(Graph graph) {
    vertexIndex = new STRtree();
    edgeIndex = new STRtree();

    for (Vertex v : graph.vertices.values()) {
      vertexIndex.insert(new Envelope(v.position), v);
    }

    for (Edge e : graph.edges.values()) {
      if (e.geometry == null)
        return;

      edgeIndex.insert(e.geometry.getEnvelopeInternal(), e);
    }
    vertexIndex.build();
    edgeIndex.build();
    currentEnvelope = (Envelope)vertexIndex.getRoot().getBounds();
  }
  
  @SuppressWarnings("unchecked")
  public List<Vertex> getVerticesInEnv(Envelope bounds) {
    return (List<Vertex>)vertexIndex.query(bounds);
  }
  
  @SuppressWarnings("unchecked")
  public List<Edge> getEdgesInEnv(Envelope bounds) {
    return (List<Edge>)edgeIndex.query(bounds);
  }
  
  public Envelope getBaseEnvelope() {
    return (Envelope)vertexIndex.getRoot().getBounds();
  }
  
  public Graph getGraph() {
    return graph;
  }
  
  public void setVisibleArea(Envelope env) {
    currentEnvelope = env;
  }
  
  public List<Vertex> getVisibleVertices() {
    return getVerticesInEnv(currentEnvelope);
  }
  
  public List<Edge> getVisibleEdges() {
    return getEdgesInEnv(currentEnvelope);
  }
   
}
