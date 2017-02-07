package allow.simulator.flow.activity.person;

import allow.simulator.entity.Entity;
import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;

/**
 * Represents an activity to execute learning.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class Learn extends Activity<Entity> {
	
	public Learn(Entity entity) {
		super(ActivityType.LEARN, entity);
	}

	@Override
	public double execute(double deltaT) {
		
		if (!(entity instanceof Person)) {
			setFinished();
			return 0.0;
		}
		Person p = (Person)entity;
		
		// Create summary of 
		p.getKnowledge().learn(p.getExperienceBuffer());
		p.getExperienceBuffer().clear();
		
		// Summarize 
		setFinished();
		return 0.0;
	}
}
