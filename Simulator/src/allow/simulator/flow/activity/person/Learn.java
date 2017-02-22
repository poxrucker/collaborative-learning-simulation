package allow.simulator.flow.activity.person;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import allow.simulator.entity.Entity;
import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.knowledge.EvoKnowledgeUtil;
import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.utility.ItineraryParams;
import allow.simulator.utility.Preferences;

/**
 * Represents an activity to execute learning.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class Learn extends Activity<Entity> {
	// Preference change thresholds
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
	
	// Future to indicate that learning has been initiated
	private Future<Void> learningFuture;
	
	public Learn(Entity entity) {
		super(ActivityType.LEARN, entity);
	}

	@Override
	public double execute(double deltaT) {
		
		if (!(entity instanceof Person)) {
			setFinished();
			return 0.0;
		}
		// Get entity
		Person p = (Person)entity;
		
		// Learn from experiences
		if (learningFuture == null) {
			learningFuture = p.getKnowledge().learn(p.getExperienceBuffer());
			return deltaT;
		}
		
		// Wait for learning to complete
		try {
			learningFuture.get();
			
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		// Update preferences
		updatePreferences();
		
		// Clear experiences buffer
		p.getExperienceBuffer().clear();
		
		// Create summary of 
		p.getKnowledge().learn(p.getExperienceBuffer());
		p.getExperienceBuffer().clear();
		
		// Summarize 
		setFinished();
		return 0.0;
	}
	
	private void updatePreferences() {
		Person p = (Person)entity;
		Itinerary it = p.getCurrentItinerary();
		ItineraryParams summary = EvoKnowledgeUtil.createFromExperiences(it, p.getExperienceBuffer());
		double estimatedTravelTime = it.duration + it.initialWaitingTime;
		Preferences prefs = p.getRankingFunction().getPreferences();
		double posteriorUtility = p.getRankingFunction().getUtilityFunction().computeUtility(summary, prefs);

		switch (it.itineraryType) {
			case CAR:
			case TAXI:
			case SHARED_TAXI:
				double actualCarTravelTime = summary.travelTime + it.initialWaitingTime;
				p.getContext().getStatistics().reportPriorAndPosteriorCarTravelTimes(estimatedTravelTime, actualCarTravelTime);					
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
}
