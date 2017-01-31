package allow.simulator.world;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;

import allow.simulator.core.Context;
import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;

public final class StreetMap extends World implements Observer {
	
	private static final class StreetComparator implements Comparator<Street> {
		
		@Override
		public int compare(Street o1, Street o2) {
			if (o1.getVehicleLengthRatio() > o2.getVehicleLengthRatio()) {
				return -1;
			}
			
			if (o1.getVehicleLengthRatio() == o2.getVehicleLengthRatio()) {
				return 0;
			}
			return 1;
		}
		
	}
	
	// Encodes network structure of StreetMap.
	private Graph<StreetNode, Street> map;
	private Object2ObjectOpenHashMap<String, StreetNode> nodesReduced;	
	private Object2ObjectOpenHashMap<String, Street> streets;
	private Object2ObjectOpenHashMap<String, StreetNode> nodes;
	private Object2ObjectOpenHashMap<String, StreetNode> posNodes;
	
	// Set of street segments to update after each time step.
	private ObjectOpenHashSet<Street> streetsToUpdate;
	private Queue<Street> busiestStreets;
	
	public StreetMap(Path path) throws IOException {
		map = new DirectedSparseMultigraph<StreetNode, Street>(); 
		streets = new Object2ObjectOpenHashMap<String, Street>();
		nodesReduced = new Object2ObjectOpenHashMap<String, StreetNode>();
		posNodes = new Object2ObjectOpenHashMap<String, StreetNode>();
		busiestStreets = new LinkedList<Street>();
		loadStreetNetwork(path);
	}
	
