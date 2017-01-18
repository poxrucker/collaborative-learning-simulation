package allow.simulator.adaptation;

import java.util.Collection;

public interface IGroupingAlgorithm {

	Collection<Group> formGroups(Ensemble ensemble);
	
}
