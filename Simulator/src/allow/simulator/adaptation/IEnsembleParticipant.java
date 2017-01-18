package allow.simulator.adaptation;


public interface IEnsembleParticipant {

	long getParticipantId();

	Issue getTriggeredIssue();
	
	void triggerIssue(Issue issue);
		
}
