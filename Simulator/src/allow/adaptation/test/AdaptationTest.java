package allow.adaptation.test;

import java.io.FileNotFoundException;

import allow.simulator.adaptation.CollectiveAdaptation;
import allow.simulator.adaptation.Ensemble;
import allow.simulator.adaptation.Issue;
import allow.simulator.entity.Entity;
import allow.simulator.entity.PublicTransportation;

public class AdaptationTest {

	public static void main(String[] args) {

		CollectiveAdaptation ca = new CollectiveAdaptation();

		Entity bus = new PublicTransportation(1, null, null, null, 25);
		// Entity passenger1 = new Person(2, Gender.MALE, Profile.WORKER, null,
		// null, null, true, true, true, null, null);
		// Entity passenger2 = new Person(3, Gender.MALE, Profile.WORKER, null,
		// null, null, true, true, true, null, null);

		Ensemble ensemble = new Ensemble(bus);
		// ensemble.addEntity(passenger1);
		// ensemble.addEntity(passenger2);

		Issue issue = Issue.BUS_BREAKDOWN;
		try {
			ca.solveAdaptation(issue, ensemble);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
