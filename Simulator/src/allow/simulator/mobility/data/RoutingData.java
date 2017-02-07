package allow.simulator.mobility.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import allow.simulator.mobility.data.gtfs.GTFSAgency;
import allow.simulator.mobility.data.gtfs.GTFSData;
import allow.simulator.mobility.data.gtfs.GTFSRoute;
import allow.simulator.mobility.data.gtfs.GTFSStop;
import allow.simulator.mobility.data.gtfs.GTFSTrip;
import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.world.Street;
import allow.simulator.world.StreetMap;
import allow.simulator.world.StreetNode;
import allow.simulator.world.StreetSegment;

public final class RoutingData {
	// Mapping of bus stop identifiers to list of streets
	private Map<String, List<Street>> publicTransportationRouting;
		
	private RoutingData() { }
	
	public List<Street> getBusstopRouting(String start, String end) {
		return publicTransportationRouting.get(start + "," + end);
	}
	
	public static RoutingData parse(Path routing, StreetMap map, GTFSData gtfsData) throws IOException {
		RoutingData ret = new RoutingData();
		ret.publicTransportationRouting = createRouting(routing, map, gtfsData);
		return ret;
	}
	
	private static Map<String, List<Street>> createRouting(Path routing, StreetMap map, GTFSData gtfs) throws IOException {
		// Mapping to return
		Map<String, List<Street>> ret = new HashMap<String, List<Street>>();
		List<String> processedShapes = new ArrayList<String>();
		
		// Load shapes.
		Map<String, List<Coordinate>> shapes = gtfs.getShapes();
		Map<String, GTFSAgency> agencies = gtfs.getAgencies();
		
		for (GTFSAgency agency : agencies.values()) {
			String agencyId = agency.getId();
			List<GTFSRoute> routes = gtfs.getRoutes().get(agencyId);
			
			for (GTFSRoute route : routes) {
				List<GTFSTrip> trips = gtfs.getTrips().get(route.getId());
				
				for (GTFSTrip trip : trips) {
					String[] stopIds = gtfs.getStopTimes().get(trip.getTripId()).getStopIds();
					
					// Check if shape has already been processed
					if (processedShapes.contains(trip.getShapeId()))
						continue;
					
					// Get trace of shape and continue, if no trace is available.
					List<Street> polyTrace = createPolyTraceStreet(shapes.get(trip.getShapeId()));
					
					if (polyTrace == null || polyTrace.size() == 0)
						continue;
					
					// Check if shape has map matched traces.
					Path mappingPath = routing.resolve("mapping/" + trip.getShapeId());
					
					if (Files.exists(mappingPath)) {
						Map<String, List<Street>> mapping = parseStreetRoutingFromFile(mappingPath, map);
						ret.putAll(mapping);
						processedShapes.add(trip.getShapeId());
						continue;
					}
					System.out.println("Warning: No routing found for " + trip.getShapeId() + ". Using non matched GPS traces instead.");

					// Otherwise split trace along stops and create "virtual segments".
					List<List<Coordinate>> newShape = new ArrayList<List<Coordinate>>(stopIds.length);
					int lastIndex = 0;
					Coordinate lastCoordinate = polyTrace.get(0).getStartingNode().getPosition();
					
					for (int i = 0; i < stopIds.length; i++) {
						// Get position of next stop.
						GTFSStop stop = gtfs.getStops().get(stopIds[i]);
						Coordinate stopPos = new Coordinate(stop.getLon(), stop.getLat());
						List<Coordinate> s = new ArrayList<Coordinate>();
						
						// Find closest segment by projecting stop position to segment and measure distance.
						int minIndex = 0;
						double minDist = Geometry.haversineDistance(stopPos, projectPointToVirtualStreet(stopPos, polyTrace.get(0)));
						Coordinate minCoord = projectPointToVirtualStreet(stopPos, polyTrace.get(0));
						
						for (int j = 0; j < polyTrace.size(); j++) {
							Coordinate d = projectPointToVirtualStreet(stopPos, polyTrace.get(j));
							double dist = Geometry.haversineDistance(stopPos, d);
							
							if (dist < minDist) {
								minDist = dist;
								minIndex = j;
								minCoord = d;
							}
						}
						
						if (minIndex >= lastIndex) {
							Street seg = polyTrace.get(lastIndex);
							Coordinate start = lastCoordinate;
							s.add(start);
							
							for (int k = lastIndex; k < minIndex; k++) {
								seg = polyTrace.get(k);
								s.add(seg.getEndNode().getPosition());
							}
							s.add(minCoord);
							
						} else {
							Street seg = polyTrace.get(lastIndex);
							Coordinate start = lastCoordinate;
							s.add(start);
							
							// If point is closer to one of previous segments, iterate 
							for (int k = lastIndex; k < polyTrace.size(); k++) {
								seg = polyTrace.get(k);
								s.add(seg.getEndNode().getPosition());
							}
							
							for (int k = 0; k < minIndex; k++) {
								seg = polyTrace.get(k);
								s.add(seg.getEndNode().getPosition());
							}
							s.add(minCoord);

						}
						lastIndex = minIndex;
						lastCoordinate = minCoord;
						newShape.add(s);
					}
					
					for (int i = 0; i < stopIds.length - 1; i++) {
						ret.put(stopIds[i] + "," + stopIds[i + 1], createPolyTraceStreet(newShape.get(i + 1)));
					}
					processedShapes.add(trip.getShapeId());
				}
			}
		}
		return ret;
	}
	
