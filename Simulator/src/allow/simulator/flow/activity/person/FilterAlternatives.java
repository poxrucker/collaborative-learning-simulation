package allow.simulator.flow.activity.person;

import java.util.List;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.mobility.planner.Itinerary;

public final class FilterAlternatives extends Activity<Person> {
	// List of itineraries to filter
	private List<Itinerary> toFilter;
	
	public FilterAlternatives(Person person, List<Itinerary> it) {
		super(ActivityType.FILTER_ALTERNATIVES, person);
		toFilter = it;
	}

	@Override
	public double execute(double deltaT) {
		/* 
		 * Do filtering here 
		 */
		entity.getFlow().addActivity(new RankAlternatives(entity, toFilter));
		setFinished();
		return 0;
	}

	public String toString() {
		return "FilterAlternatives " + entity;
	}
}
