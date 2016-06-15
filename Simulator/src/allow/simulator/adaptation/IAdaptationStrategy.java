package allow.simulator.adaptation;

import java.io.FileNotFoundException;

public interface IAdaptationStrategy {

	/**
	 * Solves an issue for a given ensemble.
	 * 
	 * @param issue Issue to solve
	 * @param ensemble Ensemble of entities
	 * @throws FileNotFoundException 
	 */
	void solveAdaptation(Issue issue, Ensemble ensemble) throws FileNotFoundException;
	
}
