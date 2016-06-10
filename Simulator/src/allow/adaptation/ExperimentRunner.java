package allow.adaptation;

import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import org.jgraph.JGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.tree.JGraphTreeLayout;
import com.mxgraph.analysis.mxAnalysisGraph;
import com.mxgraph.analysis.mxTraversal;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraph.mxICellVisitor;

import allow.adaptation.api.CollectiveAdaptationProblem;
import allow.adaptation.api.CollectiveAdaptationRole;
import allow.adaptation.ensemble.Issue;
import allow.adaptation.ensemble.Role;
import allow.adaptation.model.IssueResolution;
import allow.adaptation.presentation.CATree;
import allow.adaptation.presentation.CAWindow;

public class ExperimentRunner {

    private static ExperimentRunner instance;
    private static long memoryBaseline;
    // private static double cpuLoadBaseline;
    private static ThreadMXBean threadBean;
    // private static Runtime runtime;
    private static MemoryMXBean memoryBean;

    private static int distance = 0;
    private static int depth = 0;

    private static int miniumExtent = 1;
    private static int maximumExtent = 1;
    private static boolean printed = false;

    public static ExperimentRunner getInstance() {
	if (ExperimentRunner.instance == null) {
	    // System.gc();
	    ExperimentRunner.instance = new ExperimentRunner();
	    ExperimentRunner.threadBean = ManagementFactory.getThreadMXBean();// getPlatformMXBean(OperatingSystemMXBean.class);
	    ExperimentRunner.memoryBean = ManagementFactory.getMemoryMXBean();
	    // ExperimentRunner.runtime = Runtime.getRuntime();

	    System.gc();
	    // ExperimentRunner.memoryBaseline =
	    // ExperimentRunner.runtime.totalMemory() -
	    // ExperimentRunner.runtime.freeMemory();
	    // ExperimentRunner.processCpuLoadBaseline =
	    // ExperimentRunner.osMean.getProcessCpuLoad();

	    ExperimentRunner.memoryBaseline = ExperimentRunner.memoryBean.getHeapMemoryUsage().getUsed();
	    // System.gc();

	}
	return ExperimentRunner.instance;

    }

