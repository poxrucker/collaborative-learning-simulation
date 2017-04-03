package de.dfki.visualization;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import de.dfki.data.Graph;
import de.dfki.data.Graph.Edge;
import de.dfki.data.Graph.Vertex;
import de.dfki.renderer.DirectedEdgeRenderer;
import de.dfki.renderer.GraphRenderer;
import de.dfki.renderer.SimpleVertexRenderer;
import de.dfki.util.Util;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.MapEvent;
import de.fhpotsdam.unfolding.events.PanMapEvent;
import de.fhpotsdam.unfolding.events.ZoomMapEvent;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.OpenStreetMap;
import de.fhpotsdam.unfolding.utils.MapUtils;
import processing.core.PApplet;

public final class GraphVisualization extends PApplet {

  private static final long serialVersionUID = -1934157616453575554L;
  
  // Wrapper a graph to display
  private GraphDisplayAdapter graphDisplay;
  
  // Map tiles to show
  private UnfoldingMap map;

  // Display state
  private boolean showEdges;
  private boolean showVertices;
  private boolean showTiles;
  private int previousWidth;
  private int previousHeight;

  // Selection modes
  private boolean edgeSelectionMode;
  private boolean vertexSelectionMode;
  //private VertexSelection vertexSelection;

  List<Edge> selectedEdges;
  Vertex lastSelectedVertex;

  private Edge cursorEdge;
  private Vertex cursorVertex;
  
  // Renderer for drawing
  private GraphRenderer graphRenderer;
  private SimpleVertexRenderer cursorVertexRenderer;
  private SimpleVertexRenderer lastSelectedVertexRenderer;
  private DirectedEdgeRenderer cursorEdgeRenderer;
  private DirectedEdgeRenderer cursorEdgeRendererWithoutLabels;
  private DirectedEdgeRenderer selectedEdgesRenderer;
  
