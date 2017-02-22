package de.dfki.crf;

import java.util.List;

import allow.simulator.entity.Entity;
import allow.simulator.knowledge.Experience;

public interface CRFKnowledgeModel {

	boolean addEntry(Entity agent, List<Experience> entries, String tablePrefix);
	
	List<Experience> getPredictedItinerary(Entity agent, List<Experience> it, String tablePrefix);
	
	boolean exchangeKnowledge(Entity agent1, Entity agent2, String tablePrefix);
	
	void clean(Entity entity, String tablePrefix);
	
}
