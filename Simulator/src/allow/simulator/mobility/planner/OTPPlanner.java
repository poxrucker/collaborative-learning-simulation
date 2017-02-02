package allow.simulator.mobility.planner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;

import allow.simulator.core.Time;
import allow.simulator.mobility.data.IDataService;
import allow.simulator.world.Street;
import allow.simulator.world.StreetMap;
import allow.simulator.world.StreetNode;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a journey planner for the Allow Ensembles urban traffic
 * simulation. Implements the IPlannerService interface. Encapsulates a client
 * querying OpenTripPlanner web service.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class OTPPlanner extends AbstractOTPPlanner {
	// Client to send requests.
	private final HttpClient client;

	// Host requests are send to.
	private final HttpHost target;

	// Streetmap to map driving/walking/cycling traces to if provided
	private final StreetMap map;
	
	// Dataservive to map bus traces to if prvided
	private final IDataService dataService;
	
	// Time instance to use
	private final Time time;
	
	/**
	 * Creates a new instance of OTPJourneyPlanner sending requests to
	 * an OpenTripPlanner server instance.
	 * 
	 * @param host Host running OpenTripPlanner service
	 * @param port Port of OpenTripPlanner service
	 */
	public OTPPlanner(String host, int port) {
		this(host, port, null, null, null);
	}

	/**
	 * Creates a new instance of OTPJourneyPlanner sending requests to
	 * an OpenTripPlanner server instance. The instance can use the
	 * supplied StreetMap and IDataService instances to map returned
	 * traces of GPS points to the underlying map.
	 * 
	 * @param host Host running OpenTripPlanner service
	 * @param port Port of OpenTripPlanner service
	 * @param map Streetmap to map returned traces to
	 * @param dataService Service providing routing between bus stops
	 */
	public OTPPlanner(String host, int port, StreetMap map, IDataService dataService, Time time) {
		target = new HttpHost(host, port, "http");
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", port, PlainSocketFactory.getSocketFactory()));
		PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
		cm.setMaxTotal(300);
		cm.setDefaultMaxPerRoute(150);
		client = new DefaultHttpClient(cm);
		this.map = map;
		this.dataService = dataService;
		this.time = time;
	}
	
	@Override
	public boolean requestSingleJourney(JourneyRequest request, List<Itinerary> itineraries) {
		// Create new get request and set URI.
		HttpGet getRequest = new HttpGet();

		try {
			getRequest.setURI(new URI(createQueryString(request)));
			
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

		try {
			// Execute request and receive response
			HttpResponse httpResponse = client.execute(target, getRequest);
			String res = EntityUtils.toString(httpResponse.getEntity());

			// Check for errors
			final JsonNode root = mapper.readTree(res);
			final JsonNode error = root.get("error");

			if (error != null)
				return false;

			// Parse response
			JsonNode travelPlan = root.get("plan");
			JsonNode it = travelPlan.get("itineraries");

			for (Iterator<JsonNode> jt = it.elements(); jt.hasNext();) {
				JsonNode next = jt.next();
				Itinerary nextIt = parseItinerary(next);
				nextIt.from = request.From;
				nextIt.to = request.To;
				nextIt.reqId = request.ReqId;
				nextIt.reqNumber = request.ReqNumber;
				
				for (Leg l : nextIt.legs) {
					mapTracesToStreets(l);
				}
				nextIt.initialWaitingTime = (time != null) ? Math.max((nextIt.startTime - time.getTimestamp()) / 1000, 0) : 0;
				itineraries.add(nextIt);
			}
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private void mapTracesToStreets(Leg leg) {
		leg.streets = new ArrayList<Street>();
		if (map != null && (leg.mode == TType.CAR || leg.mode == TType.BICYCLE || leg.mode == TType.WALK)) {

			for (int j = 0; j < leg.osmNodes.size() - 1; j++) {
				String first = normalize(leg.osmNodes.get(j), map);
				String second = normalize(leg.osmNodes.get(j + 1), map);
				Street street = map.getStreet(first, second);

				if (street != null)
					leg.streets.add(street);
			}

		} else if (dataService != null && leg.mode == TType.BUS) {
			List<String> allStops = new ArrayList<String>(leg.stops.size() + 2);
			allStops.add(leg.stopIdFrom);
			
			for (int j = 0; j < leg.stops.size(); j++) {
				allStops.add(leg.stops.get(j));
			}
			allStops.add(leg.stopIdTo);
			
			for (int j = 0; j < allStops.size() - 1; j++) {
				String first = allStops.get(j);
				String second = allStops.get(j + 1);
				List<Street> segs = dataService.getBusstopRouting(first, second);

				if (segs != null)
					leg.streets.addAll(segs);
			}
		}
	}
	
	private static String normalize(String nodeLabel, StreetMap map) {
		if (nodeLabel.startsWith("osm:node") || nodeLabel.startsWith("split"))
			// These are nodes which have the same label as in the planner.
			return nodeLabel;
		String tokens[] = nodeLabel.split("_");

		if (tokens.length == 1) {
			// These are unknown nodes.
			return "";
		}
		// These are intermediate nodes which can be determined by their position.
		// Planner returns "streetname_lat,lon".
		StreetNode n = map.getStreetNodeFromPosition(tokens[1]);

		if (n == null) {
			return "";
		}
		return n.getLabel();
	}
}
