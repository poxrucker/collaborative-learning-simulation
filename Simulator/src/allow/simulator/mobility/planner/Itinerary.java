package allow.simulator.mobility.planner;

import java.util.ArrayList;
import java.util.List;

import allow.simulator.util.Coordinate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * An Itinerary is one complete way of getting from the start location to the end location.
 */
public final class Itinerary {
	/**
	 * Request identifier
	 */
	public long reqId;

	/**
	 * Request number
	 */
	public int reqNumber;

	/**
	 * Contains the concrete itineraries in case shared modalities are used
	 */
	public List<Itinerary> subItineraries;
	
	/**
	 * Duration of the trip on this itinerary, in seconds
	 */
	public long duration;

	/**
	 * Time initially spent waiting, in seconds
	 */
	public long initialWaitingTime;

	/**
	 * Time that the trip departs
	 */
	public long startTime = 0;

	/**
	 * Starting point
	 */
	public Coordinate from = null;

	/**
	 * Time that the trip arrives
	 */
	public long endTime = 0;

	/**
	 * Destination location
	 */
	public Coordinate to = null;

	/**
	 * How much time is spent walking, in seconds
	 */
	public long walkTime = 0;

	/**
	 * How much time is spent on transit, in seconds
	 */
	public long transitTime = 0;

	/**
	 * How much time is spent waiting for transit to arrive, in seconds
	 */
	public long waitingTime = 0;

	/**
	 * How far the user has to walk, in meters
	 */
	public double walkDistance = 0.0;

	/**
	 * The number of transfers this trip has
	 */
	public int transfers = 0;

	/**
	 * A list of Legs. Each Leg is either a walking (cycling, car) portion of
	 * the trip, or a transit trip on a particular vehicle. So a trip where the
	 * use walks to the Q train, transfers to the 6, then walks to their
	 * destination, has four legs
	 */
	public List<Leg> legs = new ArrayList<Leg>();

	/**
	 * Costs of this itinerary
	 */
	public double costs;

	/**
	 * Utility value of the itinerary from [0;1]
	 */
	public double utility;

	/**
	 * Maximum transit filling level over all transit legs
	 */
	public double maxFillingLevel;

	public TType itineraryType;

	public static TType getItineraryType(Itinerary it) {
		if (it.legs.size() == 1)
			return it.legs.get(0).mode;

		for (int i = 0; i < it.legs.size(); i++) {
			Leg l = it.legs.get(i);

			switch (l.mode) {
			case BUS:
			case CABLE_CAR:
				return TType.BUS;

			case BICYCLE:
			case SHARED_BICYCLE:
			case TAXI:
			case SHARED_TAXI:
				return l.mode;

			default:
				break;
			}
		}
		throw new IllegalStateException(
				"Error: Cannot determine itinerary type.");
	}

	public Itinerary clone() {
		Itinerary ret = new Itinerary();
		ret.costs = costs;
		ret.duration = duration;
		ret.initialWaitingTime = initialWaitingTime;
		ret.endTime = endTime;
		ret.from = from;
		ret.itineraryType = itineraryType;
		ret.legs = legs;
		ret.reqId = reqId;
		ret.reqNumber = reqNumber;
		ret.startTime = startTime;
		ret.to = to;
		ret.transfers = transfers;
		ret.transitTime = transitTime;
		ret.utility = utility;
		ret.maxFillingLevel = maxFillingLevel;
		ret.waitingTime = waitingTime;
		ret.walkDistance = walkDistance;
		ret.walkTime = walkTime;
		return ret;
	}

	public String toString() {
		return "[" + itineraryType + ", " + (duration + initialWaitingTime)
				+ " s, " + costs + " €, " + maxFillingLevel + " fill grade, "
				+ utility + "]";
	}
}
