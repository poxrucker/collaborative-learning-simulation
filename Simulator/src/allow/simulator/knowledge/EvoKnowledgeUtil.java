package allow.simulator.knowledge;

import java.util.List;

import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.mobility.planner.TType;
import allow.simulator.utility.ItineraryParams;

public final class EvoKnowledgeUtil {

	public static ItineraryParams createFromExperiences(Itinerary prior, List<Experience> ex) {
		
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
	
	private static ItineraryParams summarizeCarItinerary(Itinerary it, List<Experience> ex) {
		double travelTime = 0.0;
		double costs = 0.0;
		int numberOfTransfers = 0;
		double walkingDistance = 0.0;
		double totalDistance = 0.0;
		
		for (Experience e : ex) {
			travelTime += e.getTravelTime();
			costs += e.getCosts();
			numberOfTransfers = 0;
			totalDistance += e.getSegmentLength();
			
			if (e.getTransportationMean() == TType.WALK)
				walkingDistance += e.getSegmentLength();
		}
		return new ItineraryParams(it.itineraryType, (long) travelTime, costs, 0.0,
				walkingDistance, numberOfTransfers, totalDistance);
	}
	
	private static ItineraryParams summarizeBusItinerary(Itinerary it, List<Experience> ex) {
		double travelTime = 0.0;
		double busFillingLevel = 0.0;
		double walkingDistance = 0.0;
		double totalDistance = 0.0;
		
		for (Experience e : ex) {
			travelTime += e.getTravelTime();
			totalDistance += e.getSegmentLength();
			
			if (e.getTransportationMean() == TType.WALK)
				walkingDistance += e.getSegmentLength();
			
			if (e.getTransportationMean() == TType.BUS) 
				busFillingLevel = Math.max(busFillingLevel, e.getPublicTransportationFillingLevel());
		}
		return new ItineraryParams(it.itineraryType, (long) travelTime, 1.2, busFillingLevel,
				walkingDistance, it.transfers, totalDistance);
	}
	
	private static ItineraryParams summarizeBicycleItinerary(Itinerary it, List<Experience> ex) {
		double travelTime = 0.0;
		double costs = 0.0;
		double walkingDistance = 0.0;
		double totalDistance = 0.0;
		
		for (Experience e : ex) {
			travelTime += e.getTravelTime();
			costs += e.getCosts();
			totalDistance += e.getSegmentLength();
			
			if (e.getTransportationMean() == TType.WALK)
				walkingDistance += e.getSegmentLength();
		}
		return new ItineraryParams(it.itineraryType, (long) travelTime, costs, 0.0,
				walkingDistance, 0, totalDistance);
	}
	
	private static ItineraryParams summarizeWalkItinerary(Itinerary it, List<Experience> ex) {
		double travelTime = 0.0;
		double walkingDistance = 0.0;
		double totalDistance = 0.0;
		
		for (Experience e : ex) {
			travelTime += e.getTravelTime();
			totalDistance += e.getSegmentLength();
			
			if (e.getTransportationMean() == TType.WALK)
				walkingDistance += e.getSegmentLength();
		}
		return new ItineraryParams(it.itineraryType, (long) travelTime, 0.0, 0.0, 
				walkingDistance, 0, totalDistance);
	}
}
