package allow.simulator.flow.activity.person;

import java.util.List;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.knowledge.EvoKnowledge;
import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.utility.Preferences;

public class RankAlternatives extends Activity<Person> {
	// Itineraries to rank
	private final List<Itinerary> toRank;
	
	private boolean preferencesUpdated;
	
	public RankAlternatives(Person entity, List<Itinerary> it) {
		super(ActivityType.RANK_ALTERNATIVES, entity);
		toRank = it;
		preferencesUpdated = false;
	}

	@Override
	public double execute(double deltaT) {

		if (!preferencesUpdated) {
		  preferencesUpdated = true;
			updatePreferences();
			updateItineraryParameters();
			return deltaT;
		}
		List<Itinerary> ret = entity.getRankingFunction().reason(toRank);
		entity.getFlow().addActivity(new PrepareJourney(entity, ret.get(0)));
		setFinished();
		return 0.0;
	}
	
	private void updateItineraryParameters() {
		// Correct journey parameters before calling utility function.
		EvoKnowledge evo = entity.getKnowledge();
		evo.predict(toRank, entity.getContext());
	}
	
	private void updatePreferences() {
		long minTTime = Long.MAX_VALUE;
		double minCosts = Double.MAX_VALUE;
		double minWalking = Double.MAX_VALUE;
		
		for (Itinerary it : toRank) {
			// if (it.itineraryType == 2 || it.itineraryType == 3) continue;
			
			if (it.duration < minTTime) minTTime = it.duration;
			if (it.costs > 0 && it.costs < minCosts) minCosts = it.costs;
			if (it.walkDistance > 0 && it.walkDistance < minWalking) minWalking = it.walkDistance;
		}
		Preferences prefs = entity.getRankingFunction().getPreferences();
		prefs.setTmax((long) (minTTime * 1.2));
		prefs.setCmax(minCosts * 1.2);
		prefs.setWmax(minWalking * 1.2);
		
	}
	
	public String toString() {
		return "RankAlternatives " + entity;
	}
}
