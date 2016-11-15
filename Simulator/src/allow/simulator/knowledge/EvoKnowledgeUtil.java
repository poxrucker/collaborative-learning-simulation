package allow.simulator.knowledge;

import java.util.List;

import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.mobility.planner.TType;
import allow.simulator.utility.ItineraryParams;

public final class EvoKnowledgeUtil {

	public static ItineraryParams createFromExperiences(Itinerary prior, List<TravelExperience> ex) {
		
		switch (prior.itineraryType) {
		
		case CAR:
		case TAXI:
		case SHARED_TAXI:
			return summarizeCarItinerary(prior, ex);
		
		case BUS:
			return summarizeBusItinerary(prior, ex);
			
		case BICYCLE:
		case SHARED_BICYCLE:
			return summarizeBicycleItinerary(prior, ex);
			
		case WALK:
			return summarizeWalkItinerary(prior, ex);
			
		default:
			throw new IllegalArgumentException("Illegal journey type " + prior.itineraryType);
		}
	}
	
	private static ItineraryParams summarizeCarItinerary(Itinerary it, List<TravelExperience> ex) {
		double travelTime = 0.0;
		double costs = 0.0;
		int numberOfTransfers = 0;
		double walkingDistance = 0.0;
		
		for (TravelExperience e : ex) {
			travelTime += e.getTravelTime();
			costs += e.getCosts();
			numberOfTransfers = 0;
			
			if (e.getTransportationMean() == TType.WALK)
				walkingDistance += e.getSegmentLength();
		}
		return new ItineraryParams(it.itineraryType, (long) travelTime, costs, 0.0,
				walkingDistance, numberOfTransfers);
	}
	
	private static ItineraryParams summarizeBusItinerary(Itinerary it, List<TravelExperience> ex) {
		double travelTime = 0.0;
		double busFillingLevel = 0.0;
		double walkingDistance = 0.0;
		
		for (TravelExperience e : ex) {
			travelTime += e.getTravelTime();
			
			if (e.getTransportationMean() == TType.WALK)
				walkingDistance += e.getSegmentLength();
			
			if (e.getTransportationMean() == TType.BUS) 
				busFillingLevel = Math.max(busFillingLevel, e.getPublicTransportationFillingLevel());
		}
		return new ItineraryParams(it.itineraryType, (long) travelTime, 1.2, busFillingLevel,
				walkingDistance, it.transfers);
	}
	
	private static ItineraryParams summarizeBicycleItinerary(Itinerary it, List<TravelExperience> ex) {
		double travelTime = 0.0;
		double costs = 0.0;
		double walkingDistance = 0.0;
		
		for (TravelExperience e : ex) {
			travelTime += e.getTravelTime();
			costs += e.getCosts();
			
			if (e.getTransportationMean() == TType.WALK)
				walkingDistance += e.getSegmentLength();
		}
		return new ItineraryParams(it.itineraryType, (long) travelTime, costs, 0.0,
				walkingDistance, 0);
	}
	
	private static ItineraryParams summarizeWalkItinerary(Itinerary it, List<TravelExperience> ex) {
		double travelTime = 0.0;
		double walkingDistance = 0.0;
		
		for (TravelExperience e : ex) {
			travelTime += e.getTravelTime();
			
			if (e.getTransportationMean() == TType.WALK)
				walkingDistance += e.getSegmentLength();
		}
		return new ItineraryParams(it.itineraryType, (long) travelTime, 0.0, 0.0, walkingDistance, 0);
	}
}
