package allow.simulator.adaptation;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import allow.adaptation.DemoManagementSystem;
import allow.adaptation.EnsembleManager;
import allow.adaptation.ExperimentResult;
import allow.adaptation.ExperimentRunner;
import allow.adaptation.RoleManager;
import allow.adaptation.Treatment;
import allow.adaptation.Utilities;
import allow.adaptation.api.CollectiveAdaptationEnsemble;
import allow.adaptation.api.CollectiveAdaptationProblem;
import allow.adaptation.api.CollectiveAdaptationRole;
import allow.adaptation.api.RoleCommand;
import allow.adaptation.presentation.CAWindow;
import allow.simulator.entity.Gender;
import allow.simulator.entity.Profile;
import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.util.Coordinate;

public class CollectiveAdaptation implements IAdaptationStrategy {
	
	@Override
	public void solveAdaptation(Issue issue, Ensemble ensemble) {

		String issueType = issue.toString();

		String entityType = ensemble.getCreator().toString();

		// BusRoute

		List<CollectiveAdaptationRole> rolesRouteA = new ArrayList<CollectiveAdaptationRole>();

		CollectiveAdaptationRole p1 = new CollectiveAdaptationRole();
		p1.setRole("RoutePassenger_33");

		CollectiveAdaptationRole p2 = new CollectiveAdaptationRole();
		p2.setRole("RoutePassenger_30");

		CollectiveAdaptationRole p3 = new CollectiveAdaptationRole();
		p3.setRole("RoutePassenger_36");

		CollectiveAdaptationRole p4 = new CollectiveAdaptationRole();
		p3.setRole("RouteManagement_1");

		CollectiveAdaptationRole p5 = new CollectiveAdaptationRole();
		p3.setRole("FlexibusDriver_13");

		rolesRouteA.add(p1);
		rolesRouteA.add(p2);
		rolesRouteA.add(p3);
		rolesRouteA.add(p4);
		rolesRouteA.add(p5);

		// ROUTE B
		List<CollectiveAdaptationRole> rolesRouteB = new ArrayList<CollectiveAdaptationRole>();
		CollectiveAdaptationRole p6 = new CollectiveAdaptationRole();
		p6.setRole("RoutePassenger_64");

		CollectiveAdaptationRole p7 = new CollectiveAdaptationRole();
		p7.setRole("RoutePassenger_69");

		CollectiveAdaptationRole p9 = new CollectiveAdaptationRole();
		p9.setRole("RoutePassenger_74");

		CollectiveAdaptationRole p10 = new CollectiveAdaptationRole();
		p10.setRole("RouteManagement_2");

		CollectiveAdaptationRole p11 = new CollectiveAdaptationRole();
		p11.setRole("FlexibusDriver_28");

		rolesRouteB.add(p6);
		rolesRouteB.add(p7);
		rolesRouteB.add(p9);
		rolesRouteB.add(p10);
		rolesRouteB.add(p11);

		// FlexiBusMngmt
		List<CollectiveAdaptationRole> rolesFBMngmt = new ArrayList<CollectiveAdaptationRole>();
		CollectiveAdaptationRole p12 = new CollectiveAdaptationRole();
		p12.setRole("RouteManagement_1");

		CollectiveAdaptationRole p13 = new CollectiveAdaptationRole();
		p13.setRole("RouteManagement_2");

		CollectiveAdaptationRole p14 = new CollectiveAdaptationRole();
		p14.setRole("FBCManager");

		rolesRouteB.add(p12);
		rolesRouteB.add(p13);
		rolesRouteB.add(p14);

		// CP Ride A
		List<CollectiveAdaptationRole> rolesCPRideA = new ArrayList<CollectiveAdaptationRole>();
		CollectiveAdaptationRole p15 = new CollectiveAdaptationRole();
		p15.setRole("CPDriver_A");

		CollectiveAdaptationRole p16 = new CollectiveAdaptationRole();
		p16.setRole("CPPassenger_1");

		rolesCPRideA.add(p15);
		rolesCPRideA.add(p16);

		// CP Ride B
		List<CollectiveAdaptationRole> rolesCPRideB = new ArrayList<CollectiveAdaptationRole>();
		CollectiveAdaptationRole p17 = new CollectiveAdaptationRole();
		p17.setRole("CPDriver_B");

		CollectiveAdaptationRole p18 = new CollectiveAdaptationRole();
		p18.setRole("CPPassenger_3");

		rolesCPRideB.add(p17);
		rolesCPRideB.add(p18);

		// CP Company
		List<CollectiveAdaptationRole> rolesCPCompany = new ArrayList<CollectiveAdaptationRole>();
		CollectiveAdaptationRole p19 = new CollectiveAdaptationRole();
		p19.setRole("CPDriver_A");

		CollectiveAdaptationRole p20 = new CollectiveAdaptationRole();
		p20.setRole("CPDriver_B");

		CollectiveAdaptationRole p21 = new CollectiveAdaptationRole();
		p21.setRole("CPManager");

		rolesCPCompany.add(p19);
		rolesCPCompany.add(p20);
		rolesCPCompany.add(p21);

		// UMS
		List<CollectiveAdaptationRole> rolesUMS = new ArrayList<CollectiveAdaptationRole>();
		CollectiveAdaptationRole p22 = new CollectiveAdaptationRole();
		p22.setRole("CPManager");

		CollectiveAdaptationRole p23 = new CollectiveAdaptationRole();
		p23.setRole("FBC");

		rolesUMS.add(p22);
		rolesUMS.add(p23);

		List<CollectiveAdaptationEnsemble> ensemblesCAP = new ArrayList<CollectiveAdaptationEnsemble>();
		ensemblesCAP.add(new CollectiveAdaptationEnsemble("BusRoute",
				rolesRouteA));
		/*
		 * ensemblesCAP .add(new CollectiveAdaptationEnsemble("RouteB",
		 * rolesRouteB)); ensemblesCAP.add(new
		 * CollectiveAdaptationEnsemble("FlexiBusMngmt", rolesFBMngmt));
		 * ensemblesCAP.add(new CollectiveAdaptationEnsemble("CPRideA",
		 * rolesCPRideA)); ensemblesCAP.add(new
		 * CollectiveAdaptationEnsemble("CPRideB", rolesCPRideB));
		 * ensemblesCAP.add(new CollectiveAdaptationEnsemble("CPCompany",
		 * rolesCPCompany)); ensemblesCAP.add(new
		 * CollectiveAdaptationEnsemble("UMS", rolesUMS));
		 */
		CollectiveAdaptationProblem cap = new CollectiveAdaptationProblem(
				"CAP_1", ensemblesCAP, null, null, ensemblesCAP.get(0)
						.getEnsembleName(), null);

		DemoManagementSystem dms = null;
		try {
			dms = DemoManagementSystem
					.initializeSystem("scenarioECAS/Mobility/");
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
		// List<Treatment> treatments = createTreatmentMobility();

		// Ensemble Creation - Instance of Ensemble 1
		allow.adaptation.ensemble.Ensemble e1 = dms
				.getEnsemble("BusRoute", cap);
		EnsembleManager e1Manager = new EnsembleManager(e1);

		// Ensemble Creation - Instance of Ensemble 2
		/*
		 * allow.adaptation.ensemble.Ensemble e2 = dms.getEnsemble("RouteB",
		 * cap); EnsembleManager e2Manager = new EnsembleManager(e2);
		 * 
		 * // Ensemble Creation - Instance of Ensemble 3
		 * allow.adaptation.ensemble.Ensemble e3 = dms.getEnsemble(
		 * "FlexiBusMngmt", cap); EnsembleManager e3Manager = new
		 * EnsembleManager(e3);
		 * 
		 * // Ensemble Creation - Instance of Ensemble 4
		 * allow.adaptation.ensemble.Ensemble e4 = dms.getEnsemble("CPRideA",
		 * cap); EnsembleManager e4Manager = new EnsembleManager(e4);
		 * 
		 * // Ensemble Creation - Instance of Ensemble 5
		 * allow.adaptation.ensemble.Ensemble e5 = dms.getEnsemble("CPRideB",
		 * cap); EnsembleManager e5Manager = new EnsembleManager(e5);
		 * 
		 * // Ensemble Creation - Instance of Ensemble 6
		 * allow.adaptation.ensemble.Ensemble e6 = dms.getEnsemble("CPCompany",
		 * cap); EnsembleManager e6Manager = new EnsembleManager(e6);
		 * 
		 * // Ensemble Creation - Instance of Ensemble 7
		 * allow.adaptation.ensemble.Ensemble e7 = dms.getEnsemble("UMS", cap);
		 * EnsembleManager e7Manager = new EnsembleManager(e7);
		 */
		List<EnsembleManager> ensembles = new ArrayList<EnsembleManager>();
		ensembles.add(e1Manager);
		/*
		 * ensembles.add(e2Manager); ensembles.add(e3Manager);
		 * ensembles.add(e4Manager); ensembles.add(e5Manager);
		 * ensembles.add(e6Manager); ensembles.add(e7Manager);
		 */
		Utilities.buildSolversMapMobility(ensembles);

		System.gc();
		try {
			System.out.println("Experiment starting in 5 seconds...");
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		CAWindow window = null;
		int id = 1;

		runTreatment(cap, ensembles, window, id, "mobility");

		for (int j = 0; j < ensembles.size(); j++) {

			EnsembleManager em = ensembles.get(j);
			List<RoleManager> roleManagers = em.getRolesManagers();
			for (int i = 0; i < roleManagers.size(); i++) {
				RoleManager rm = roleManagers.get(i);
				System.out.println("ROLE: " + rm.getRole().getType());
				if (rm.getRoleCommands() != null) {
					RoleCommand command = rm.getRoleCommands();

					System.out.println("RoleCommand: "
							+ command.getCommands().get(0));
					// for each RoleCommand retrieve the plan for each entity
					// involved
					this.executeCommand(command);
				} else {
					System.out.println("RoleCommand: None");

				}
			}
		}

		// Utilities.genericWriteFile(treatments, "treatmentsMobility.csv");
		System.out.println("END SIMULATION");
		System.exit(1);

	}

	private void executeCommand(RoleCommand command) {
		System.out.println("Generate Plan for the role: " + command.getRole());

		String[] typeString = command.getRole().split("_");
		String roleType = typeString[0];
		if (roleType.equalsIgnoreCase("RoutePassenger")) {

			Coordinate c1 = new Coordinate(11.5, 34.4);

			allow.simulator.entity.Person entity = new allow.simulator.entity.Person(
					0, Gender.MALE, Profile.WORKER, null, null, c1, false,
					false, false, null, null);

			List<Itinerary> itineraries = new ArrayList<Itinerary>();
			entity.getContext().getJourneyPlanner().getTaxiPlannerService()
					.requestSingleJourney(null, itineraries);

			System.out.println("New Itinerary Calculated for role type: "
					+ roleType);

			// entity.getContext().getJourneyPlanner()
		}

	}

	static ExperimentResult runTreatment(CollectiveAdaptationProblem cap,
			List<EnsembleManager> ensembles, CAWindow window, int id,
			String scenario) {

		HashMap<RoleManager, HashMap<String, ArrayList<Integer>>> GlobalResult = new HashMap<RoleManager, HashMap<String, ArrayList<Integer>>>();

		Treatment treatment = createRandomTreatmentMobility(1, 1);
		System.out.println("" + id + " - " + treatment.toString());
		System.out.println("ISSUE TYPE:"
				+ treatment.getIssues().get(0).getIssueType());
		return ExperimentRunner.getInstance().run(cap, treatment, ensembles,
				window, scenario, GlobalResult);

	}

	static List<Treatment> createTreatmentMobility() {

		List<Treatment> result = new ArrayList<Treatment>();
		int treatmentsForSubject = 100;

		int[] v1Values = { 1, 250, 500, 750, 1000 };
		int[] othersValues = { 0, 20, 40, 60, 80, 100 };
		boolean fullyRandom = true;

		int currentTreatmentId = 1;

		if (fullyRandom) {
			for (int i = 0; i < v1Values.length; i++) {
				for (int t = 1; t <= treatmentsForSubject; t++) {
					result.add(createRandomTreatmentMobility(
							currentTreatmentId++, v1Values[i]));
				}
				System.gc();
			}
		} else {
			// othersValues.length
			for (int i = 0; i < v1Values.length; i++) {
				for (int issueIndex = 0; issueIndex <= othersValues.length; issueIndex++) {
					for (int j = 0; j < othersValues.length; j++) {
						for (int t = 1; t <= treatmentsForSubject; t++) {
							result.add(createTreatmentMobility(
									currentTreatmentId++, v1Values[i],
									issueIndex, othersValues[j]));
						}
					}
				}
			}
		}
		// Collections.shuffle(result, new Random(System.nanoTime()));
		// add a copy of the first element, it will be always at the beginning
		// and at the end of the list of treatments
		Treatment treatmentToAdd = ((Treatment) result.get(0)).clone();
		result.add(treatmentToAdd);
		return result;
	}

	static Treatment createTreatmentMobility(int id, int v1Value,
			int issueIndex, int othersValue) {
		Treatment result = new Treatment(id, v1Value, issueIndex, othersValue,
				"Mobility");
		result.populateMobility();
		return result;
	}

	static Treatment createRandomTreatmentMobility(int id, int v1Value) {
		Treatment result = new Treatment(id, v1Value, "Mobility");
		result.populateMobility();
		return result;
	}

}
