package allow.simulator.adaptation;

import java.util.List;

public class Group {
	List<IEnsembleParticipant> participants;
	IEnsembleParticipant leader;

	public Group(IEnsembleParticipant creator,
			List<IEnsembleParticipant> members) {
		this.leader = creator;
		this.participants = members;

	}

	public List<IEnsembleParticipant> getParticipants() {
		return participants;
	}

	public void setParticipants(List<IEnsembleParticipant> participants) {
		this.participants = participants;
	}

	public IEnsembleParticipant getLeader() {
		return leader;
	}

	public void setLeader(IEnsembleParticipant leader) {
		this.leader = leader;
	}
}
