package allow.simulator.entity.knowledge.crf;

import java.util.List;

import allow.simulator.entity.Entity;
import allow.simulator.entity.knowledge.TravelExperience;

public interface CRFKnowledgeModel {

	boolean addEntry(Entity agent, List<TravelExperience> prior, List<TravelExperience> posterior, String tablePrefix);
	
	List<TravelExperience> getPredictedItinerary(Entity agent, List<TravelExperience> it, String tablePrefix);
	
	boolean exchangeKnowledge(Entity agent1, Entity agent2, String tablePrefix);
	
	void clean(Entity entity, String tablePrefix);
	
}
