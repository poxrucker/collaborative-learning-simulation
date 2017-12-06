package allow.simulator.flow.activity.person;

import java.util.List;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.knowledge.EvoKnowledge;
import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.utility.Preferences;

public class RankAlternatives extends Activity<Person> {
	// Itineraries to rank
	private List<Itinerary> toRank;
	
	// Indicates, if preferences and itinerary parameters have already been updated
	private boolean updated;
	
	public RankAlternatives(Person entity, List<Itinerary> it) {
		super(ActivityType.RANK_ALTERNATIVES, entity);
		toRank = it;
	}

	@Override
	public double execute(double deltaT) {

		if (!updated) {
			updatePreferences();
			updateItineraryParameters();
			updated = true;
			return deltaT;
		}
		toRank = entity.getRankingFunction().reason(toRank);
		entity.getFlow().addActivity(new PrepareJourney(entity, toRank.get(0)));
		setFinished();
		return 0.0;
	}
	
	private void updateItineraryParameters() {
		EvoKnowledge evo = entity.getKnowledge(); // Correct journey parameters before calling utility function
		evo.predict(toRank);
	}
	
	private void updatePreferences() {
		long minTTime = Long.MAX_VALUE;
		double minCosts = Double.MAX_VALUE;
		double minWalking = Double.MAX_VALUE;
		
		for (Itinerary it : toRank) {
			
			if (it.duration < minTTime) 
				minTTime = it.duration;
			
			if (it.costs > 0 && it.costs < minCosts) 
				minCosts = it.costs;
			
			if (it.walkDistance > 0 && it.walkDistance < minWalking) 
				minWalking = it.walkDistance;
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
