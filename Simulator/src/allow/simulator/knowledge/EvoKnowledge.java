package allow.simulator.knowledge;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import de.dfki.crf.DBConnector;
import allow.simulator.core.EvoKnowledgeConfiguration;
import allow.simulator.entity.Entity;
import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.mobility.planner.Leg;
import allow.simulator.mobility.planner.TType;
import allow.simulator.world.Street;
import allow.simulator.world.StreetSegment;
import allow.simulator.world.Weather;

/**
 * Represents the knowledge of entities and implements functionalities of
 * Allow Ensembles Evolutionary Knowledge.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class EvoKnowledge implements IPredictor<List<Itinerary>, Future<List<Itinerary>>>, ITrainable<List<Experience>, Future<Void>> {
	
	private static ExecutorService service;
	
	public static void initialize(EvoKnowledgeConfiguration config, String knowledgeModel, String prefix, ExecutorService service) {
		DBConnector.init(config, knowledgeModel, prefix);
		EvoKnowledge.service = service;
	}

	// Entity this knowledge instance belongs to.
	private Entity entity; 
	
	/**
	 * Creates a new instance of EvoKnowledge associated with an entity.
	 * 
	 * @param entity Entity this instance of EvoKnowledge is associated with.
	 */
	public EvoKnowledge(Entity entity) {
		this.entity = entity;
	}
	
	/**
	 * Update EvoKnowledge from the collected observations.
	 */
	public Future<Void> learn(List<Experience> experiences) {
		return service.submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				DBConnector.addEntry(entity, experiences);
				return null;
			}
			
		});
	}
	
	/**
	 * Predicts the values of a set of parameters given a set of observations.
	 * 
	 * @param fromPlanner Itinerary as returned by the planner.
	 * @return Itinerary updated by EvoKnowledge.
	 */
	public Future<List<Itinerary>> predict(List<Itinerary> toPredict) {
		return service.submit(new Callable<List<Itinerary>>() {

			@Override
			public List<Itinerary> call() throws Exception {
				for (Itinerary it : toPredict) {
					List<Experience> ex = itineraryToTravelExperience(entity, it);
					
					try {
						DBConnector.getPredictedItinerary(entity, ex);
						updateItineraryFromTravelExperience(it, ex);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return toPredict;
			}
		});
	}
	
	private static List<Experience> itineraryToTravelExperience(Entity e, Itinerary it) {
		List<Experience> ret = new ObjectArrayList<Experience>();
		Weather.State currentWeather = e.getContext().getWeather().getCurrentState();
		
		for (Leg l : it.legs) {
			long tStart = l.startTime;
			long tEnd = 0;
			
			if (l.streets.size() == 0) {
				double v = StreetSegment.DEFAULT_DRIVING_SPEED;
				
				if (l.mode == TType.WALK)
					v = StreetSegment.WALKING_SPEED; 
				else if ((l.mode == TType.BICYCLE) || (l.mode == TType.SHARED_BICYCLE))
					v = StreetSegment.CYCLING_SPEED;
				
				tEnd = (long) (tStart + l.distance / v);
				ret.add(new Experience(tEnd - tStart, 0.0, l.mode, tStart, tEnd, -1, -1, null, currentWeather));
				continue;
			}
			
			for (Street street : l.streets) {
				double v = (l.mode == TType.WALK) ? street.getSubSegments()
						.get(0).getWalkingSpeed()
						: ((l.mode == TType.BICYCLE || l.mode == TType.SHARED_BICYCLE) ? street.getSubSegments()
								.get(0).getCyclingSpeed() : street
								.getSubSegments().get(0).getMaxSpeed());
				double travelTime = street.getLength() / v;
				double costs = l.costs * (street.getLength() / l.distance);
				tEnd = (long) (tStart + travelTime * 1000);
				Experience t = new Experience(street, travelTime,
						costs, l.mode, tStart, tEnd, -1, -1, l.tripId,
						currentWeather);
				ret.add(t);
				tStart = tEnd;
			}
		}
		return ret;
	}

	private static void updateItineraryFromTravelExperience(Itinerary it, List<Experience> ex) {
		if (ex.size() == 0) {
			return;
		}
		it.waitingTime = 0;
		int exIndex = 0;
		long legStartTime = 0;
		long legEndTime = 0;
		boolean first = true;
	
		for (Leg l : it.legs) {
			
			if (first || l.mode == TType.BUS || l.mode == TType.CABLE_CAR) {
				legStartTime = l.startTime;
				legEndTime = legStartTime;

			} else {
				legStartTime = legEndTime;
			}
			
			int added = 0;
			for (int i = exIndex; i < ex.size(); i++) {		
				Experience e = ex.get(i);
				
				if (e.isTransient())
					break;
				
				// In case transportation means changes.
				if ((e.getTransportationMean() != l.mode)) {
					break;
				}
				
				// In case these is an intermediate bus change.
				if ((l.mode == TType.BUS || l.mode == TType.CABLE_CAR) && !l.tripId.equals(e.getPublicTransportationTripId())) {
					break;
				}
				double duration = e.getTravelTime() * 1000;
				it.maxFillingLevel = Math.max(e.getPublicTransportationFillingLevel(), it.maxFillingLevel);
				legEndTime += duration;
				added++;
			}
			exIndex += added;
			
			if (added == 0) {
				legEndTime += ex.get(exIndex++).getTravelTime() * 1000;
			}
			first = false;
			l.startTime = legStartTime;
			l.endTime = legEndTime;
		}
		
		// Update itinerary time.
		it.startTime = it.legs.get(0).startTime;
		it.endTime = it.startTime;
		
		for (Leg l : it.legs) {
			long duration = (l.endTime - l.startTime);
			it.endTime += duration;
		}
		it.duration = ((it.endTime - it.startTime) / 1000);
		// Compute waiting time.
		for (int i = 0; i < it.legs.size() - 1; i++) {
			it.waitingTime += ((it.legs.get(i + 1).startTime - it.legs.get(i).endTime) / 1000);
		}
		it.duration += (it.waitingTime / 1000);
	}
	
	public Future<Boolean> exchangeKnowledge(Entity other) {
		return service.submit(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return DBConnector.exchangeKnowledge(entity, other);
			}
		});
	}
}
