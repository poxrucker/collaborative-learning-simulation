package allow.simulator.adaptation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class AdaptationManager {
	// Collection of all ensembles registered to this EnsembleManager instance
	private final Map<String, Ensemble> ensembles;
	
	// Adaptation strategy to run
	private final IAdaptationStrategy adaptationStrategy;
	
	/**
	 * Creates a new instance of an AdaptationManager.
	 */
	public AdaptationManager(IAdaptationStrategy adaptationStrategy) {
		ensembles = new HashMap<String, Ensemble>();
		this.adaptationStrategy = adaptationStrategy;
	}
	
	/**
	 * Creates a new ensemble with given Id and creator and adds it to the 
	 * collection of ensembles maintained within the AdaptationManager.
	 * 
	 * @param creator Entity which is the creator of the ensemble
	 * @param ensembleId Id of the ensemble
	 * @return New ensemble instance
	 */
	public Ensemble createEnsemble(IEnsembleParticipant creator, String ensembleId) {		
		if (ensembles.containsKey(ensembleId))
			throw new IllegalStateException("Error: Ensemble " + ensembleId + " already exists.");
		
		Ensemble ensemble = new Ensemble(creator);
		ensembles.put(ensembleId, ensemble);
		return ensemble;
	}
	
	/**
	 * Removes the ensemble with the given Id from this instance.
	 * 
	 * @param ensembleId Id of ensemble to remove
	 * @return True if ensemble was removed, false if no ensemble with the
	 * given Id is registered in this AdaptationManager instance
	 */
	public boolean terminateEnsemble(String ensembleId) {
		return (ensembles.remove(ensembleId) != null);
	}
	
	/**
	 * Returns the ensemble with the given Id.
	 * 
	 * @param ensembleId Id of ensemble to return
	 * @return Ensemble with given Id or null, if no ensemble with the given
	 * Id is registered in this AdaptationManager instance
	 */
	public Ensemble getEnsemble(String ensembleId) {
		return ensembles.get(ensembleId);
	}
	
	public void runAdaptations() {
		final Collection<Ensemble> temp = ensembles.values();
		
		for (Ensemble ensemble : temp) {
			Issue issue = ensemble.getCreator().getTriggeredIssue();
			
			if (issue != Issue.NONE)
				adaptationStrategy.solveAdaptation(issue, ensemble);
		}
	}
}