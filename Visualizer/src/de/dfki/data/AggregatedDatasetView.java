package de.dfki.data;

import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;

import de.dfki.data.Graph.Edge;

public class AggregatedDatasetView {
   
  private Dataset dataSet;
  private Envelope currentEnvelope;
  private STRtree edgeIndex;

  public AggregatedDatasetView(Graph graph, Dataset dataSet) {
    this.dataSet = dataSet;
    buildSpatialIndex(graph, dataSet);
  }
  
  private void buildSpatialIndex(Graph graph, Dataset dataSet) {
    edgeIndex = new STRtree();

    for (String edgeLabel : dataSet.getEdges()) {
      Edge edge = graph.edges.get(edgeLabel);
      
      if (edge == null)
        continue;

      double data = dataSet.getAggregatedDataForEdge(edge.start.label + ";;" + edge.end.label);
      
      Edge newEdge = graph.new Edge(edge.start, edge.end, edge.label, edge.geometry, edge.directed, data);
      edgeIndex.insert(newEdge.geometry.getEnvelopeInternal(), newEdge);
    }
    edgeIndex.build();
    currentEnvelope = (Envelope)edgeIndex.getRoot().getBounds();
  }
  
  @SuppressWarnings("unchecked")
  public List<Edge> getEdgesInEnv(Envelope bounds) {
    return (List<Edge>)edgeIndex.query(bounds);
  }
  
  public Dataset getDataset() {
    return dataSet;
  }
  
  public Envelope getBaseEnvelope() {
    return (Envelope)edgeIndex.getRoot().getBounds();
  }
  
  public void setVisibleArea(Envelope env) {
    currentEnvelope = env;
  }
  
  public List<Edge> getVisibleEdges() {
    return getEdgesInEnv(currentEnvelope);
  }
}
