package allow.simulator.mobility.planner;

import java.util.ArrayList;
import java.util.List;

import allow.simulator.entity.knowledge.TravelExperience;
import allow.simulator.util.Coordinate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)

/**
 * An Itinerary is one complete way of getting from the start location to the end location.
 */
public final class Itinerary {

    /**
     * Duration of the trip on this itinerary, in seconds.
     */
    public long duration = 0L;

    public long initialWaitingTime = 0L;
    
    public long reqId;
    
    public int reqNumber;
        
    /**
     * Time that the trip departs.
     */
    public long startTime = 0;
    
    /**
     * Starting point.
     */
    public Coordinate from = null;
    
    /**
     * Time that the trip arrives.
     */
    public long endTime = 0;

    /**
     * Destination location.
     */
    public Coordinate to = null;
    
    /**
     * How much time is spent walking, in seconds.
     */
    public long walkTime = 0;
    
    /**
     * How much time is spent on transit, in seconds.
     */
    public long transitTime = 0;
    
    /**
     * How much time is spent waiting for transit to arrive, in seconds.
     */
    public long waitingTime = 0;

    /**
     * How far the user has to walk, in meters.
     */
    public double walkDistance = 0.0;
   
    /**
     * The number of transfers this trip has.
     */
    public int transfers = 0;

    /**
     * A list of Legs. Each Leg is either a walking (cycling, car) portion of the trip, or a transit
     * trip on a particular vehicle. So a trip where the use walks to the Q train, transfers to the
     * 6, then walks to their destination, has four legs.
     */
    public List<Leg> legs = new ArrayList<Leg>();

    /** 
     * adds leg to array list
     * @param leg
     */
    public void addLeg(Leg leg) {
    	
        if(leg != null) {
            legs.add(leg);
        }
    }

    /** 
     * remove the leg from the list of legs 
     * @param leg object to be removed
     */
    public void removeLeg(Leg leg) {
    	
        if(leg != null) {
            legs.remove(leg);
        }
    }
  
    /**
     * Costs of this itinerary.
     */
    public double costs;
    
    /**
     * Utility value of the itinerary from [0;1].
     */
    public double utility = -1.0;
    
    /**
     * Maximum bus filling level.
     */
    public double maxFillingLevel = 0.0;
    
    /**
     * Determines if this journey is a taxi itinerary.
     */
    public boolean isTaxiItinerary;
    
    public int itineraryType;
    
    public List<TravelExperience> priorSegmentation;
    
    public static int getItineraryType(Itinerary it) {
    	if ((it.legs.size() == 1)) {
			Leg l = it.legs.get(0);
			
			switch (l.mode) {
			case CAR:
				return 0;
				
			case BICYCLE:
				return 2;
				
			case WALK:
				return 3;
				
			case TAXI:
				return 4;
				
			default:
				break;
				
			}
		}
		
		for (int i = 0; i < it.legs.size(); i++) {
			Leg l = it.legs.get(i);
			
			switch (l.mode) {
			case BUS:
			case CABLE_CAR:
				return 1;
				
			case BICYCLE:
				return 2;
				
			default:
				break;
			}			
		}
		return -1;
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
    	return "[" + itineraryType + (isTaxiItinerary ? "(taxi)" : "") + ", " + (duration + initialWaitingTime) + " s, " + costs + " €, " + maxFillingLevel + " fill grade, " + utility + "]";
    }
}
