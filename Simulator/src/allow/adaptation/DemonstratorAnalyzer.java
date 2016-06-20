package allow.adaptation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import allow.adaptation.api.CollectiveAdaptationCommandExecution;
import allow.adaptation.api.CollectiveAdaptationInterface;
import allow.adaptation.api.CollectiveAdaptationProblem;
import allow.adaptation.api.CollectiveAdaptationSolution;
import allow.adaptation.api.RoleCommand;
import allow.adaptation.ensemble.Issue;
import allow.adaptation.model.IssueCommunication;
import allow.adaptation.model.IssueResolution;
import allow.adaptation.presentation.CATree;
import allow.adaptation.presentation.CAWindow;

public class DemonstratorAnalyzer implements CollectiveAdaptationInterface {

	private final static String PROP_PATH = "adaptation.properties";
	private static final String STYLE_INIT = "verticalAlign=middle;dashed=false;dashPattern=5;rounded=true;fillColor=white;size=2";
	private static final String STYLE_ROLE = "verticalAlign=middle;dashed=false;dashPattern=5;rounded=true;align=center;fontSize=9;";

	private static final String STYLE_ISSUE_EDGE = "fontColor=#FF0000;fontSize=8;endArrow=classic;html=1;fontFamily=Helvetica;align=left;";

	// private final static String PreferencesDir =
	// "scenario/ALLOWEnsembles/Preferences/";

	private CollectiveAdaptationCommandExecution executor;

	@Override
	public void executeCapNew(CollectiveAdaptationProblem cap,
			CollectiveAdaptationCommandExecution executor) {
		this.executor = executor;

		Properties props = new Properties();
		try {
			props.load(getClass().getClassLoader().getResourceAsStream(
					PROP_PATH));
		} catch (FileNotFoundException e) {
			System.out.println("Error loading file " + e.getMessage());

			throw new NullPointerException(e.getMessage());

		} catch (IOException e) {
			System.out.println("Error loading file " + e.getMessage());

			throw new NullPointerException(e.getMessage());

		}

	}

