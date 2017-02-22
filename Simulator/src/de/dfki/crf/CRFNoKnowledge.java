package de.dfki.crf;

import java.util.List;

import allow.simulator.entity.Entity;
import allow.simulator.knowledge.Experience;

public class CRFNoKnowledge implements CRFKnowledgeModel {

	private static CRFNoKnowledge instance;
	
	private CRFNoKnowledge() { }
	
	public static CRFNoKnowledge getInstance() {
		if (instance == null)
			instance = new CRFNoKnowledge();
		return instance;
	}
	
	@Override
	public boolean addEntry(Entity agent, List<Experience> entries, String tablePrefix) {
		return true;
	}

	@Override
	public List<Experience> getPredictedItinerary(Entity agent, List<Experience> it, String tablePrefix) {
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
