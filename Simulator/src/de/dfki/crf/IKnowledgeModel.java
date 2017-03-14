package de.dfki.crf;

import java.util.List;

public interface IKnowledgeModel<E> {

  String getInstanceId();
  
	boolean learn(List<E> dataPoints);
	
	List<E> predict(List<E> observations);
	
	boolean merge(IKnowledgeModel<E> other);
	
	void clean();
	
}