	private static Map<String, List<Street>> parseStreetRoutingFromFile(Path path, StreetMap map) throws IOException {
		Map<String, List<Street>> ret = new HashMap<String, List<Street>>();
		List<String> lines = Files.readAllLines(path);
		
		for (int i = 0; i < lines.size(); i += 2) {
			String key = lines.get(i);
			String nodes = lines.get(i + 1);
			String tokens[] = nodes.split(" ");
			List<Street> streets = new ArrayList<Street>();
			
			//for (int j = 0; j < tokens.length - 1; j++) {
				StreetNode n1 = null, n2 = null;
				int k = 0;

				while ((k < tokens.length) && (n1 == null)) {
					n1 = map.getStreetNodeReduced(tokens[k]);
					k++;
				}

				while (k < tokens.length) {
					n2 = null;
					
					while ((k < tokens.length) && (n2 == null)) {
						n2 = map.getStreetNodeReduced(tokens[k]);
						k++;
					}
					
					if (k == tokens.length)
						break;
					Street street = map.getStreet(n1.getLabel(), n2.getLabel());
					
					if (street == null)
						throw new IllegalStateException("Error: Could not find street.");
					n1 = n2;
					streets.add(street);
				}
			//}
			ret.put(key, streets);
		}
		return ret;
	}
	
	private static long virtualSegId = 1;
	private static long virtualNodeId = 0;
	
	private static List<Street> createPolyTraceStreet(List<Coordinate> coordinates) {
		if (coordinates == null) {
			return null;
		}
		
		if (coordinates.size() == 0) {
			return new ArrayList<Street>(0);
		}
		List<Street> ret = new ArrayList<Street>(coordinates.size() - 1);
		
		for (int i = 0; i < coordinates.size() - 1; i++) {
			
			if (!coordinates.get(i).equals(coordinates.get(i + 1))) {
				List<StreetSegment> segs = new ArrayList<StreetSegment>(1);
				segs.add(new StreetSegment(-(virtualSegId++), 
						new StreetNode(-(virtualNodeId++), "", coordinates.get(i)),
						new StreetNode(-(virtualNodeId++), "", coordinates.get(i + 1)),
						11.1,
						Geometry.haversineDistance(coordinates.get(i), coordinates.get(i + 1))));
				ret.add(new Street(-(virtualSegId++), "", segs));
			}
		}
		return ret;
	}
	
	private static Coordinate projectPointToVirtualStreet(Coordinate c, Street s) {
		Coordinate start = s.getStartingNode().getPosition();
		Coordinate end = s.getEndNode().getPosition();
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
}