	private void loadStreetNetwork(Path mapFile) throws IOException {
		List<String> lines = Files.readAllLines(mapFile);
		int offset = 0;
		dimensions[0] = 180.0;
		dimensions[1] = -180.0;
		dimensions[2] = 90.0;
		dimensions[3] = -90.0;

		// Read nodes.
		String headerNodes = lines.get(offset++);
		String tokens[] = headerNodes.split(" ");
		int numberOfNodes = Integer.parseInt(tokens[1]);
		nodes = new Object2ObjectOpenHashMap<String, StreetNode>();
		long nodeIds = 0;
			
		for (int i = 0; i < numberOfNodes; i++) {
			String temp = lines.get(offset++);
			tokens = temp.split(";;");
			Coordinate c = new Coordinate(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]));
			if (c.x < dimensions[0]) dimensions[0] = c.x;
			if (c.x > dimensions[1]) dimensions[1] = c.x;
			if (c.y < dimensions[2]) dimensions[2] = c.y;
			if (c.y > dimensions[3]) dimensions[3] = c.y;
			StreetNode n = new StreetNode(nodeIds++, tokens[0], c);
			// map.addVertex(n);
			nodes.put(tokens[0], n);
			posNodes.put(c.y + "," + c.x, n);
		}
		offset++;

		// Read links.
		String headerLinks = lines.get(offset++);
		tokens = headerLinks.split(" ");
		int numberOfLinks = Integer.parseInt(tokens[1]);
		long linkIds = 1;
		double mphTomps = 1.609 / 3.6;
		
		for (int i = 0; i < numberOfLinks; i++) {
			String temp = lines.get(offset++);
			tokens = temp.split(";;");
			String idStart = tokens[1];
			String idEnd = tokens[2];
			String name = tokens[3];
			double speedLimit = Double.parseDouble(tokens[4]) * mphTomps;
			String subSegs[] = tokens[5].split(" ");
				
			StreetNode source = nodes.get(idStart);
			StreetNode dest = nodes.get(idEnd);
			nodesReduced.put(source.getLabel(), source);
			nodesReduced.put(dest.getLabel(), dest);
			
			// Add a new street from the loaded segments.						
			List<StreetSegment> segments = new ArrayList<StreetSegment>();
			List<StreetSegment> segmentsRev = new ArrayList<StreetSegment>();
			
			for (int j = 0; j < subSegs.length - 1; j++) {
				StreetNode start = nodes.get(subSegs[j]);
				StreetNode end = nodes.get(subSegs[j + 1]);
				StreetSegment seg = new StreetSegment(linkIds++, start, end, speedLimit, Geometry.haversineDistance(start.getPosition(), end.getPosition()));
				segments.add(seg);
				//map.addEdge(seg, start, end, EdgeType.DIRECTED);
				
				// Add reversed segment for walking.
				StreetSegment segRev = new StreetSegment(linkIds++, end, start, speedLimit, seg.getLength());
				segmentsRev.add(segRev);
				//if (end.getLabel().startsWith("split"))
				//map.addEdge(segRev, end, start, EdgeType.DIRECTED); 
			}
			Street s = new Street(linkIds++, name, segments);
			
			//if (!streets.containsKey(source.getLabel() + ";;" + dest.getLabel())) {
				s.addObserver(this);
				streets.put(source.getLabel() + ";;" + dest.getLabel(), s);
				map.addEdge(s, segments.get(0).getStartingNode(), segments.get(segments.size() - 1).getEndingNode());
			//}

			Collections.reverse(segmentsRev);
			Street sRev = new Street(linkIds++, name, segmentsRev);
			
			//if (!streets.containsKey(dest.getLabel() + ";;" + source.getLabel())) {
				sRev.addObserver(this);
				streets.put(dest.getLabel() + ";;" + source.getLabel(), sRev);
				map.addEdge(sRev, segmentsRev.get(0).getStartingNode(), segmentsRev.get(segmentsRev.size() - 1).getEndingNode());
			//}
		}
		streetsToUpdate = new ObjectOpenHashSet<Street>(streets.size() / 2);
		System.out.println("|V|: " + map.getVertexCount() + ", |E|: " + map.getEdgeCount() + " " 
		+ dimensions[0] + " " + dimensions[1] + " " + dimensions[2] + " " + dimensions[3]);
	}
	
	/**
	 * Returns the nodes, i.e. beginning and end points of street (segments) 
	 * forming the street graph together with the streets.
	 * 
	 * @return List of street nodes of the street graph.
	 */
	/*public List<StreetNode> getStreetNodes() {
		return temp;
	}*/
	
	public Collection<StreetNode> getStreetNodes() {
		return Collections.unmodifiableCollection(map.getVertices());
	}
	
	/**
	 * Returns the street segments forming the street graph together with the
	 * set of street nodes.
	 * 
	 * @return Set of street segments of the street graph.
	 */
	/*public Collection<StreetSegment> getStreetSegments() {
		return map.getEdges();
	}*/
	
	/*public Pair<StreetNode, StreetNode> getIncidentNodes(Street seg) {
		edu.uci.ics.jung.graph.util.Pair<StreetNode> nodes = map.getEndpoints(seg);
		return new Pair<StreetNode, StreetNode>(nodes.getFirst(), nodes.getSecond());
	}*/

	public Collection<Street> getIncidentEdges(StreetNode node) {
		return map.getIncidentEdges(node);
	}
	
	/*public Collection<StreetSegment> getOutGoingSegments(StreetNode source) {
		return map.getOutEdges(source);
	}
	
	public StreetNode getSource(StreetSegment seg) {
		return map.getSource(seg);
	}
	
	public StreetNode getDestination(StreetSegment seg) {
		return map.getDest(seg);
	}*/
	
	/**
	 * Updates all street segments which 
	 */
	@Override
	public boolean update(Context context) {
		boolean changed = false;
		// Reset busiest streets queue. 
		busiestStreets.clear();
		
		for (Street toUpdate : streetsToUpdate) {
			toUpdate.updatePossibleSpeedOnSegments();
			
			/*if (toUpdate.getNumberOfVehicles() > 0)
				busiestStreets.add(toUpdate);*/
			changed = true;
		}
		streetsToUpdate.clear();
		return changed || super.update(context);
	}
	
	public List<Street> getNBusiestStreets(int n) {
		int actual = Math.min(n, busiestStreets.size());
		List<Street> ret = new ArrayList<Street>(actual);
		Street temp[] = busiestStreets.toArray(new Street[busiestStreets.size()]);
		Arrays.sort(temp, new StreetComparator());
		
		for (int i = 0; i < actual; i++) {
			System.out.print(temp[i].getVehicleLengthRatio() + ", ");
			ret.add(temp[i]);
		}
		System.out.println();
		return ret;
	}
	
	/**
	 * Returns a street given its start and end node.
	 * 
	 * @param first Start node of street.
	 * @param second End node of street.
	 * @return
	 */
	public Street getStreet(String first, String second) {
		return streets.get(first + ";;" + second);
	}

	public Collection<Street> getStreets() {
		return Collections.unmodifiableCollection(streets.values());
	}
	public Street getStreetReduced(StreetNode first, StreetNode second) {
		return map.findEdge(first, second);
	}
	
	public StreetNode getStreetNode(String label) {
		return nodes.get(label);
	}
	
	public StreetNode getStreetNodeReduced(String label) {
		return nodesReduced.get(label);
	}
	
	public StreetNode getStreetNodeFromPosition(String posString) {
		return posNodes.get(posString);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		streetsToUpdate.add((Street) o);
	}
}
