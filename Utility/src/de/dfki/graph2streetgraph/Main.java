package de.dfki.graph2streetgraph;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import allow.simulator.util.Geometry;
import allow.simulator.world.Street;
import allow.simulator.world.StreetNode;
import allow.simulator.world.StreetSegment;

public class Main {

	public static void main(String args[]) throws ClassNotFoundException, IOException {
		// Load street graphs
		Map<String, StreetNode> trento_node1 = new HashMap<String, StreetNode>();
		Map<String, Street> trento_street1 = new HashMap<String, Street>();
		loadGraph(Paths.get("/Users/Andi/Documents/DFKI/Allow Ensembles/Repository/repos/Software/DFKI Simulator/NetLogo/data/world/trento.world"), trento_node1, trento_street1);
		
		Map<String, StreetNode> trento_node2 = new HashMap<String, StreetNode>();
		Map<String, Street> trento_street2 = new HashMap<String, Street>();
		loadGraph(Paths.get("/Users/Andi/Documents/DFKI/Allow Ensembles/Repository/repos/Software/DFKI Simulator/NetLogo/data/world/trento2.world"), trento_node2, trento_street2);
		
		//mergeStreetMaps(trento_node1, trento_street1, trento_node2, trento_street2);
		
		mergeStreetMaps(trento_node2, trento_street2, trento_node1, trento_street1);
		
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Users/Andi/Documents/trento_merged.world")));
		
		wr.write("NODES " + trento_node1.size() + "\n");
		
		for (StreetNode node : trento_node1.values()) {
			wr.write(node.getLabel() + ";;" + node.getPosition().x + ";;" + node.getPosition().y + "\n");
		}
		wr.write("\n");
		
		wr.write("LINKS " + trento_street1.size() + "\n");
		
		for (Street s : trento_street1.values()) {
			wr.write(s.getStartingNode().getLabel() + "->" + s.getEndNode().getLabel() + ";;");
			wr.write(s.getStartingNode().getLabel() + ";;");
			wr.write(s.getEndNode().getLabel() + ";;");
			wr.write(s.getName() + ";;");
			wr.write(s.getSubSegments().get(0).getMaxSpeed() + ";;");
			
			for (int i = 0; i < s.getSubSegments().size() - 1; i++) {
				wr.write(s.getSubSegments().get(i).getStartingNode().getLabel() + " ");
			}
			wr.write(s.getSubSegments().get(s.getSubSegments().size() - 1).getStartingNode().getLabel() + " " + s.getSubSegments().get(s.getSubSegments().size() - 1).getEndingNode().getLabel() + "\n");
			//wr.write(s.segs[s.segs.length - 1] + "\n");
		}
		wr.close();

	}
		//Graph g = Graph.load(new File("/Users/Andi/otprepos/Graph/Graph.obj"), LoadLevel.FULL);
		/*Graph g = Graph.load(new File("/Users/Andi/Documents/DFKI/OpenTripPlanner/copy/Graph.obj"), LoadLevel.FULL);
		Map<String, StreetNode> nodes = new HashMap<String, StreetNode>();
		Map<String, Segment> segments = new HashMap<String, Segment>();
		
		int id = 0;
		
		System.out.println("Number of nodes: " + g.getVertices().size());
		System.out.println("Number of edges: " + g.getEdges().size());
		
		for (Edge e : g.getEdges()) {

			if (e instanceof StreetEdge) {
				StreetEdge link = (StreetEdge) e;
				StreetTraversalPermission p = link.getPermission();
				if ((p == StreetTraversalPermission.ALL)
						|| (p == StreetTraversalPermission.BICYCLE)
						|| (p == StreetTraversalPermission.BICYCLE_AND_CAR)
						|| (p == StreetTraversalPermission.CAR)
						|| (p == StreetTraversalPermission.PEDESTRIAN)
						|| (p == StreetTraversalPermission.PEDESTRIAN_AND_BICYCLE)
						|| (p == StreetTraversalPermission.PEDESTRIAN_AND_BICYCLE_AND_CAR)
						|| (p == StreetTraversalPermission.PEDESTRIAN_AND_CAR)
						|| (p == StreetTraversalPermission.PEDESTRIAN_AND_BICYCLE)) {
					
					Vertex v1 = link.getFromVertex();
					StreetNode n1;
					
					if (!nodes.containsKey(v1.getLabel())) {
						n1 = new StreetNode(id++, v1.getLabel().replaceAll("\\s",""), new allow.simulator.util.Coordinate(v1.getLon(), v1.getLat()));
						nodes.put(v1.getLabel(), n1);
					} else {
						n1 = nodes.get(v1.getLabel());
					}
				
					Vertex v2 = link.getToVertex();
					StreetNode n2;
				
					if (!nodes.containsKey(v2.getLabel())) {
						n2 = new StreetNode(id++, v2.getLabel().replaceAll("\\s",""), new allow.simulator.util.Coordinate(v2.getLon(), v2.getLat()));
						nodes.put(v2.getLabel(), n2);
					} else {
						n2 = nodes.get(v2.getLabel());
					}
				
					Segment s = new Segment();
					s.id = n1.getLabel() + "->" + n2.getLabel();
					s.fromLabel = n1.getLabel();
					s.tolabel = n2.getLabel();
					LineString segs = link.getGeometry();
					Coordinate coord[] = segs.getCoordinates();
					s.segs = new String[coord.length];
					s.segs[0] = n1.getLabel();
					
					for (int i = 1; i < coord.length - 1; i++) {
						StreetNode temp = new StreetNode(id++, n1.getLabel() + "->" + n2.getLabel() + "->" + i, new allow.simulator.util.Coordinate(coord[i].x, coord[i].y));
						nodes.put(temp.getLabel(), temp);
						s.segs[i] = temp.getLabel();
					}
					s.segs[coord.length - 1] = n2.getLabel();
					s.name = link.getName();
					s.speed = link.getCarSpeed();
					s.length = link.getDistance();
					segments.put(n1.getLabel() + "->" + n2.getLabel(), s);
				//}
			}
		}

		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Users/Andi/Documents/trento2.world")));
		
		wr.write("NODES " + nodes.size() + "\n");
		
		for (StreetNode node : nodes.values()) {
			wr.write(node.getLabel() + ";;" + node.getPosition().x + ";;" + node.getPosition().y + "\n");
		}
		wr.write("\n");
		
		wr.write("LINKS " + segments.size() + "\n");
		
		for (Segment s : segments.values()) {
			wr.write(s.id + ";;");
			wr.write(s.fromLabel + ";;");
			wr.write(s.tolabel + ";;");
			wr.write(s.name + ";;");
			wr.write(s.speed + ";;");
			
			for (int i = 0; i < s.segs.length - 1; i++) {
				wr.write(s.segs[i] + " ");
			}
			wr.write(s.segs[s.segs.length - 1] + "\n");
		}
		wr.close();*/
		/*Graph g = Graph.load(new File("/var/otp/graphs/Graph.obj"), LoadLevel.FULL);
		Map<String, StreetNode> nodes = new HashMap<String, StreetNode>();
		Map<Long, Segment> segments = new HashMap<Long, Segment>();
		
		double minX = 10.9824643;
		double maxX = 11.2052265;
		double minY = 45.9434744;
		double maxY = 46.1721383;
		
		int id = 0;
		int segId = 0;
		
		System.out.println("Number of nodes: " + g.getVertices().size());
		System.out.println("Number of edges: " + g.getEdges().size());
		
		for (Edge e : g.getEdges()) {

			if (e instanceof StreetEdge) {
				StreetEdge link = (StreetEdge) e;
				StreetTraversalPermission p = link.getPermission();
				
				if ((p == StreetTraversalPermission.ALL)
						//|| (p == StreetTraversalPermission.ALL_DRIVING)
						//|| (p == StreetTraversalPermission.BICYCLE_AND_DRIVING)
						|| (p == StreetTraversalPermission.BICYCLE_AND_CAR)
						|| (p == StreetTraversalPermission.CAR)
						|| (p == StreetTraversalPermission.PEDESTRIAN_AND_BICYCLE_AND_CAR)
						|| (p == StreetTraversalPermission.PEDESTRIAN_AND_CAR)
						//|| (p == StreetTraversalPermission.PEDESTRIAN_AND_DRIVING
						) {
					
					Vertex v1 = link.getFromVertex();
					StreetNode n1;
					
					if (!nodes.containsKey(v1.getLabel())) {
						n1 = new StreetNode(id++, v1.getLabel(), new allow.simulator.util.Coordinate(v1.getLon(), v1.getLat()));
						nodes.put(v1.getLabel(), n1);
					} else {
						n1 = nodes.get(v1.getLabel());
					}
				
					Vertex v2 = link.getToVertex();
					StreetNode n2;
				
					if (!nodes.containsKey(v2.getLabel())) {
						n2 = new StreetNode(id++, v2.getLabel(), new allow.simulator.util.Coordinate(v2.getLon(), v2.getLat()));
						nodes.put(v2.getLabel(), n2);
					} else {
						n2 = nodes.get(v2.getLabel());
					}
					Segment s = new Segment();
					s.id = segId++;
					s.fromId = n1.getId();
					s.fromLabel = n1.getLabel();
					s.toId = n2.getId();
					s.tolabel = n2.getLabel();
					LineString segs = link.getGeometry();
					Coordinate coord[] = segs.getCoordinates();
					s.segs = coord;
					s.name = link.getName();
					s.speed = link.getCarSpeed();
					s.length = link.getLength();
					segments.put(s.id, s);
				}
			}
		}

		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("matching_graph")));
		wr.write("ENVELOPE " + minX + " " + maxX + " " + minY + " " + maxY + "\n\n");
		
		wr.write("NODES " + nodes.size() + "\n");
		
		for (StreetNode node : nodes.values()) {
			wr.write(node.getId() + ";;" + node.getLabel() + ";;" + node.getPosition().x + ";;" + node.getPosition().y + "\n");
		}
		wr.write("\n");
		
		wr.write("LINKS " + segments.size() + "\n");
		
		for (Segment s : segments.values()) {
			wr.write(s.id + ";;");
			wr.write(s.fromId + ";;");
			wr.write(s.toId + ";;");
			wr.write(s.name + ";;");
			wr.write(s.speed + ";;");
			
			for (int i = 0; i < s.segs.length - 1; i++) {
				wr.write(s.segs[i].x + " " + s.segs[i].y + " ");
			}
			wr.write(s.segs[s.segs.length - 1].x + " " + s.segs[s.segs.length - 1].y + "\n");
		}
		wr.close();
	}*/