	@Override
	public HashMap<CATree, Integer> executeCap(
			CollectiveAdaptationProblem cap,
			CollectiveAdaptationCommandExecution executor,
			String scenario,
			HashMap<RoleManager, HashMap<String, ArrayList<Integer>>> GlobalResult,
			CATree cat, DirectedGraph<String, DefaultEdge> graph,
			DefaultEdge startEdge, List<EnsembleManager> ensembles) {

		// call the analyzer with the specific CAP

		// reading property file
		// String propPath = PROP_PATH;

		HashMap<CATree, Integer> result = new HashMap<CATree, Integer>();

		this.executor = executor;

		Properties props = new Properties();
		try {
			props.load(getClass().getClassLoader().getResourceAsStream(
					PROP_PATH));
		} catch (FileNotFoundException e) {
			System.out.println("Error loading file " + e.getMessage());

			throw new NullPointerException(e.getMessage());

		} catch (IOException e) {
			System.out.println("Error loading file " + e.getMessage());

			throw new NullPointerException(e.getMessage());

		}

		// demo management system construction

		DemoManagementSystem dms = null;
		try {
			if (scenario.equalsIgnoreCase("Mobility")) {
				dms = DemoManagementSystem
						.initializeSystem("scenarioECAS/Mobility/");
			} else {
				dms = DemoManagementSystem.initializeSystem("scenario/Drones/");
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// creation of ensembles
		// List<EnsembleManager> ensembles = new ArrayList<EnsembleManager>();
		/*
		 * for (int i = 0; i < cap.getEnsembles().size(); i++) {
		 * CollectiveAdaptationEnsemble ensemble = cap.getEnsembles().get(i);
		 * String EnsembleName = ensemble.getEnsembleName(); Ensemble e =
		 * dms.getEnsemble(EnsembleName, cap); EnsembleManager manager = new
		 * EnsembleManager(e);
		 * 
		 * // set the Evoknowledge of the Ensemble // manager.setEk(ek);
		 * 
		 * // add the ensemble to the list ensembles.add(manager);
		 * 
		 * }
		 */

		int crossEnsembles = 0;
		this.run(cap, ensembles, null, cap.getIssue(), cap.getCapID(),
				cap.getStartingRole(), 0, GlobalResult, cat, graph, startEdge,
				crossEnsembles);
		result.put(cat, crossEnsembles);
		return result;

	}

	public CollectiveAdaptationSolution run(
			CollectiveAdaptationProblem cap,
			List<EnsembleManager> ensembles,
			CAWindow window,
			String issueName,
			String capID,
			String startingRole,
			int issueIndex,
			HashMap<RoleManager, HashMap<String, ArrayList<Integer>>> GlobalResult,
			CATree cat, DirectedGraph<String, DefaultEdge> graph,
			DefaultEdge lastEdge, int crossEnsembles) {

		CollectiveAdaptationSolution solution = new CollectiveAdaptationSolution(
				capID, null);
		Issue issue = new Issue();
		issue.setIssueType(issueName);

		// System.out.println("Ruolo da trovare: " + startingRole);

		EnsembleManager en = null;
		for (int i = 0; i < ensembles.size(); i++) {
			EnsembleManager e = ensembles.get(i);
			for (int j = 0; j < e.getRolesManagers().size(); j++) {
				RoleManager r = e.getRolesManagers().get(j);
				if (r.getRole().getType().equalsIgnoreCase(startingRole)) {
					en = e;
					break;
				}

			}

		}

		// search the role that can trigger the specific issue
		// EnsembleManager en = ensembles.stream()
		// .filter(e ->
		// e.getEnsemble().getName().equals(cap.getStartingRoleEnsemble())).findFirst().get();

		// EnsembleManager en = ensembles.stream().filter(e ->
		// e.getEnsemble().getName().equals(startingRole)).findFirst()
		// .get();

		RoleManager r = en.getRolebyType(startingRole);
		// System.out.println("ISSUE TRIGGERED: " + issue.getIssueType());

		IssueResolution resolution1 = new IssueResolution(1, "ISSUE_TRIGGERED",
				r, r, issue, null);
		resolution1.setRoot(true);
		// r.addIssueResolution(resolution1);

		EnsembleManager em = null;

		// add the issueresolution to the right Ensemble
		for (int i = 0; i < ensembles.size(); i++) {
			for (int j = 0; j < ensembles.get(i).getRolesManagers().size(); j++) {
				RoleManager currentManager = ensembles.get(i)
						.getRolesManagers().get(j);
				if (currentManager.getRole().getType()
						.equalsIgnoreCase(r.getRole().getType())) {
					ArrayList<IssueResolution> resolutions = new ArrayList<IssueResolution>();

					em = ensembles.get(i);
					if (em.getIssueCommunications() != null) {
						em.getIssueCommunications().clear();
					}

					solution.setCapID(capID);
					HashMap<String, List<RoleCommand>> ensembleCommands = new HashMap<String, List<RoleCommand>>();
					List<RoleCommand> commands = new ArrayList<RoleCommand>();
					ensembleCommands.put(em.getEnsemble().getName(), commands);

					solution.setEnsembleCommands(ensembleCommands);

					// update id of the issue resolution
					em.setIssueResolutionCount(1);
					resolution1.setIssueResolutionID(em
							.getIssueResolutionCount());

					resolutions.add(resolution1);
					em.setActiveIssueResolutions(resolutions);

					List<IssueCommunication> relatedComs = new ArrayList<IssueCommunication>();

					em.setCommunicationsRelations(resolution1, relatedComs);

					CATree hierarchyTree = createHierarchyTree(ensembles);

					// window.updateHierarchy(hierarchyTree);

					em.checkIssues(cap, capID, window, ensembles, solution,
							issueIndex, hierarchyTree, GlobalResult, cat,
							graph, lastEdge);

					break;
				}
			}
		}
		// retrieve the final solution for the ensemble
		List<RoleCommand> roleCommands = new ArrayList<RoleCommand>();
		solution.setCapID(capID);
		for (int i = 0; i < em.getRolesManagers().size(); i++) {
			RoleManager rm = em.getRolesManagers().get(i);
			// System.out.println("ROLE: " + rm.getRole().getType());
			RoleCommand command = rm.getRoleCommands();

			roleCommands.add(command);

		}

		HashMap<String, List<RoleCommand>> ensembleCommands = new HashMap<String, List<RoleCommand>>();
		ensembleCommands.put(em.getEnsemble().getName(), roleCommands);
		solution.setEnsembleCommands(ensembleCommands);
		return solution;

	}

	private CATree createHierarchyTree(List<EnsembleManager> ensembles) {
		CATree hierarchyTree = new CATree();

		// CREATE FIRST PART OF THE HIERARCHY TREE

		Object root1 = hierarchyTree.insertNodeHierarchy(
				hierarchyTree.getDefaultParent(), null, "UMS", STYLE_INIT);

		Object v1 = hierarchyTree.insertNodeHierarchy(
				hierarchyTree.getDefaultParent(), null, "FBC", STYLE_INIT);
		hierarchyTree.insertEdge(hierarchyTree.getDefaultParent(), "", "",
				root1, v1, STYLE_ISSUE_EDGE);

		for (int k = 0; k < ensembles.size(); k++) {
			EnsembleManager e = ensembles.get(k);
			if (!(e.getEnsemble().getName().contains("Flexi"))) {
				List<RoleManager> roles = e.getRolesManagers();
				for (int m = 0; m < roles.size(); m++) {
					RoleManager role = roles.get(m);
					if (role.getRole().getType().contains("RouteManagement")) {
						Object v = hierarchyTree.insertNodeHierarchy(
								hierarchyTree.getDefaultParent(), null, role
										.getRole().getType(), STYLE_INIT);
						hierarchyTree.insertEdge(
								hierarchyTree.getDefaultParent(), "", "", v1,
								v, STYLE_ISSUE_EDGE);
						for (int n = 0; n < roles.size(); n++) {
							RoleManager role1 = roles.get(n);
							if (role1.getRole().getType()
									.contains("RoutePassenger")) {
								Object v2 = hierarchyTree.insertNodeHierarchy(
										hierarchyTree.getDefaultParent(), null,
										role1.getRole().getType(), STYLE_INIT);
								hierarchyTree.insertEdge(
										hierarchyTree.getDefaultParent(), "",
										"", v, v2, STYLE_ISSUE_EDGE);
							} else if (role1.getRole().getType()
									.contains("FlexibusDriver")) {
								Object v2 = hierarchyTree.insertNodeHierarchy(
										hierarchyTree.getDefaultParent(), null,
										role1.getRole().getType(), STYLE_INIT);
								hierarchyTree.insertEdge(
										hierarchyTree.getDefaultParent(), "",
										"", v, v2, STYLE_ISSUE_EDGE);
							}

						}
					}

				}

			}

		}
		return hierarchyTree;
	}

	private static int randomThree(List<Integer> numbers) {
		Random rand = new Random();
		return (rand.nextInt(numbers.size()));
	}

}
