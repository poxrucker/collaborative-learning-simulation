package allow.adaptation.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import allow.adaptation.EnsembleManager;
import allow.adaptation.RoleManager;
import allow.adaptation.presentation.CATree;

public interface CollectiveAdaptationInterface {

	public HashMap<CATree, Integer> executeCap(
			CollectiveAdaptationProblem cap,
			CollectiveAdaptationCommandExecution executor,
			String scenario,
			HashMap<RoleManager, HashMap<String, ArrayList<Integer>>> GlobalResult,
			CATree cat, DirectedGraph<String, DefaultEdge> graph,
			DefaultEdge startEdge, List<EnsembleManager> ensembles);

	public void executeCapNew(CollectiveAdaptationProblem cap,
			CollectiveAdaptationCommandExecution executor);

}
