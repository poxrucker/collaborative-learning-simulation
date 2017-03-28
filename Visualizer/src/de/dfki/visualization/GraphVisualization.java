package de.dfki.visualization;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;

import de.dfki.visualization.Graph.Edge;
import de.dfki.visualization.Graph.Vertex;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.MapEvent;
import de.fhpotsdam.unfolding.events.PanMapEvent;
import de.fhpotsdam.unfolding.events.ZoomMapEvent;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.providers.OpenStreetMap;
import de.fhpotsdam.unfolding.providers.OpenStreetMap.OpenStreetMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import processing.core.PApplet;
import processing.core.PVector;

public final class GraphVisualization extends PApplet {

  /**
   * 
   */
  private static final long serialVersionUID = -1934157616453575554L;

  // Graph/StreetMap to visualize
  private Graph visualizationGraph;
  private Graph completeGraph;

  // Actual map to show
  UnfoldingMap map;

  // Display options
  boolean showEdges;
  boolean showTiles;
  boolean selectionMode;
  
  // Spatial indices for nodes an edges
  STRtree vertexIndex;
  STRtree edgeIndex;

  Envelope modelBounds;
  List<Vertex> visibleVertices;
  List<Edge> visibleEdges;
  List<Edge> selectedEdges;
  int drawOffset = 0;

  Edge closeEdge;
  boolean continueSelection;
  
  // DebugDisplay debugDisplay1;
  OpenStreetMapProvider provider;
  SimpleLinesMarker linesMarker;

  public GraphVisualization() {
    visibleVertices = new ArrayList<Vertex>();
    visibleEdges = new ArrayList<Edge>();
  }

