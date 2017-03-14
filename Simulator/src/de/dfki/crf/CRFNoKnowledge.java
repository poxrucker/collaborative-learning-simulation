package de.dfki.crf;

import java.util.List;

import allow.simulator.knowledge.Experience;

public class CRFNoKnowledge implements IKnowledgeModel<Experience> {

	private static CRFNoKnowledge instance;
	
	private CRFNoKnowledge() { }
	
	public static CRFNoKnowledge getInstance() {
		if (instance == null)
			instance = new CRFNoKnowledge();
		return instance;
	}
	
  @Override
  public String getInstanceId() {
    return "";
  }
	
	@Override
	public boolean learn(List<Experience> dataPoints) {
		return true;
	}

	@Override
	public List<Experience> predict(List<Experience> observations) {
		return observations;
	}
	
	@Override
	public void clean() { }

	@Override
	public boolean merge(IKnowledgeModel<Experience> other) {
		return false;
	}
}
