package allow.simulator.adaptation;

public interface IAdaptationStrategy {

	/**
	 * Solves an issue for a given ensemble.
	 * 
	 * @param issue Issue to solve
	 * @param ensemble Ensemble of entities
	 */
	void solveAdaptation(Issue issue, Ensemble ensemble);
	
}
