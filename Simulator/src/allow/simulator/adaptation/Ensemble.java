package allow.simulator.adaptation;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;

import java.util.Collection;
import java.util.Collections;

public final class Ensemble {
	// Ensemble participant which created the ensemble (responsible for
	// terminating it afterwards)
	private IEnsembleParticipant creator;
	
	// Entities participating in the ensemble including the creator
	private Long2ObjectArrayMap<IEnsembleParticipant> participants;
	
	/**
	 * Creates a new instance of an ensemble structure with the specified
	 * participant set as creator which is responsible to terminate the ensemble
	 * in the end.
	 * 
	 * @param creator Participant which creates the ensemble
	 */
	public Ensemble(IEnsembleParticipant creator) {
		this.creator = creator;
		participants = new Long2ObjectArrayMap<IEnsembleParticipant>();
		participants.put(creator.getParticipantId(), creator);
	}
	
	/**
	 * Adds a new participant to the ensemble.
	 * 
	 * @param entity Participant to add to the ensemble
	 */
	public void addEntity(IEnsembleParticipant participant) {
		participants.put(participant.getParticipantId(), participant);
	}
	
	/**
	 * Removes the given participant from the ensemble. If the participant 
	 * is not part of the ensemble, calling this method has no effect.
	 * 
	 * @param participant Participant to remove from the ensemble
	 * @return True if participant was removed, false if participant is not
	 * part of the ensemble
	 */
	public boolean removeEntity(IEnsembleParticipant participant) {
		return (participants.remove(participant.getParticipantId()) != null);
	}
	
	/**
	 * Returns the participant which has created the ensemble.
	 * 
	 * @return Participant which has created the ensemble
	 */
	public IEnsembleParticipant getCreator() {
		return creator;
	}
	
	/**
	 * Returns the set of participants of the ensemble including the creator.
	 * 
	 * @return Set of participants of the ensemble including the creator
	 */
	public Collection<IEnsembleParticipant> getEntities() {
		return Collections.unmodifiableCollection(participants.values());
	}
	
}
