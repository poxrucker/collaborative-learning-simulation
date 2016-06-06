package allow.simulator.mobility.planner;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import allow.simulator.mobility.data.TType;
import allow.simulator.util.Coordinate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractOTPPlanner implements IPlannerService {
	// Json mapper to parse planner responses.
	protected static final ObjectMapper mapper = new ObjectMapper();

	// URI to send requests to.
	private static final String routingURI = "/otp/routers/default/plan";

	// DateFormat to format departure date.
	private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
					
	// DateFormat to format departure time.
	private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mma");
		
	/**
	 * Parses an itinerary from a Json node.
	 * 
	 * @param it Json node.
	 * @return Itinerary parsed from given node.
	 * 
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	protected Itinerary parseItinerary(JsonNode it) throws JsonParseException,
			JsonMappingException, IOException {
		Itinerary newIt = new Itinerary();
		newIt.startTime = it.get("startTime").asLong();
		newIt.endTime = it.get("endTime").asLong();
		newIt.walkTime = it.get("walkTime").asLong();
		newIt.transitTime = it.get("transitTime").asLong();
		newIt.waitingTime = it.get("waitingTime").asLong();
		newIt.walkDistance = it.get("walkDistance").asDouble();
		newIt.transfers = it.get("transfers").asInt();
		newIt.duration = it.get("duration").asLong();
		JsonNode legNode = it.get("legs");

		for (Iterator<JsonNode> jt = legNode.elements(); jt.hasNext();) {
			Leg newLeg = parseLeg(jt.next());
			newIt.legs.add(newLeg);
			newIt.costs += newLeg.costs;
		}
		newIt.itineraryType = Itinerary.getItineraryType(newIt);
		
		if (newIt.itineraryType == TType.BUS) {
			newIt.costs = 1.2;
		}
		return newIt;
	}

	/**
	 * Parses a leg from a Json node.
	 * 
	 * @param lt Json node.
	 * @return Leg parsed from given Json node.
	 */
	protected Leg parseLeg(JsonNode lt) {
		Leg newLeg = new Leg();
		newLeg.startTime = Long.parseLong(lt.get("startTime").asText());
		newLeg.endTime = Long.parseLong(lt.get("endTime").asText());
		newLeg.distance = lt.get("distance").asDouble();
		newLeg.from = parsePlace(lt.get("from"));
		newLeg.stopIdFrom = parseStopId(lt.get("from"));
		newLeg.to = parsePlace(lt.get("to"));
		newLeg.stopIdTo = parseStopId(lt.get("to"));
		newLeg.legGeometry = lt.get("legGeometry").get("points").asText();
		newLeg.mode = TType.valueOf(lt.get("mode").asText());
		
		switch (newLeg.mode) {
		case WALK:
			newLeg.costs = 0.0;
			break;
			
		case BICYCLE:
			newLeg.costs = newLeg.distance * 0.000005;
			break;
			
		case BUS:
		case CABLE_CAR:
			newLeg.routeId = lt.get("routeId").asText();
			newLeg.agencyId = lt.get("agencyId").asText();
			newLeg.tripId = lt.get("tripId").asText();			
			newLeg.costs = 1.2;
			newLeg.stops = parseStopIds(lt.get("intermediateStops"));
			break;
			
		case CAR:
			newLeg.costs = newLeg.distance * 0.00035; // 0.00025;
			break;
			
		case RAIL:
			newLeg.costs = 20.0;
			break;
			
		case TRANSIT:
		default:
			throw new IllegalArgumentException("Error: Unknown transport type " + newLeg.mode);
		}
		
		List<String> nodes = new ArrayList<String>();
		for (Iterator<JsonNode> it = lt.get("osmNodes").iterator(); it.hasNext(); ) {
			nodes.add(it.next().asText());
		}
		newLeg.osmNodes = nodes;
		return newLeg;
	}

	protected String parseStopId(JsonNode place) {
		JsonNode stopId = place.get("stopId");
		
		if (stopId != null) {
			String tokens[] = stopId.asText().split(":");
			return tokens[1];
		}
		return null;
	}
	
	protected List<String> parseStopIds(JsonNode stops) {
		List<String> ids = new ArrayList<String>();
		
		for (Iterator<JsonNode> it = stops.iterator(); it.hasNext(); ) {
			ids.add(parseStopId(it.next()));
		}
		return ids;
	}
	
	protected Coordinate parsePlace(JsonNode place) {
		Coordinate c = new Coordinate(place.get("lon").doubleValue(),
				place.get("lat").doubleValue());
		return c;
	}
	
	protected static String createQueryString(JourneyRequest request) {
		StringBuilder paramBuilder = new StringBuilder();
		paramBuilder.append(routingURI);
		paramBuilder.append("?toPlace=");
		paramBuilder.append(request.To.y + "," + request.To.x);
		paramBuilder.append("&fromPlace=");
		paramBuilder.append(request.From.y + "," + request.From.x);
		paramBuilder.append("&date=" + request.Date.format(dateFormat));

		if (request.ArrivalTime != null) {
			paramBuilder.append("&time=" + request.ArrivalTime.format(timeFormat));
			paramBuilder.append("&arriveBy=true");
			
		} else {
			paramBuilder.append("&time=" + request.DepartureTime.format(timeFormat));
			paramBuilder.append("&arriveBy=false");
		}
		paramBuilder.append("&optimize=" + request.RouteType.toString());
		paramBuilder.append("&mode=");

		for (int i = 0; i < request.TransportTypes.length - 1; i++) {
			paramBuilder.append(request.TransportTypes[i].toString() + ",");
		}
		paramBuilder.append(request.TransportTypes[request.TransportTypes.length - 1]);
		paramBuilder.append("&numItineraries=" + request.ResultsNumber);
		paramBuilder.append("&walkSpeed=" + 1.29);
		paramBuilder.append("&bikeSpeed=" + 4.68);
		paramBuilder.append("&showIntermediateStops=true");
		// paramBuilder.append("&maxWalkDistance=" + journey.MaximumWalkDistance);
		return paramBuilder.toString();
	}
}
