package allow.simulator.flow.activity.person;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.util.Coordinate;

public class CorrectPosition extends Activity<Person> {

	private Coordinate destination;
	
	public CorrectPosition(Person entity, Coordinate destination) {
		super(ActivityType.CORRECT_POSITION, entity);
		this.destination = destination;
	}

	@Override
	public double execute(double deltaT) {
		entity.setPosition(destination);

		if (entity.hasUsedCar() && entity.isAtHome())
			entity.setUsedCar(false);
		
		setFinished();
		return 0.0;
	}
}
