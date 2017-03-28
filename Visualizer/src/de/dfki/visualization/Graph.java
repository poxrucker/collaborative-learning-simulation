package de.dfki.visualization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class Graph {
  
  public class Vertex {
    final String label;
    final Coordinate position;
    
    public Vertex(String label, Coordinate position) {
      this.label = label;
      this.position = position;
    }
  }
  
  public class Edge {
    final Vertex start;
    final Vertex end;
    final String label;
    boolean directed;
    final LineString geometry;
    
    public Edge(Vertex start, Vertex end, String label, LineString geometry, boolean directed) {
      this.start = start;
      this.end = end;
      this.label = label;
      this.geometry = geometry;
      this.directed = directed;
    }
    
    public String toString() {
      return label + " (" + start.label + "->" + end.label + ")";
    }
  }
  
  Map<String, Vertex> vertices;
  Map<String, Edge> edges;
  
  Graph() {
    vertices = new HashMap<String, Vertex>();
    edges = new HashMap<String, Edge>();
  }
  
  public void addEdge(Edge edge) {
    edges.put(edge.start.label + ";;" + edge.end.label, edge);
    
    if (!vertices.containsKey(edge.start.label))
      vertices.put(edge.start.label, edge.start);
    
    if (!vertices.containsKey(edge.end.label))
      vertices.put(edge.end.label, edge.end);
  }
  
  public static Graph toUndirectedGraph(Graph graph) {
    Graph ret = new Graph();
    Set<String> added = new HashSet<String>();
    Collection<Edge> edges =  graph.edges.values();
    
    for (Edge e : edges) {
      
      if (added.contains(e.end.label + ";;" + e.start.label)) {
        Edge temp = ret.edges.get(e.end.label + ";;" + e.start.label);
        temp.directed = false;
        continue;
      }
      Edge copy = ret.new Edge(e.start, e.end, e.label, e.geometry, true);
      added.add(e.start.label + ";;" + e.end.label);
      ret.addEdge(copy);
    }
    return ret;
  }
  
  public static Graph load(Path path) throws IOException {
    Graph ret = new Graph();
    List<String> lines = Files.readAllLines(path);
    int offset = 0;

    // Read nodes.
    String headerNodes = lines.get(offset++);
    String tokens[] = headerNodes.split(" ");
    int numberOfNodes = Integer.parseInt(tokens[1]);
    
    Map<String, Vertex> vertexBuffer = new HashMap<String, Vertex>(numberOfNodes);
      
    for (int i = 0; i < numberOfNodes; i++) {
      String temp = lines.get(offset++);
      tokens = temp.split(";;");
      Coordinate c = new Coordinate(Double.parseDouble(tokens[2]), Double.parseDouble(tokens[1]));
      vertexBuffer.put(tokens[0], ret.new Vertex(tokens[0], c));
    }
    offset++;
    
    // Create and add edges
    GeometryFactory geometryFactory = new GeometryFactory();
    String headerLinks = lines.get(offset++);
    tokens = headerLinks.split(" ");
    int numberOfLinks = Integer.parseInt(tokens[1]);
    
    for (int i = 0; i < numberOfLinks; i++) {
      String temp = lines.get(offset++);
      tokens = temp.split(";;");
      String idStart = tokens[1];
      String idEnd = tokens[2];
      String name = tokens[3];
      String subSegs[] = tokens[5].split(" ");
        
      Vertex source = vertexBuffer.get(idStart);
      Vertex dest = vertexBuffer.get(idEnd);
    
      // Parse LineString    
      Coordinate[] points = new Coordinate[subSegs.length];
      
      for (int j = 0; j < subSegs.length; j++) {
        Vertex v = vertexBuffer.get(subSegs[j]);
        
        if (v == null)
          throw new IllegalStateException();
        
        points[j] = new Coordinate(v.position.x, v.position.y);
      }
      ret.addEdge(ret.new Edge(source, dest, name, geometryFactory.createLineString(points), true));
    }
    return ret;
  }
  
}
