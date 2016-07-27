package allow.simulator.entity.knowledge.crf;

import java.util.List;

import allow.simulator.entity.Entity;
import allow.simulator.entity.knowledge.TravelExperience;

public class CRFNoKnowledge implements CRFKnowledgeModel {

	private static CRFNoKnowledge instance;
	
	private CRFNoKnowledge() { }
	
	public static CRFNoKnowledge getInstance() {
		if (instance == null)
			instance = new CRFNoKnowledge();
		return instance;
	}
	
	@Override
	public boolean addEntry(Entity agent, List<TravelExperience> prior, List<TravelExperience> experiences, String tablePrefix) {
		return true;
	}

	@Override
	public List<TravelExperience> getPredictedItinerary(Entity agent, List<TravelExperience> it, String tablePrefix) {
		return it;
	}
	
	@Override
	public void clean(Entity agent, String tablePrefix) {
		
	}

	@Override
	public boolean exchangeKnowledge(Entity agent1, Entity agent3, String tablePrefix) {
		return false;
	}
}