    // Run of single treatment with a set of issues
    public ExperimentResult run(CollectiveAdaptationProblem cap, Treatment treatment, List<EnsembleManager> ensembles,
	    CAWindow window, String scenario, HashMap<RoleManager, HashMap<String, ArrayList<Integer>>> GlobalResult) {

	System.gc();
	try {
	    Thread.sleep(200);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

	// System.gc();

	ExperimentResult result = new ExperimentResult();

	// set id
	result.setId(treatment.getId());

	// start logging the CPU usage for filling dv9
	// double startCPU = ExperimentRunner.osMean.getProcessCpuLoad();
	long startCPU = ExperimentRunner.threadBean.getCurrentThreadCpuTime();

	// start logging the time for setting dv1 of the result
	long startTime = System.nanoTime();

	int totalWidth = 0;
	int totalDepth = 0;
	int numOfEnsembles = 0;
	int numOfRoles = 0;

	for (int t = 0; t < treatment.getIssues().size(); t++) {

	    int countIndex = 0;

	    Issue issue = treatment.getIssues().get(t);
	    RoleManager r = null;
	    if (scenario.equalsIgnoreCase("drones")) {
		r = Utilities.pickRoleForIssueDrones(issue);
	    } else {
		r = Utilities.pickRoleForIssueMobility(issue);
	    }

	    // restart the cross ensemble counter
	    r.setCrossEnsembleIssues(0);
	    r.setMinDepth(0);
	    r.setMaxDepth(0);
	    r.setMinExtent(0);
	    r.setMaxExtent(0);

	    // [controlla]
	    // System.out.println("ROLE: " + r.getRole().getType());

	    IssueResolution resolution1 = new IssueResolution(1, "ISSUE_TRIGGERED", r, r, issue, null);
	    r.addIssueResolution(resolution1);

	    // add the issueresolution to the right Ensemble
	    for (int i = 0; i < ensembles.size(); i++) {
		for (int j = 0; j < ensembles.get(i).getRolesManagers().size(); j++) {
		    RoleManager currentManager = ensembles.get(i).getRolesManagers().get(j);
		    if (currentManager.getRole().getType().equalsIgnoreCase(r.getRole().getType())) {
			ArrayList<IssueResolution> resolutions = new ArrayList<IssueResolution>();
			resolutions.add(resolution1);
			ensembles.get(i).setActiveIssueResolutions(resolutions);
			String capID = Integer.toString(treatment.getId());
			// window.updateResolutions(capID, resolution1,
			// ensembles.get(i));
			// window.loadActiveIssueResolutionsTable(ensembles.get(i).getActiveIssueResolutions(),
			// ensembles.get(i));
			// window.loadTreeFrame();

			DemonstratorAnalyzer demo = new DemonstratorAnalyzer();

			List<CollectiveAdaptationRole> roles = new ArrayList<CollectiveAdaptationRole>();
			for (int k = 0; k < ensembles.get(i).getRolesManagers().size(); k++) {
			    RoleManager rm = ensembles.get(i).getRolesManagers().get(k);
			    Role r1 = rm.getRole();
			    CollectiveAdaptationRole car = new CollectiveAdaptationRole();
			    car.setRole(r1.getType());
			    roles.add(car);
			}

			cap.setIssue(issue.getIssueType());
			cap.setStartingRole(r.getRole().getType());

			// execution of a single ISSUE in the current Treatment
			// it returns the resolution tree
			CATree cat = new CATree();
			// System.out.println(cat.getNodesHierarchy().size());
			DirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<String, DefaultEdge>(
				DefaultEdge.class);
			DefaultEdge startEdge = null;
			HashMap<CATree, Integer> solution = new HashMap<CATree, Integer>();

			solution = demo.executeCap(cap, new DummyExecution(), scenario, GlobalResult, cat, graph,
				startEdge);

			Map.Entry<CATree, Integer> entry = solution.entrySet().iterator().next();
			CATree solutionCAT = entry.getKey();
			int crossEnsembles = entry.getValue();

			// totalCrossEnsembles = totalCrossEnsembles +
			// crossEnsembles;

			// showCAT(cat);
			// END OF ONE ISSUE IN ONE TREATMENT
			// System.out.println("**** ISSUE SOLVED IN ONE
			// TREATMENT: " + issue.getIssueType());
			// showTree(graph);
			// ANALISI DEI GRAFI IN USCITA
			// int width = 0;
			GraphIterator<String, DefaultEdge> iterator1 = new BreadthFirstIterator<String, DefaultEdge>(
				graph);
			String lastNode = "";
			while (iterator1.hasNext()) {
			    // System.out.println(iterator1.next());
			    lastNode = iterator1.next();
			}

			int count = 0;

			// Depth
			List path = DijkstraShortestPath.findPathBetween(graph, "INIT", lastNode);
			path = DijkstraShortestPath.findPathBetween(graph, "INIT", lastNode);
			int depth = path.size();
			totalDepth = totalDepth + depth;

			// Width
			int initialWidth = 0;
			int finalWidth = 0;
			GraphIterator<String, DefaultEdge> iterator = new DepthFirstIterator<String, DefaultEdge>(
				graph);
			while (iterator.hasNext()) {

			    String nextNode = iterator.next();
			    Set<DefaultEdge> edges = graph.outgoingEdgesOf(nextNode);
			    int currentWidth = edges.size();

			    if (currentWidth >= initialWidth) {
				finalWidth = currentWidth;
				initialWidth = currentWidth;
			    }

			}
			totalWidth = totalWidth + finalWidth;

			// Number of ensembles used in the resolution - dv6
			// showTree(graph);
			ArrayList<String> Ensembles = new ArrayList<String>();
			GraphIterator<String, DefaultEdge> iterator2 = new DepthFirstIterator<String, DefaultEdge>(
				graph);
			while (iterator2.hasNext()) {

			    String nextNode = iterator2.next();

			    String[] tokens = nextNode.split("_");

			    if (scenario.equalsIgnoreCase("drones")) {
				if (tokens.length > 2) {

				    if (Ensembles.contains(tokens[2])) { // <-
									 // look
									 // for
									 // item!
					// ... item already in list
				    } else {
					Ensembles.add(tokens[2]);
				    }

				}
			    } else {
				// MOBILITY Scenario
				if (tokens.length > 3) {

				    if (Ensembles.contains(tokens[3])) { // <-
									 // look
									 // for
									 // item!
					// ... item already in list
				    } else {
					Ensembles.add(tokens[3]);
				    }

				}

			    }
			}
			numOfEnsembles = numOfEnsembles + Ensembles.size();

			// number of roles involved in the issue resolution
			ArrayList<String> rolesName = new ArrayList<String>();
			GraphIterator<String, DefaultEdge> iterator3 = new DepthFirstIterator<String, DefaultEdge>(
				graph);
			while (iterator3.hasNext()) {
			    String nextNode = iterator3.next();
			    String[] tokens = nextNode.split("_");
			    if (tokens.length > 1) {
				String first = tokens[0];
				String second = tokens[1];
				String roleName = first.concat(second);

				if (!rolesName.contains(roleName) && !tokens[0].equalsIgnoreCase("COM")
					&& !tokens[0].equalsIgnoreCase("OR") && !tokens[0].equalsIgnoreCase("AND")) {
				    rolesName.add(roleName);

				}
			    }

			}
			numOfRoles = numOfRoles + rolesName.size();

			break;
		    }
		}
	    }
	}

	// set dv1
	long endTime = System.nanoTime();
	long totalTime = endTime - startTime;

	// set dv2
	// long usedMemory = ExperimentRunner.runtime.totalMemory() -
	// ExperimentRunner.runtime.freeMemory() -
	// ExperimentRunner.memoryBaseline;
	long usedMemory = ExperimentRunner.memoryBean.getHeapMemoryUsage().getUsed() - ExperimentRunner.memoryBaseline;

	// set dv3
	double endCPU = ExperimentRunner.threadBean.getCurrentThreadCpuTime(); // in
	// nanoseconds
	double usedCPU = (endCPU - startCPU) / 1000000; // those are in
	// milliseconds

	System.gc();

	result.setDv1(totalTime);
	result.setDv2(usedMemory);
	result.setDv3(usedCPU);

	// DV4:
	int issues = treatment.getIssues().size();

	// set dv4: average depth of resolution trees
	int dv4 = totalDepth / issues;
	result.setDv4(dv4);
	System.out.println("Average Depth: " + dv4);

	// // set dv5: average width of resolution trees
	int dv5 = totalWidth / issues;
	result.setDv5(dv5);
	System.out.println("Average Width: " + dv5);

	// // set dv6: average number of ensembles invoked during the issue
	// resolution
	// System.out.println(numOfEnsembles);
	int dv6 = numOfEnsembles / issues;
	result.setDv6(dv6);
	System.out.println("Average number of ensembles used to solve the issue: " + dv6);

	// // set dv7: average number of roles involved in an issue resolution
	// resolution
	int dv7 = numOfRoles / issues;
	result.setDv7(dv7);
	System.out.println("Average number of roles involved in an issue resolution: " + dv7);

	System.gc();

	return result;
    }

    // METHODS FOR TREE ANALYSIS
    /* max depth */
    private int maxDepth(CATree cat) {
	// build graph
	distance = 0;
	mxGraph graph = new mxGraph();

	mxAnalysisGraph aGraph = new mxAnalysisGraph();
	aGraph.setGraph(cat);

	// apply dfs to find depth of a tree
	mxTraversal.dfs(aGraph, cat.getFirstNode(), new mxICellVisitor() {

	    @Override
	    public boolean visit(Object vertex, Object edge) {
		mxCell v = (mxCell) vertex;
		mxCell e = (mxCell) edge;
		String eVal = "N/A";

		if (e != null) {
		    if (e.getValue() == null) {
			eVal = "1.0";
		    } else {
			eVal = e.getValue().toString();
		    }
		}

		if (!eVal.equals("N/A")) {
		    distance = distance + 1;
		}

		// System.out.print("(v: " + v.getValue() + " e: " + eVal +
		// ")");

		return false;
	    }
	});
	// System.out.println("MaxDepth= " + distance);
	return distance;

    }

    private int MinimumDepth(CATree cat) {
	depth = 0;
	// init analysis
	mxAnalysisGraph aGraph = new mxAnalysisGraph();
	aGraph.setGraph(cat);

	// apply bfs
	mxTraversal.bfs(aGraph, cat.getFirstNode(), new mxICellVisitor() {

	    @Override
	    public boolean visit(Object vertex, Object edge) {
		mxCell v = (mxCell) vertex;
		mxCell e = (mxCell) edge;

		if (e != null) {
		    depth++;
		    // System.out.println("Visit " + v.getValue());
		    if (hasChild(v) == 0) {
			if (!printed) {
			    printed = true;
			    // System.out.println("Minimum depth = " + depth);
			}
		    }

		}

		return false;
	    }
	});
	printed = false;

	return depth;
    }

    private int MinExtent(CATree cat) {

	// init analysis
	mxAnalysisGraph aGraph = new mxAnalysisGraph();
	aGraph.setGraph(cat);

	// apply bfs
	mxTraversal.bfs(aGraph, cat.getFirstNode(), new mxICellVisitor() {

	    @Override
	    public boolean visit(Object vertex, Object edge) {
		mxCell v = (mxCell) vertex;
		mxCell e = (mxCell) edge;

		if (e != null) {
		    // System.out.println("Visit " + v.getValue());
		    int c = hasChild(v);
		    if (c != 0) {
			if (c < miniumExtent) {
			    miniumExtent = c;
			}
		    }

		}

		return false;
	    }
	});
	// System.out.println("minimum Extent : " + miniumExtent);
	return miniumExtent;

    }

    private int MaxExtent(CATree cat) {

	// init analysis
	mxAnalysisGraph aGraph = new mxAnalysisGraph();
	aGraph.setGraph(cat);

	// apply bfs
	mxTraversal.bfs(aGraph, cat.getFirstNode(), new mxICellVisitor() {

	    @Override
	    public boolean visit(Object vertex, Object edge) {
		mxCell v = (mxCell) vertex;
		mxCell e = (mxCell) edge;

		if (e != null) {
		    // System.out.println("Visit " + v.getValue());
		    int c = hasChild(v);

		    if (c != 0) {

			if (c > maximumExtent) {
			    maximumExtent = c;
			}
		    }

		}

		return false;
	    }
	});
	// System.out.println("maximum Extent : " + maximumExtent);
	return maximumExtent;

    }

    private static int hasChild(mxICell cell) {

	if (cell.getChildCount() == 0) {
	    return 0;
	}
	int childs = 0;
	for (int i = 0; i < cell.getChildCount(); i++) {
	    mxICell child = cell.getChildAt(i);
	    if (child != null && child.isVertex()) {
		childs++;
	    }
	}
	return childs;
    }

    private void showTree(DirectedGraph<String, DefaultEdge> graph) {

	// VISUALIZE GRAPH
	// create a visualization using JGraph, via the adapter
	JGraphModelAdapter jgAdapter = new JGraphModelAdapter<>(graph);

	JGraph jgraph = new JGraph(jgAdapter);
	/* Apply tree Layout */

	// Object roots = getRoots(); // replace getRoots with your own

	JGraphFacade facade = new JGraphFacade(jgraph);
	// Pass the facade the JGraph instance
	JGraphLayout layout = new JGraphTreeLayout();
	// Create an instance of the appropriate layout
	layout.run(facade); // Run the layout on the facade.
	Map nested = facade.createNestedMap(true, true);
	// Obtain a map of the resulting attribute changes from the facade
	jgraph.getGraphLayoutCache().edit(nested);
	// Apply the results to the actual graph

	JFrame frame = new JFrame();
	frame.getContentPane().add(jgraph);

	frame.setBounds(600, 125, 650, 600);

	frame.setTitle("COLLECTIVE ADAPTATION TREE");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.pack();

	frame.setVisible(true);
    }

    private void showCAT(CATree cat) {

	// frame to see the issue resolution tree mxGraphComponent
	mxGraphComponent graphComponent = new mxGraphComponent(cat);

	CATree hierarchy = new CATree();
	mxGraphComponent graphComponentHierarchy = new mxGraphComponent(hierarchy);

	// layout = new mxParallelEdgeLayout(graphComponent.getGraph());

	// layout = new mxHierarchicalLayout(graphComponent.getGraph());
	// layout1 = new mxCompactTreeLayout(graphComponent.getGraph());

	graphComponent.setEnabled(true);

	graphComponent.setBounds(600, 125, 650, 600);
	graphComponent.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0)));

	mxHierarchicalLayout layout1 = new mxHierarchicalLayout(graphComponent.getGraph());

	layout1.execute(graphComponent.getGraph().getDefaultParent());

	JFrame frame = new JFrame();
	frame.getContentPane().add(graphComponent);
	frame.setTitle("ADAPTATION RESOLUTION TREE");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.pack();

	frame.setVisible(true);

    }

}