	private static void loadGraph(Path path, Map<String, StreetNode> nodes, Map<String, Street> streets) throws IOException {
		List<String> lines = Files.readAllLines(path);
		int offset = 0;
		
		// Read nodes.
		String headerNodes = lines.get(offset++);
		String tokens[] = headerNodes.split(" ");
		int numberOfNodes = Integer.parseInt(tokens[1]);
		// nodes = new HashMap<String, StreetNode>();
		long nodeIds = 0;
			
		for (int i = 0; i < numberOfNodes; i++) {
			String temp = lines.get(offset++);
			tokens = temp.split(";;");
			allow.simulator.util.Coordinate c = new allow.simulator.util.Coordinate(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]));
			StreetNode n = new StreetNode(nodeIds++, tokens[0], c);
			nodes.put(tokens[0], n);
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
			double speedLimit = Double.parseDouble(tokens[4]);
			String subSegs[] = tokens[5].split(" ");
				
			StreetNode source = nodes.get(idStart);
			StreetNode dest = nodes.get(idEnd);
			
			// Add a new street from the loaded segments.						
			List<StreetSegment> segments = new ArrayList<StreetSegment>();
			
			for (int j = 0; j < subSegs.length - 1; j++) {
				StreetNode n1 = nodes.get(subSegs[j]);
				StreetNode n2 = nodes.get(subSegs[j + 1]);
				StreetSegment seg = new StreetSegment(linkIds++, n1, n2, speedLimit, Geometry.haversineDistance(n1.getPosition(), n2.getPosition()));
				segments.add(seg);
			}
			Street s = new Street(linkIds++, name, segments);
			streets.put(source.getLabel() + "->" + dest.getLabel(), s);

		}
	}
	
	private static void mergeStreetMaps(Map<String, StreetNode> n1, Map<String, Street> s1, Map<String, StreetNode> n2, Map<String, Street> s2) {
		int missingNodes = 0;
		
		for (String key : n1.keySet()) {
			
			if (!n2.containsKey(key)) {
				n2.put(key, n1.get(key));
				missingNodes++;
			}
		}
		System.out.println("Missing nodes: " + missingNodes);
		
		int missingStreets = 0;

		for (String key : s1.keySet()) {
			if (!s2.containsKey(key)) {
				missingStreets++;
				s2.put(key, s1.get(key));
			}
			
		}
		System.out.println("Missing streets: " + missingStreets);
	}
}
