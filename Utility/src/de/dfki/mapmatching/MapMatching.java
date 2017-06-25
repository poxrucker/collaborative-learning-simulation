package de.dfki.mapmatching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import allow.simulator.util.Coordinate;
import allow.simulator.world.StreetMap;
import allow.simulator.world.StreetNode;
import allow.simulator.world.StreetSegment;

/**
 * Class implementing an algorithm for matching GPS traces to a given network.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public class MapMatching {

	// Street network this map matching is working with.
	private StreetMap map;

	// Quad tree structure to allow fast access to nearest points.
	private QuadTree<StreetNode> quad;

	public MapMatching(StreetMap map) {
		this.map = map;
		final double dim[] = map.getDimensions();
		
		// Initialize quad tree.
		quad = new QuadTree<StreetNode>(dim[0], dim[2], dim[1], dim[3]);

		for (StreetNode node : map.getStreetNodes()) {
			quad.put(node.getPosition().x, node.getPosition().y, node);
		}
	}

	public ScoredPath mapMatch(List<Coordinate> gpsTrace, List<StreetSegment> initial) {
		// Catch empty trace.
		if (gpsTrace.size() == 0) {
			return null;
		}
		List<ScoredPath> initialPaths = new ArrayList<ScoredPath>(1);
		
		for (int i = 0; i < initial.size(); i++) {
			initialPaths.add(new ScoredPath(map, gpsTrace.get(0), initial.get(i)));
		}
		return match(gpsTrace, initialPaths);
	}

	public ScoredPath mapMatch(List<Coordinate> gpsTrace) {
		System.out.print(", Gps Trace Size : "+gpsTrace.size());
		// Catch empty trace.
		if (gpsTrace.size() == 0) {
			return null;
		}

		// Get initial candidate segments using the quad tree structure.
		Coordinate firstPoint = gpsTrace.get(0);
		Coordinate secondPoint = gpsTrace.get(1);
		
		//List<StreetNode> nearest = new ArrayList<>();
		List<StreetNode> firstNearest = quad.getNNearestNeighbours(firstPoint.x, firstPoint.y, 10,10);
		/*if(firstNearest.size()<30){
			
			firstNearest = quad.get(firstPoint.x, firstPoint.y, 0.001);
			if(firstNearest.size()<30){
				firstNearest = quad.get(firstPoint.x, firstPoint.y, 0.002);
			}
		}
		List<StreetNode> secondNearest = quad.get(secondPoint.x, secondPoint.y, 0.002);*/
		
		/*if(firstNearest.size()<secondNearest.size())
		{
			for(int i = secondNearest.size() - 1; i > -1; --i){
		    StreetNode str = secondNearest.get(i);
		    if(!secondNearest.remove(str))
		    	firstNearest.remove(str);
		}
			nearest = firstNearest;
		}else{
			for(int i = firstNearest.size() - 1; i > -1; --i){
			    StreetNode str = firstNearest.get(i);
			    if(!firstNearest.remove(str))
			    	secondNearest.remove(str);
			}
				nearest = secondNearest;
		}
		*/
		
		//firstNearest.retainAll(secondNearest);
		
		//TODO: Try to do an intersection of another gps point. looks okay!
		//TODO: if intitial candidate is more than 50 then reduce the radius query.
		
		System.out.print(", Initial Candidate Size : "+firstNearest.size());
		// If no nodes were found near the starting point, return null.
		if (firstNearest.size() == 0) {
			return null;
		}
		
		// Create initial scored path for each individual segment.
		List<ScoredPath> initialPaths = new ArrayList<ScoredPath>();

		// Initialize input buffer queues for first candidate segment.
		Queue<ScoredPath> inputBuffer = new PriorityQueue<ScoredPath>(1024);
		
		// Otherwise add all segments incident to the candidate nodes to the set.
		for (StreetNode n : firstNearest) {
			Collection<StreetSegment> incidentEdges = map.getIncidentEdges(n);

			
			for (StreetSegment incident : incidentEdges) {
				ScoredPath newPath = new ScoredPath(map, firstPoint, incident);
				//TODO: Why the value is 25 ?
				if (newPath.getScore() <= 25.0 && !inputBuffer.contains(newPath)) {
					inputBuffer.add(newPath);
				}
			}
		}
		
		if (inputBuffer.size() == 0) {
			System.out.print(", Error: No initial paths.");
		}
		return match(gpsTrace, inputBuffer);
	}
	

	private ScoredPath match(List<Coordinate> gpsTrace, Queue<ScoredPath> inputBuffer) {
		TimeWatch watch = TimeWatch.start();
		

		// Initialize buffer queues creating one path per candidate segment.
		Queue<ScoredPath> outputBuffer = new PriorityQueue<ScoredPath>(1024);
		
		// Match each remaining point of trace.
		for (int j = 1; j < gpsTrace.size(); j++) {
			//System.out.println("Matching point " + j + " of " + gpsTrace.size());
			int loopMax = Math.min(inputBuffer.size(), 25);
			final Coordinate next = gpsTrace.get(j);

			for (int i = 0; i < loopMax; i++) {
				// Add point to current path.
				final List<ScoredPath> addPaths = inputBuffer.poll().addPoint(next);
				
				// Add new paths to output buffer and check for duplicates.
				for (ScoredPath pa : addPaths) {

					if (!outputBuffer.contains(pa)) {
						outputBuffer.add(pa);
					}
				}
			}
			inputBuffer.clear();
			
			// Swap buffer handles.
			Queue<ScoredPath> temp = inputBuffer;
			inputBuffer = outputBuffer;
			outputBuffer = temp;
			outputBuffer.clear();
		}
		ScoredPath ret = inputBuffer.poll();
		System.out.print(", Match Timer : "+ watch.toMinuteSeconds());
		return ret;
	}
	
	private ScoredPath match(List<Coordinate> gpsTrace, List<ScoredPath> candidatePaths) {
		TimeWatch watch = TimeWatch.start();
		

		// Initialize buffer queues creating one path per candidate segment.
		Queue<ScoredPath> inputBuffer = new PriorityQueue<ScoredPath>(1024);
		Queue<ScoredPath> outputBuffer = new PriorityQueue<ScoredPath>(1024);

/*		for (ScoredPath candidatePath : candidatePaths) {

			if (!inputBuffer.contains(candidatePath)) {
				inputBuffer.offer(candidatePath);
			}
		}
		*/
		
		inputBuffer.addAll(candidatePaths);
		
		// Match each remaining point of trace.
		for (int j = 1; j < gpsTrace.size(); j++) {
			//System.out.println("Matching point " + j + " of " + gpsTrace.size());
			int loopMax = Math.min(inputBuffer.size(), 25);
			final Coordinate next = gpsTrace.get(j);

			for (int i = 0; i < loopMax; i++) {
				// Add point to current path.
				final List<ScoredPath> addPaths = inputBuffer.poll().addPoint(next);
				
				// Add new paths to output buffer and check for duplicates.
				for (ScoredPath pa : addPaths) {

					if (!outputBuffer.contains(pa)) {
						outputBuffer.add(pa);
					}
				}
			}
			/*inputBuffer.clear();
			//TODO : Check here!!
			// Swap buffer handles.
			Queue<ScoredPath> temp = inputBuffer;
			inputBuffer = outputBuffer;
			outputBuffer = temp;
			outputBuffer.clear();*/
			
			inputBuffer = outputBuffer;
			outputBuffer.clear();
		}
		ScoredPath ret = inputBuffer.peek();
		System.out.println(",Match Timer : "+ watch.toMinuteSeconds());
		return ret;
	}
}