  public void setup() {
    // Setup window
    size(getSize().width, getSize().height, OPENGL);

    // Setup display options
    showTiles = false;
    showEdges = true;
    showVertices = false;
    edgeSelectionMode = false;
    vertexSelectionMode = false;

    // Load graph
    try {
      Graph graph = Graph.load(Paths.get("/Users/Andi/Documents/DFKI/VW simulation/data/world/trento_merged.world"));
      graphDisplay = new GraphDisplayAdapter(graph);
      
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    
    // Initialize map
    map = new UnfoldingMap(this, "tiles", new OpenStreetMap.OpenStreetMapProvider());
    map.mapDisplay.resize(width, height);
    
    Envelope baseEnv = graphDisplay.getBaseEnvelope();
    Location trento = new Location(baseEnv.centre().x, baseEnv.centre().y);
    map.zoomAndPanTo(13, trento);
    map.setZoomRange(13, 19);
    map.setPanningRestriction(trento, 4);
    frameRate(60);

    MapUtils.createDefaultEventDispatcher(this, map);

    selectedEdges = new ArrayList<Edge>();
    GeoToScreenPositionConverter conv = new GeoToScreenPositionConverter(map);
    graphRenderer = new GraphRenderer(conv, g);
    cursorVertexRenderer = new SimpleVertexRenderer(conv, color(255, 0, 0), 2, 2, true);
    lastSelectedVertexRenderer = new SimpleVertexRenderer(conv, color(0, 255, 0), 2, 2, true);
    cursorEdgeRenderer = new DirectedEdgeRenderer(conv, color(255, 0, 0), 2, 3.0f, true, false);
    cursorEdgeRendererWithoutLabels = new DirectedEdgeRenderer(conv, color(255, 0, 0), 2, 3.0f, false, true);
    selectedEdgesRenderer = new DirectedEdgeRenderer(conv, color(0, 255, 0), 2, 0, false, false);
    
    //vertexSelection = new VertexSelection(graphDisplay);
  }

  public void mapChanged(MapEvent mapEvent) {
    String e = mapEvent.getType();
    
    if (!e.equals(ZoomMapEvent.TYPE_ZOOM) && !e.equals(PanMapEvent.TYPE_PAN))
      return;
    graphDisplay.setVisibleArea(getVisibleArea());
  }

  @Override
  public void keyPressed() {

    if (key == 'e') {
      showEdges = !showEdges;
      graphRenderer.setDrawEdges(showEdges);
      
    } else if (key == 'v') {
      showVertices = !showVertices;
      graphRenderer.setDrawVertices(showVertices);
      
    } else if (key == 's') {
      edgeSelectionMode = !edgeSelectionMode;
      
      if (edgeSelectionMode)
        updateEdgeSelectorState();
      
      if (vertexSelectionMode)
        vertexSelectionMode = false;

    } else if (key == 'b') {
      vertexSelectionMode = !vertexSelectionMode;

      if (vertexSelectionMode)
        updateVertexSelectorState();
      
      if (vertexSelectionMode && selectedEdges.size() > 0)
        lastSelectedVertex = selectedEdges.get(selectedEdges.size() - 1).end;
      
      if (edgeSelectionMode)
        edgeSelectionMode = false;

    } else if (key == 't') {
      showTiles = !showTiles;

    }
  }

  @Override
  public void mouseClicked() {
    if (!showEdges)
      return;

    if (edgeSelectionMode)
      handleMouseClickedEdgeSelection();

    else if (vertexSelectionMode)
      handleMouseClickedVertexSelection();
  }

  private void handleMouseClickedVertexSelection() {
    
    if (cursorVertex == null) {
      selectedEdges.clear();
      lastSelectedVertex = null;
      return;

    } else if (lastSelectedVertex == null) {
      lastSelectedVertex = cursorVertex;

    } else if (cursorVertex == lastSelectedVertex) {
      lastSelectedVertex = null;

    } else {
      Edge candidate = graphDisplay.getGraph().edges.get(lastSelectedVertex.label + ";;" + cursorVertex.label);
      lastSelectedVertex = cursorVertex;

      if (candidate == null || selectedEdges.contains(candidate))
        return;

      selectedEdges.add(candidate);
    }
  }

  private void handleMouseClickedEdgeSelection() {

    if (cursorEdge == null) {
      selectedEdges.clear();

    } else if (selectedEdges.contains(cursorEdge)) {
      selectedEdges.remove(cursorEdge);
      lastSelectedVertex = (selectedEdges.size() > 0 ? selectedEdges.get(selectedEdges.size() - 1).end : null);

    } else {
      List<Edge> temp = new ArrayList<Edge>();

      Edge first = graphDisplay.getGraph().edges.get(cursorEdge.start.label + ";;" + cursorEdge.end.label);

      if (first != null)
        temp.add(first);

      Edge second = graphDisplay.getGraph().edges.get(cursorEdge.end.label + ";;" + cursorEdge.start.label);

      if (second != null)
        temp.add(second);

      Edge res = (Edge) JOptionPane.showInputDialog(this, "Select edge:", "Select Edge", JOptionPane.PLAIN_MESSAGE, null, temp.toArray(),
          temp.get(0));

      if (res != null) {
        selectedEdges.add(res);
      }
    }
  }

  @Override
  public void mouseMoved() {
    if (!showEdges)
      return;
    
    // Determine close edges
    if (edgeSelectionMode)
      updateEdgeSelectorState();

    else if (vertexSelectionMode) {
      updateVertexSelectorState();
    }

  }
  
  private void updateEdgeSelectorState() {
    // Create mouse envelope
    Location l = map.getLocation(mouseX, mouseY);
    Envelope mouseEnv = new Envelope(new Coordinate(l.x, l.y));
    mouseEnv.expandBy(0.0001);
    cursorEdge = getClosestEdge(mouseEnv);
  }
  
  private void updateVertexSelectorState() {
    // Create mouse envelope
    Location l = map.getLocation(mouseX, mouseY);
    Envelope mouseEnv = new Envelope(new Coordinate(l.x, l.y));
    mouseEnv.expandBy(0.0001);
    
    // Determine close vertex
    cursorVertex = getClosestVertex(mouseEnv);

    if (cursorVertex == null || lastSelectedVertex == null) {
      cursorEdge = null;
      return;
    }
    cursorEdge = graphDisplay.getGraph().edges.get(lastSelectedVertex.label + ";;" + cursorVertex.label);
  }
  
  private Vertex getClosestVertex(Envelope mouseEnv) {
    List<Vertex> closeVertices = graphDisplay.getVerticesInEnv(mouseEnv);

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

  private Edge getClosestEdge(Envelope mouseEnv) {
    List<Edge> closeEdges = graphDisplay.getEdgesInEnv(mouseEnv);

    if (closeEdges.size() == 0)
      return null;

    if (closeEdges.size() == 1)
      return closeEdges.get(0);

    double minDist = Double.MAX_VALUE;
    Edge closestEdge = null;

    for (Edge e : closeEdges) {
      double dist = Util.distToEdge(mouseEnv.centre(), e);

      if (dist > 0.00005)
        continue;

      if (dist < minDist) {
        closestEdge = e;
        minDist = dist;
      }
    }
    return closestEdge;
  }

  public synchronized void draw() {
    smooth();

    if (width != previousWidth || height != previousHeight) {
      map.mapDisplay.resize(width, height);
      previousWidth = width;
      previousHeight = height;
    }

    if (showTiles)
      map.draw();
    else
      background(255);

    if (showEdges) {
      graphRenderer.draw(graphDisplay, g);

      if (edgeSelectionMode) {

        if (cursorEdge != null)
          cursorEdgeRenderer.draw(cursorEdge, g);

      }

      if (vertexSelectionMode) {

        if (cursorVertex != null)
          cursorVertexRenderer.draw(cursorVertex, g);

        if (lastSelectedVertex != null)
          lastSelectedVertexRenderer.draw(lastSelectedVertex, g);

        if (cursorEdge != null && !selectedEdges.contains(cursorEdge))
          cursorEdgeRendererWithoutLabels.draw(cursorEdge, g);
      }

      if (edgeSelectionMode || vertexSelectionMode)
        drawSelectedEdges(selectedEdges);
    }
  }

  private void drawSelectedEdges(List<Edge> edges) {
    pushStyle();
    strokeWeight(2);
    stroke(0, 255, 0);

    for (Edge e : edges) {
      selectedEdgesRenderer.draw(e, g);
    }
    popStyle();
  }

  
  
  private Envelope getVisibleArea() {
    Location topLeftTemp = map.getTopLeftBorder();
    Coordinate topLeft = new Coordinate(topLeftTemp.x, topLeftTemp.y);

    Location bottomRightTemp = map.getBottomRightBorder();
    Coordinate bottomRight = new Coordinate(bottomRightTemp.x, bottomRightTemp.y);
    return new Envelope(topLeft, bottomRight);
  }
}
