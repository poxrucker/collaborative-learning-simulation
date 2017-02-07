package allow.simulator.flow.activity.person;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;

public final class Replan extends Activity<Person> {

	public Replan(Person p) {
		super(ActivityType.REPLAN, p);
	}

	@Override
	public double execute(double deltaT) {		
		// Clear flow of activities and knowledge.
		entity.getFlow().clear();
		entity.getExperienceBuffer().clear();
		entity.getExperienceBuffer().trimToSize();
		
		// Add new planning activity.
		entity.getFlow().addActivity(new PlanJourney(entity, entity.getPosition(), entity.getCurrentItinerary().to));
		entity.setCurrentItinerary(null);
		entity.setReplanning(true);
		setFinished();
		return 0.0;
	}
}
