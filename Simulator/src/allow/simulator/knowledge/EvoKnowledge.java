package allow.simulator.knowledge;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import allow.simulator.core.EvoKnowledgeConfig;
import allow.simulator.entity.Bus;
import allow.simulator.entity.Entity;
import allow.simulator.entity.Person;
import allow.simulator.exchange.ExchangeHandler;
import allow.simulator.knowledge.crf.DBConnector;
import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.util.Pair;
import allow.simulator.utility.ItineraryParams;
import allow.simulator.utility.Preferences;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Represents the knowledge of entities and implements functionalities of
 * Allow Ensembles Evolutionary Knowledge.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public class EvoKnowledge extends Knowledge implements IPredictor<List<Itinerary>, Boolean> {
	
	private static Queue<Pair<Entity, List<Itinerary>>> predictBuffer;
	private static List<Worker> tasks;
	private static WorkerPool workerPool;
	private static ExecutorService service;
	
	public static void initialize(EvoKnowledgeConfig config, String knowledgeModel, String prefix, ExecutorService service) {
		DBConnector.init(config, knowledgeModel, prefix);
		predictBuffer = new LinkedList<Pair<Entity, List<Itinerary>>>();
		tasks = new ArrayList<Worker>();
		workerPool = new WorkerPool(128);
		EvoKnowledge.service = service;
	}

	// Entity this knowledge instance belongs to.
	@JsonBackReference
	private Entity entity; 
	
	// Buffer holding entities to exchange knowledge with.
	private ObjectArrayList<Entity> toExchangeBuffer;
	
	// Chain of handlers to execute knowledge exchange.
	private ExchangeHandler handlerChain;
	
	/**
	 * Constructor.
	 * Creates a new instance of EvoKnowledge associated with an entity.
	 * 
	 * @param entity Entity this instance of EvoKnowledge is associated with.
	 */
	public EvoKnowledge(Entity entity) {
		this.entity = entity;
		toExchangeBuffer = new ObjectArrayList<Entity>();
		
		if (entity instanceof Person) {
			handlerChain = ExchangeHandler.StandardPersonChain;
			
		} else if (entity instanceof Bus) {
			handlerChain = ExchangeHandler.StandardBusChain;
		}
	}
	
	public String getInstanceId() {
		return "";
	}
	
	/**
	 * Adds a new vector of observations to EvoKnowledge. To learn from a set of
	 * stored experiences call learn() method.
	 * 
	 * @param observation New experience.
	 */
	/*public void collect(Experience observation) {
		
		if (observation instanceof TravelExperience) {
			travelExperienceBuffer.add((TravelExperience) observation);
		}
	}*/
	
	private static final double CAR_PREFERENCE_CHANGE_THRESHOLD1 = 600;
	private static final double CAR_PREFERENCE_CHANGE_THRESHOLD2 = 1200;
	private static final double CAR_PREFERENCE_CHANGE_THRESHOLD3 = 1800;
	private static final double CAR_PREFERENCE_CHANGE_STEP1 = 0.01;
	private static final double CAR_PREFERENCE_CHANGE_STEP2 = 0.03;
	private static final double CAR_PREFERENCE_CHANGE_STEP3 = 0.06;
	
	private static final double BUS_PREFERENCE_CHANGE_THRESHOLD1 = 0.7;
	private static final double BUS_PREFERENCE_CHANGE_THRESHOLD2 = 0.9;
	private static final double BUS_PREFERENCE_CHANGE_THRESHOLD3 = 1.0;
	private static final double BUS_PREFERENCE_CHANGE_STEP1 = 0.03;
	private static final double BUS_PREFERENCE_CHANGE_STEP2 = 0.05;
	private static final double BUS_PREFERENCE_CHANGE_STEP3 = 0.06;
	
	/**
	 * Update EvoKnowledge from the collected observations.
	 */
	public boolean learn(List<Experience> experiences) {

		// Handle statistics learning.
		if (entity instanceof Person) {
			Person p = (Person) entity;
			Itinerary it = p.getCurrentItinerary();
			DBConnector.addEntry(entity, experiences);
			ItineraryParams summary = EvoKnowledgeUtil.createFromExperiences(it, experiences);
			double estimatedTravelTime = it.duration + it.initialWaitingTime;
			Preferences prefs = p.getRankingFunction().getPreferences();
			double posteriorUtility = p.getRankingFunction().getUtilityFunction().computeUtility(summary, prefs);

			switch (it.itineraryType) {
			
				case CAR:
				case TAXI:
				case SHARED_TAXI:
					double actualCarTravelTime = summary.travelTime + it.initialWaitingTime;
					double actualDistance = summary.totalDistance;
					
					p.getContext().getStatistics().reportPriorAndPosteriorCarTravelTimes(estimatedTravelTime, actualCarTravelTime);					
					
					if (p.getOriginalTravelTime() > 0) {
						p.getContext().getStatistics().reportPriorAndPosteriorCarTravelTimesConstructionSiteRaw(p.getOriginalTravelTime(), summary.travelTimeRaw);
						p.getContext().getStatistics().reportPriorAndPosteriorCarTravelTimesConstructionSiteActual(p.getOriginalTravelTime(), summary.travelTime);
						p.setOriginalTravelTime(0);
						
						p.getContext().getStatistics().reportPriorAndPosteriorTripDistance(p.getOriginalTripDistance(), actualDistance);
						p.setOriginalTripDistance(0);
					}
					
					p.getContext().getStatistics().reportPriorAndPosteriorUtilityCar(it.utility, posteriorUtility);
					double carPreference = prefs.getCarPreference();
					double delay = actualCarTravelTime - estimatedTravelTime;

					if (delay > CAR_PREFERENCE_CHANGE_THRESHOLD3) {
						prefs.setCarPreference(carPreference - delay * CAR_PREFERENCE_CHANGE_STEP3 / CAR_PREFERENCE_CHANGE_THRESHOLD3);
					
					} else if (delay > CAR_PREFERENCE_CHANGE_THRESHOLD2) {
						prefs.setCarPreference(carPreference - CAR_PREFERENCE_CHANGE_STEP2 / CAR_PREFERENCE_CHANGE_THRESHOLD2);
					
					} else if (delay > CAR_PREFERENCE_CHANGE_THRESHOLD1) {
						prefs.setCarPreference(carPreference - CAR_PREFERENCE_CHANGE_STEP1 / CAR_PREFERENCE_CHANGE_THRESHOLD1);

					}
					
					// Reduce experienced bus filling level
					double prevFillingLevel = prefs.getLastExperiencedBusFillingLevel();
					prefs.setLastExperiencedBusFillingLevel(Math.max(prevFillingLevel - 0.2, 0));
					break;
				
				case BUS:
					double actualBusTravelTime = summary.travelTime + it.initialWaitingTime;
					p.getContext().getStatistics().reportPriorAndPosteriorTransitTravelTimes(estimatedTravelTime, actualBusTravelTime);
					p.getContext().getStatistics().reportPriorAndPosteriorUtilityBus(p.getCurrentItinerary().utility, posteriorUtility);

					p.getContext().getStatistics().reportBusFillingLevel(summary.maxBusFillingLevel);
					prefs.setLastExperiencedBusFillingLevel(summary.maxBusFillingLevel);
					double busPreference = prefs.getBusPreference();
					
					if (summary.maxBusFillingLevel >= BUS_PREFERENCE_CHANGE_THRESHOLD3) {
						prefs.setBusPreference(busPreference - summary.maxBusFillingLevel * BUS_PREFERENCE_CHANGE_STEP3 / BUS_PREFERENCE_CHANGE_THRESHOLD3);
						
					} else if (summary.maxBusFillingLevel >= BUS_PREFERENCE_CHANGE_THRESHOLD2) {
						prefs.setBusPreference(busPreference - BUS_PREFERENCE_CHANGE_STEP2 / BUS_PREFERENCE_CHANGE_THRESHOLD2);
						
					} else if (summary.maxBusFillingLevel >= BUS_PREFERENCE_CHANGE_THRESHOLD1) {
						prefs.setBusPreference(busPreference - BUS_PREFERENCE_CHANGE_STEP1 / BUS_PREFERENCE_CHANGE_THRESHOLD1);
					}
					break;
					
				case BICYCLE:
				case SHARED_BICYCLE:
					double actualBikeTravelTime = summary.travelTime;
					p.getContext().getStatistics().reportPriorAndPosteriorBikeTravelTimes(estimatedTravelTime, actualBikeTravelTime);
					
					// Reduce experienced bus filling level
					double prevFillingLevel2 = prefs.getLastExperiencedBusFillingLevel();
					prefs.setLastExperiencedBusFillingLevel(Math.max(prevFillingLevel2 - 0.2, 0));
					break;
					
				case WALK:
					double actualWalkTravelTime = summary.travelTime;
					p.getContext().getStatistics().reportPriorAndPosteriorWalkTravelTimes(estimatedTravelTime, actualWalkTravelTime);
					
					// Reduce experienced bus filling level
					double prevFillingLevel3 = prefs.getLastExperiencedBusFillingLevel();
					prefs.setLastExperiencedBusFillingLevel(Math.max(prevFillingLevel3 - 0.2, 0));
					break;
					
				default:
					System.out.print("Unclassified journey: ");
						for (int i = 0; i < p.getCurrentItinerary().legs.size(); i++) {
							System.out.print(p.getCurrentItinerary().legs.get(i).mode + " ");
						}
						System.out.println();
			}
		}
		return true;
	}
	
	/**
	 * Predicts the values of a set of parameters given a set of observations.
	 * 
	 * @param fromPlanner Itinerary as returned by the planner.
	 * @return Itinerary updated by EvoKnowledge.
	 */
	public Boolean predict(List<Itinerary> fromPlanner) {
		return predictBuffer.add(new Pair<Entity, List<Itinerary>>(entity, fromPlanner));
	}
	
	public static void cleanModel() {
		DBConnector.cleanModel(null);
	}
	
	public static void invokeRequest() {
		if (predictBuffer.size() == 0) {
			return;
		}
		
		CountDownLatch latch = new CountDownLatch(predictBuffer.size());
		
		while (predictBuffer.size() > 0) {
			Pair<Entity, List<Itinerary>> request = predictBuffer.poll();
			Worker w = workerPool.pop();
			w.prepare(request.first, request.second, latch);
			tasks.add(w);
		}
		
		try {
			service.invokeAll(tasks);
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (int j = 0; j < tasks.size(); j++) {
			Worker w = tasks.get(j);
			w.reset();
			workerPool.put(w);
		}
		tasks.clear();
	}
	
	public boolean exchangeKnowledge(Entity other) {
		return DBConnector.exchangeKnowledge(entity, other);
	}
	
	/**
	 * Executes knowledge exchange based on relations of the associated entity.
	 */
	public void exchangeKnowledge() {
		// Get relations.
		entity.getRelations().updateRelations(toExchangeBuffer);
		
		// Execute knowledge exchange.
		for (Entity other : toExchangeBuffer) {
				boolean exchanged = handlerChain.exchange(entity, other);
				
				if (exchanged) {
					entity.getRelations().addToBlackList(other);
					other.getRelations().addToBlackList(entity);
				}
			}
		// Clear relations buffer.
		toExchangeBuffer.clear();
		toExchangeBuffer.trim();
	}
}
