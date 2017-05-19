package de.dfki.data;

import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;

import de.dfki.data.Graph.Edge;

public class BrowsableDatasetView {
  
  private Graph graph;
  private Dataset dataSet;
  private Envelope currentEnvelope;
  private STRtree edgeIndex;
  private STRtree currentIndex;
  private double currentTime;
  
  public BrowsableDatasetView(Graph graph, Dataset dataSet) {
    this.dataSet = dataSet;
    this.graph = graph;
    buildSpatialIndex();
    currentIndex = new STRtree();
    setCurrentTime(dataSet.getMinimumTime());
  }
  
  private void buildSpatialIndex() {
    edgeIndex = new STRtree();

    for (String edgeLabel : dataSet.getEdges()) {
      Edge edge = graph.edges.get(edgeLabel);
      
      if (edge == null)
        continue;

      double[] data = dataSet.getDataForEdge(edge.start.label + ";;" + edge.end.label);
      
      Edge newEdge = graph.new Edge(edge.start, edge.end, edge.label, edge.geometry, edge.directed, data);
      edgeIndex.insert(newEdge.geometry.getEnvelopeInternal(), newEdge);
    }
    edgeIndex.build();
    currentEnvelope = (Envelope)edgeIndex.getRoot().getBounds();
  }
  
  private void buildCurrentIndex() {
    
    synchronized (currentIndex) {
      
      if (currentTime < dataSet.getMinimumTime() || currentTime > dataSet.getMaximumTime())
        throw new IllegalArgumentException();
      
      int[] time = dataSet.getTime();
      int index = 0;
      
      while (time[index] < currentTime)
        index++;
      
      @SuppressWarnings("unchecked")
      List<Edge> edges = edgeIndex.query((Envelope)edgeIndex.getRoot().getBounds());
      currentIndex = new STRtree();
      
      
      for (Edge edge : edges) {
        double[] all = (double[]) edge.property;
        Edge newEdge = graph.new Edge(edge.start, edge.end, edge.label, edge.geometry, edge.directed, all[index]);
        currentIndex.insert(newEdge.geometry.getEnvelopeInternal(), newEdge);
      }
      currentIndex.build();
    }
  }
  
  public double getCurrentTime() {
    return currentTime;
  }
  
  public void setCurrentTime(double currentTime) {
    
    if ((currentTime < dataSet.getMinimumTime()) || (currentTime > dataSet.getMaximumTime()))
      throw new IllegalArgumentException();
  
    this.currentTime = currentTime;
    buildCurrentIndex();
  }
  
  public double getMinimumTime() {
    return dataSet.getMinimumTime();
  }
  
  public double getMaximumTime() {
    return dataSet.getMaximumTime();
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
  
  @SuppressWarnings("unchecked")
  public List<Edge> getEdgesInEnv(Envelope bounds) {
    
    synchronized (currentIndex) {
      return (List<Edge>)currentIndex.query(bounds);
    }
  }
  
  public List<Edge> getVisibleEdges() {
    return getEdgesInEnv(currentEnvelope);
  }
}
