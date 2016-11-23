package allow.simulator.entity.knowledge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import allow.simulator.entity.Entity;
import allow.simulator.knowledge.TravelExperience;
import allow.simulator.knowledge.crf.DBConnector;
import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.mobility.planner.Leg;
import allow.simulator.mobility.planner.TType;
import allow.simulator.world.Street;
import allow.simulator.world.StreetSegment;
import allow.simulator.world.Weather;

public class Worker implements Callable<List<Itinerary>> {
	// Requests to send to the planner.
	public List<Itinerary> toUpdate;
	public Entity entity;
	
	// Latch to count down for thread synchronization.
	public CountDownLatch latch;

	public void prepare(Entity entity, List<Itinerary> toUpdate, CountDownLatch latch) {
		this.entity = entity;
		this.toUpdate = toUpdate;
		this.latch = latch;
	}
	
	public void reset() {
		toUpdate = null;
		latch = null;
		entity = null;
	}
	
	@Override
	public List<Itinerary> call() throws Exception {
		
		for (Itinerary it : toUpdate) {
			List<TravelExperience> ex = itineraryToTravelExperience(entity, it);
			
			try {
				DBConnector.getPredictedItinerary(entity, ex);
				
				it.priorSegmentation = new ArrayList<TravelExperience>(ex.size());
				
				for (TravelExperience e : ex) {
					it.priorSegmentation.add(e.clone());
				}
				updateItineraryFromTravelExperience(it, ex);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		latch.countDown();
		return toUpdate;
	}
	
	private static List<TravelExperience> itineraryToTravelExperience(Entity e, Itinerary it) {
		List<TravelExperience> ret = new ArrayList<TravelExperience>();
		Weather.State currentWeather = e.getContext().getWeather().getCurrentState();
		
		for (Leg l : it.legs) {
			long tStart = l.startTime;
			long tEnd = 0;
			
			if (l.streets.size() == 0) {
				double v = (l.mode == TType.WALK) ? StreetSegment.WALKING_SPEED 
								: ((l.mode == TType.BICYCLE || l.mode == TType.SHARED_BICYCLE)
										? StreetSegment.CYCLING_SPEED : StreetSegment.DEFAULT_DRIVING_SPEED);
				tEnd = (long) (tStart + l.distance / v);
				ret.add(new TravelExperience(tEnd - tStart, 0.0, l.mode, tStart, tEnd, -1, -1, null, currentWeather));
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
				TravelExperience t = new TravelExperience(street, travelTime,
						costs, l.mode, tStart, tEnd, -1, -1, l.tripId,
						currentWeather);
				ret.add(t);
				tStart = tEnd;
			}
		}
		return ret;
	}

	private static void updateItineraryFromTravelExperience(Itinerary it, List<TravelExperience> ex) {
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
				TravelExperience e = ex.get(i);
				
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
}