  public void setup() {
    // Setup window
    size(1024, 768, OPENGL);

    // Setup display options
    showTiles = true;
    showEdges = false;
    selectionMode = false;

    // Load graph
    try {
      completeGraph = Graph.load(Paths.get("/Users/Andi/Documents/DFKI/VW simulation/data/world/trento_merged.world"));
      visualizationGraph = Graph.toUndirectedGraph(completeGraph);
      
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    // Build spatial indices from graph to show
    buildSpatialIndex();

    // Initialize map
    modelBounds = (Envelope) vertexIndex.getRoot().getBounds();
    Location trento = new Location(modelBounds.centre().x, modelBounds.centre().y);
    provider = new OpenStreetMap.OpenStreetMapProvider();
    map = new UnfoldingMap(this, "tiles", provider);
    map.zoomAndPanTo(13, trento);
    map.setZoomRange(13, 19);
    map.setPanningRestriction(trento, 4);
    frameRate(30);

    MapUtils.createDefaultEventDispatcher(this, map);
    
    selectedEdges = new ArrayList<Edge>();
  }

  public synchronized void buildSpatialIndex() {
    vertexIndex = new STRtree();
    edgeIndex = new STRtree();

    for (Vertex v : visualizationGraph.vertices.values()) {
      vertexIndex.insert(new Envelope(v.position), v);
    }

    for (Edge e : visualizationGraph.edges.values()) {
      if (e.geometry == null)
        return;

      edgeIndex.insert(e.geometry.getEnvelopeInternal(), e);
    }
    vertexIndex.build();
    edgeIndex.build();
  }

  @SuppressWarnings("unchecked")
  private synchronized void findVisibleElements() {
      visibleVertices = (List<Vertex>) vertexIndex.query(modelBounds);
      visibleEdges = (List<Edge>) edgeIndex.query(modelBounds);
  }
  
  public void mapChanged(MapEvent mapEvent) {

    if (mapEvent.getType().equals(ZoomMapEvent.TYPE_ZOOM)) {
      Location topLeftTemp = map.getTopLeftBorder();
      Coordinate topLeft = new Coordinate(topLeftTemp.x, topLeftTemp.y);
      
      Location bottomRightTemp = map.getBottomRightBorder();
      Coordinate bottomRight = new Coordinate(bottomRightTemp.x, bottomRightTemp.y);
      
      modelBounds = new Envelope(topLeft, bottomRight);
      
    } else if (mapEvent.getType().equals(PanMapEvent.TYPE_PAN)) {
      Location topLeftTemp = map.getTopLeftBorder();
      Coordinate topLeft = new Coordinate(topLeftTemp.x, topLeftTemp.y);
      
      Location bottomRightTemp = map.getBottomRightBorder();
      Coordinate bottomRight = new Coordinate(bottomRightTemp.x, bottomRightTemp.y);
      
      modelBounds = new Envelope(topLeft, bottomRight);

    } else {
      System.out.println(mapEvent.getType());
    }
  }

  @Override
  public void keyPressed() {

    if (key == 'e') {
      showEdges = !showEdges;

    } else if (key == 's') {
      selectionMode = !selectionMode;

    } else if (key == 't') {
      showTiles = !showTiles;

    }
  }

  @Override
  public void mouseClicked() {
    if (!showEdges || !selectionMode)
      return;
    
    if (closeEdge == null) {
      selectedEdges.clear();
      
    } else if (selectedEdges.contains(closeEdge)){
      selectedEdges.remove(closeEdge);
      
    } else {
      List<Edge> temp = new ArrayList<Edge>();
      
      Edge first = completeGraph.edges.get(closeEdge.start.label + ";;" + closeEdge.end.label);
      
      if (first != null)
        temp.add(first);
      
      Edge second = completeGraph.edges.get(closeEdge.end.label + ";;" + closeEdge.start.label);
      
      if (second != null)
        temp.add(second);
      
      Edge res = (Edge)JOptionPane.showInputDialog(this,
          "Select edge:", "Select Edge", JOptionPane.PLAIN_MESSAGE, null, temp.toArray(), temp.get(0));
      
      if (res != null) {
        selectedEdges.add(res);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void mouseMoved() {
    if (!showEdges)
      return;
    
    Location l = map.getLocation(mouseX, mouseY);
    Envelope mouseEnv = new Envelope(new Coordinate(l.x, l.y));
    mouseEnv.expandBy(0.00001);
    List<Edge> closeEdges = (List<Edge>) edgeIndex.query(mouseEnv);
    closeEdge = null;

    if (closeEdges.size() == 0)
      return;
    
    if (closeEdges.size() == 1) {
      closeEdge = closeEdges.get(0);
      return;
    }
    
    
    double minDist = Double.MAX_VALUE;

    for (Edge e : closeEdges) {
      double dist = distToEdge(mouseEnv.centre(), e);
      
      if (dist > 0.00005)
        continue;
      
      if (dist  < minDist) {
        closeEdge = e;
        minDist = dist;
      }
     
    }
  }
  
  private static double distToEdge(Coordinate c, Edge e) {
    double minDist = Double.MAX_VALUE;
    Coordinate[] coordinates = e.geometry.getCoordinates();
    
    for (int i = 0; i < coordinates.length - 1; i++) {
      Coordinate proj = projectToLine(coordinates[i], coordinates[i + 1], c);
      double dist = euclideanDistance(c, proj);
      
      if (dist < minDist)
        minDist = dist;
    }
    return minDist;
  }
  
  private static Coordinate projectToLine(Coordinate start, Coordinate end, Coordinate c) {
    Coordinate a = new Coordinate(end.x - start.x, end.y - start.y);
    Coordinate b = new Coordinate(c.x - start.x, c.y - start.y);
    
    double norm_a_square = a.x * a.x + a.y * a.y;
    double r = (a.x * b.x + a.y * b.y) / norm_a_square;
    
    if (r < 0.0) {
      return start;
      
    } else if (r >= 0.0 && r < 1.0) {
      return new Coordinate(start.x + r * a.x, start.y + r * a.y);
    
    } else {
      return end;
    }
  }
  
  private static double euclideanDistance(Coordinate first, Coordinate second) {
    double diffX = first.x - second.x;
    double diffY = first.y - second.y;
    return Math.sqrt(diffX * diffX + diffY * diffY);
  }
  
  public synchronized void draw() {
    smooth();
    map.mapDisplay.resize(width, height);

    if (showTiles)
      map.draw();
    else
      background(255);

    if (showEdges) {
      findVisibleElements();
      drawGraph();
      
      if (!finished)
        return;
    }
  }

  private void drawGraph() {
    noFill();
    
    if (showEdges) {
      drawSimpleEdges(visibleEdges);
    }
    
    if (selectionMode) {
      
      if (closeEdge != null) {
        drawCursorEdge(closeEdge);
      }     
      drawSelectedEdges(selectedEdges);
    }
    
  }
  
  private void drawSelectedEdges(List<Edge> edges) {
    pushStyle();
    strokeWeight(2);
    stroke(0, 255, 0);
    
    for (Edge e : edges) {
      drawDirectedEdge(e);
    }
    popStyle();
  }
  
  private void drawCursorEdge(Edge edge) {
    if (selectedEdges.contains(edge))
      return;
    
    pushStyle();
    strokeWeight(2);
    stroke(255, 0, 0);
    drawDirectedEdge(edge);
    
    ScreenPosition s = map.getScreenPosition(new Location(edge.start.position.x, edge.start.position.y));
    ScreenPosition e = map.getScreenPosition(new Location(edge.end.position.x, edge.end.position.y));
    
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
    text(lowerText, lower.x, lower.y);
    text(upperText, upper.x, upper.y);
    popStyle();
  }
  
  private void drawArrow(float x0, float y0, float x1, float y1, float size) {
    PVector d = new PVector(x1 - x0, y1 - y0);
    d.normalize();   
    float coeff = 1.5f;
    float angle = atan2(d.y, d.x);

    pushMatrix();
    translate(x0, y0);
    rotate(angle + PI);
    strokeCap(ROUND);
    triangle(-size*coeff, -size, -size*coeff, size, 0, 0);
    popMatrix();    
  }
  
  private void drawSimpleEdges(List<Edge> edges) {
    pushStyle();
    strokeWeight(1);
    stroke(150, 150, 150);
    
    for (Edge e : edges) {
      drawSimpleEdge(e);
    }
    popStyle();
  }
  
  private void drawDirectedEdge(Edge edge) {
    float headx0 = -1.0f, heady0 = -1.0f, headx1 = -1.0f, heady1 = -1.0f;
    float tailx0 = -1.0f, taily0 = -1.0f, tailx1 = -1.0f, taily1 = -1.0f;
    Coordinate[] coords = edge.geometry.getCoordinates();

    for (int i = 0; i < coords.length - 1; i++) {
      ScreenPosition pos = map.getScreenPosition(new Location(coords[i].x, coords[i].y));
      ScreenPosition next = map.getScreenPosition(new Location(coords[i + 1].x, coords[i + 1].y));
      line(pos.x, pos.y, next.x, next.y);

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
    fill(255, 0, 0);
    drawArrow(tailx0, taily0, tailx1, taily1, 2);
    
    if (!edge.directed)
      drawArrow(headx0, heady0, headx1, heady1, 2);   
  }
  
  private void drawSimpleEdge(Edge e) {
    if (e.geometry == null)
        return;
    
    Coordinate[] coords = e.geometry.getCoordinates();
   
    for (int i = 0; i < coords.length - 1; i++) {
      ScreenPosition pos = map.getScreenPosition(new Location(coords[i].x, coords[i].y));
      ScreenPosition next = map.getScreenPosition(new Location(coords[i + 1].x, coords[i + 1].y));
      line(pos.x, pos.y, next.x, next.y);
    }
  }
}
