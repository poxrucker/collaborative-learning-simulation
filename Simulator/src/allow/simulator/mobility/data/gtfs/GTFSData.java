package allow.simulator.mobility.data.gtfs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import allow.simulator.util.Coordinate;

/**
 * A GTFS dataset container.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class GTFSData {
	// Agencies
	private Map<String, GTFSAgency> agencies;
	
	// Routes
	private Map<String, List<GTFSRoute>> routes;
	
	// Services
	private Map<String, GTFSService> service;
	
	// Service exceptions
	private Map<String, List<GTFSServiceException>> exceptions;
	
	// Stops
	private Map<String, GTFSStop> stops;
	
	// Stop times
	private Map<String, GTFSStopTimes> stoptimes;
	
	// Trips
	private Map<String, List<GTFSTrip>> trips;
	
	// Shapes
	private Map<String, List<Coordinate>> shapes;
	
	private GTFSData() { }
	
	/**
	 * Returns the agencies contained in the GTFS dataset.
	 * 
	 * @return All agencies of GTFS dataset
	 */
	public Map<String, GTFSAgency> getAgencies() {
		return Collections.unmodifiableMap(agencies);
	}
	
	/**
	 * Returns the routes contained in the GTFS dataset.
	 * 
	 * @return All routes of GTFS dataset
	 */
	public Map<String, List<GTFSRoute>> getRoutes() {
		return Collections.unmodifiableMap(routes);
	}
	
	/**
	 * Returns the services contained in the GTFS dataset.
	 * 
	 * @return All services of GTFS dataset
	 */
	public Map<String, GTFSService> getServices() {
		return Collections.unmodifiableMap(service);
	}
	
	/**
	 * Returns the service exceptions contained in the GTFS dataset.
	 * 
	 * @return All service exceptions of GTFS dataset
	 */
	public Map<String, List<GTFSServiceException>> getServiceExceptions() {
		return Collections.unmodifiableMap(exceptions);
	}
	
	/**
	 * Returns the stops contained in the GTFS dataset.
	 * 
	 * @return All stops of GTFS dataset
	 */
	public Map<String, GTFSStop> getStops() {
		return Collections.unmodifiableMap(stops);
	}
	
	/**
	 * Returns the stop times contained in the GTFS dataset.
	 * 
	 * @return All stop times of GTFS dataset
	 */
	public Map<String, GTFSStopTimes> getStopTimes() {
		return Collections.unmodifiableMap(stoptimes);
	}
	
	/**
	 * Returns the trips contained in the GTFS dataset.
	 * 
	 * @return All trips of GTFS dataset
	 */
	public Map<String, List<GTFSTrip>> getTrips() {
		return Collections.unmodifiableMap(trips);
	}
	
	/**
	 * Returns the shapes contained in the GTFS dataset.
	 * 
	 * @return All shapes of GTFS dataset
	 */
	public Map<String, List<Coordinate>> getShapes() {
		return Collections.unmodifiableMap(shapes);
	}
	
	public static GTFSData parse(Path gtfsRoot) throws IOException {
		GTFSData ret = new GTFSData();
		ret.agencies = loadAgencies(gtfsRoot.resolve("agency.txt"));
		ret.routes = loadRoutes(gtfsRoot.resolve("routes.txt"));
		ret.service = loadServices(gtfsRoot.resolve("calendar.txt"));
		ret.exceptions = loadServiceExceptions(gtfsRoot.resolve("calendar_dates.txt"));
		ret.stops = loadStops(gtfsRoot.resolve("stops.txt"));
		ret.stoptimes = loadStopTimes(gtfsRoot.resolve("stop_times.txt"));
		ret.trips = loadTrips(gtfsRoot.resolve("trips.txt"));
		ret.shapes = loadShapes(gtfsRoot.resolve("shapes.txt"));
		return ret;
	}
	
	private static Map<String, GTFSAgency> loadAgencies(Path path) throws IOException {
		// Read file.
		List<String> content = Files.readAllLines(path);

		// Initialize map to return.
		Map<String, GTFSAgency> ret = new HashMap<String, GTFSAgency>(content.size() - 1);
		
		// Parse agencies from lines.
		for (int i = 1; i < content.size(); i++) {
			GTFSAgency newAgency = GTFSAgency.fromGTFS(content.get(i));
			ret.put(newAgency.getId(), newAgency);
		}
		return ret;
	}
	
	private static Map<String, List<GTFSRoute>> loadRoutes(Path path) throws IOException {
		// Read file.
		List<String> content = Files.readAllLines(path);

		// Initialize map to return.
		Map<String, List<GTFSRoute>> ret = new HashMap<String, List<GTFSRoute>>(content.size() - 1);
		
		// Parse route from lines.
		for (int i = 1; i < content.size(); i++) {
			GTFSRoute newRoute = GTFSRoute.fromGTFS(content.get(i));
			List<GTFSRoute> temp = ret.get(newRoute.getAgencyId());
			
			if (temp != null) {
				temp.add(newRoute);
				
			} else {
				List<GTFSRoute> r = new ArrayList<GTFSRoute>();
				r.add(newRoute);
				ret.put(newRoute.getAgencyId(), r);
			}
		}
		return ret;
	}
	
	private static Map<String, GTFSService> loadServices(Path path) throws IOException {
		// Read file.
		List<String> content = Files.readAllLines(path);

		// Initialize map to return.
		Map<String, GTFSService> ret = new HashMap<String, GTFSService>(content.size() - 1);
		
		// Parse route from lines.
		for (int i = 1; i < content.size(); i++) {
			GTFSService newService;
			try {
				newService = GTFSService.fromGTFS(content.get(i));
			} catch (ParseException e) {
				throw new IOException(e);
			}
			ret.put(newService.getServiceId(), newService);
		}
		return ret;
	}
	
	private static Map<String, List<GTFSServiceException>> loadServiceExceptions(Path path) throws IOException {
		// Read file.
		List<String> content = Files.readAllLines(path);

		// Initialize map to return.
		Map<String, List<GTFSServiceException>> ret = new HashMap<String, List<GTFSServiceException>>(content.size() - 1);
		
		// Parse route from lines.
		for (int i = 1; i < content.size(); i++) {
			GTFSServiceException newServiceException;
			try {
				newServiceException = GTFSServiceException.fromGTFS(content.get(i));
			} catch (ParseException e) {
				throw new IOException(e);
			}
			List<GTFSServiceException> exceptions = ret.get(newServiceException.getServiceId());
			
			if (exceptions == null) {
				List<GTFSServiceException> ex = new ArrayList<GTFSServiceException>();
				ex.add(newServiceException);
				ret.put(newServiceException.getServiceId(), ex);
				
			} else {
				exceptions.add(newServiceException);
			}
		}
		return ret;
	}
	
	private static Map<String, GTFSStop> loadStops(Path path) throws IOException {
		// Read file.
		List<String> content = Files.readAllLines(path);

		// Initialize map to return.
		Map<String, GTFSStop> ret = new HashMap<String, GTFSStop>(content.size() - 1);
		
		// Parse stops from lines.
		for (int i = 1; i < content.size(); i++) {
			GTFSStop newStop = GTFSStop.fromGTFS(content.get(i));
			ret.put(newStop.getId(), newStop);
		}
		return ret;
	}
	
	private static Map<String, GTFSStopTimes> loadStopTimes(Path path) throws IOException {
		// Read file.
		List<String> content = Files.readAllLines(path);

		// Initialize map to return.
		Map<String, GTFSStopTimes> ret = new HashMap<String, GTFSStopTimes>();
		
		// Parse stop times from corresponding lines.
		int startIdx = 1;
		int endIdx = 1;
		int seq = 0;
		String previousTripId = "";
		
		for (int i = 1; i < content.size(); i++) {
			String line = content.get(i);
			String tokens[] = line.split(",");
			int seqNumber = Integer.parseInt(tokens[4]);
			String tripId = tokens[0];
			
			if (previousTripId.equals("")) {
				previousTripId = tokens[0];
			}
			
			if (i == content.size() - 1) {
				GTFSStopTimes newStopTime = GTFSStopTimes.fromGTFS(content.subList(startIdx, endIdx));
				ret.put(newStopTime.getTripId(), newStopTime);
				
			} else if (seqNumber > seq && tripId.equals(previousTripId)) {
				endIdx++;
				seq = seqNumber;
				
			} else {
				GTFSStopTimes newStopTime = GTFSStopTimes.fromGTFS(content.subList(startIdx, endIdx));
				ret.put(newStopTime.getTripId(), newStopTime);
				startIdx = endIdx;
				endIdx++;
				seq = seqNumber;
				previousTripId = tripId;
			}	
		}
		return ret;
	}
	
	private static Map<String, List<GTFSTrip>> loadTrips(Path path) throws IOException {
		// Read file.
		List<String> content = Files.readAllLines(path);

		// Initialize map to return.
		Map<String, List<GTFSTrip>> ret = new HashMap<String, List<GTFSTrip>>();
		
		// Parse trips from lines.
		for (int i = 1; i < content.size(); i++) {
			GTFSTrip newTrip = GTFSTrip.fromGTFS(content.get(i));
			List<GTFSTrip> temp = ret.get(newTrip.getRouteId());
			
			if (temp != null) {
				temp.add(newTrip);
			} else {
				List<GTFSTrip> n = new ArrayList<GTFSTrip>();
				n.add(newTrip);
				ret.put(newTrip.getRouteId(), n);
			}
		}
		return ret;
	}
	
	private static Map<String, List<Coordinate>> loadShapes(Path path) throws IOException {
		Map<String, List<Coordinate>> ret = new HashMap<String, List<Coordinate>>();
		
		// Read file.
		List<String> content = Files.readAllLines(path);
						
		// Read first data line.
		List<Coordinate> currentSequence = null;
		String currentId = "";
		
		for (int i = 1; i < content.size(); i++) {
			// Split current line.
			String tokens[] = content.get(i).split(",");
			
			if (!tokens[0].equals(currentId)) {
				currentId = tokens[0];
				currentSequence = new ArrayList<Coordinate>(256);
				ret.put(currentId, currentSequence);
			}
			currentSequence.add(new Coordinate(Double.parseDouble(tokens[2]), Double.parseDouble(tokens[1])));
		}
		ret.put(currentId, currentSequence);
		return ret;
	}
}
