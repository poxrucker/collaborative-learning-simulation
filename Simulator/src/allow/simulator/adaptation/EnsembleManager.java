package allow.simulator.adaptation;

import java.util.HashMap;
import java.util.Map;

import allow.simulator.entity.Entity;

public final class EnsembleManager {
	// Collection of all ensembles registered to this EnsembleManager instance
	private Map<String, Ensemble> ensembles;
	
	/**
	 * Creates a new instance of an EnsembleManager.
	 */
	public EnsembleManager() {
		ensembles = new HashMap<String, Ensemble>();
	}
	
	/**
	 * Creates a new ensemble with given Id and creator and adds it to the 
	 * collection of ensembles maintained within the EnsembleManager.
	 * 
	 * @param creator Entity which is the creator of the ensemble
	 * @param ensembleId Id of the ensemble
	 * @return New ensemble instance
	 */
	public Ensemble createEnsemble(Entity creator, String ensembleId) {		
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
	 * given Id is registered in this EnsembleManager instance
	 */
	public boolean terminateEnsemble(String ensembleId) {
		return (ensembles.remove(ensembleId) != null);
	}
	
	/**
	 * Returns the ensemble with the given Id.
	 * 
	 * @param ensembleId Id of ensemble to return
	 * @return Ensemble with given Id or null, if no ensemble with the given
	 * Id is registered in this EnsembleManager instance
	 */
	public Ensemble getEnsemble(String ensembleId) {
		return ensembles.get(ensembleId);
	}
	
}
