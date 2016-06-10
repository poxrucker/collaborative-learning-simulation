package allow.adaptation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import javax.naming.ConfigurationException;
import javax.swing.UIManager;

import allow.adaptation.api.CollectiveAdaptationEnsemble;
import allow.adaptation.api.CollectiveAdaptationProblem;
import allow.adaptation.api.CollectiveAdaptationRole;
import allow.adaptation.ensemble.Ensemble;
import allow.adaptation.presentation.CAWindow;

public class CollectiveDrones {
    private final static String REPO_PATH = "adaptation.properties";

    private static int idEnsembles = 0;
    private static int idRoles = 0;

    public static void main(String[] args) throws ConfigurationException, FileNotFoundException {

	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} catch (Exception e) {

	}

	System.gc();
	String propPath = REPO_PATH;
	if (args.length > 0) {
	    propPath = args[0];
	}

	List<CollectiveAdaptationRole> E1 = new ArrayList<CollectiveAdaptationRole>();

	CollectiveAdaptationRole p1 = new CollectiveAdaptationRole();
	p1.setRole("D1");

	CollectiveAdaptationRole p2 = new CollectiveAdaptationRole();
	p2.setRole("D2");

	CollectiveAdaptationRole p3 = new CollectiveAdaptationRole();
	p3.setRole("H1");

	CollectiveAdaptationRole p4 = new CollectiveAdaptationRole();
	p4.setRole("M1");

	CollectiveAdaptationRole p5 = new CollectiveAdaptationRole();
	p5.setRole("C1");

	E1.add(p1);
	E1.add(p2);
	E1.add(p3);
	E1.add(p4);
	E1.add(p5);

	List<CollectiveAdaptationRole> E2 = new ArrayList<CollectiveAdaptationRole>();

	CollectiveAdaptationRole p7 = new CollectiveAdaptationRole();
	p7.setRole("D3");

	CollectiveAdaptationRole p8 = new CollectiveAdaptationRole();
	p8.setRole("D4");

	CollectiveAdaptationRole p9 = new CollectiveAdaptationRole();
	p9.setRole("H2");

	CollectiveAdaptationRole p10 = new CollectiveAdaptationRole();
	p10.setRole("M2");

	CollectiveAdaptationRole p11 = new CollectiveAdaptationRole();
	p11.setRole("C2");

	E2.add(p7);
	E2.add(p8);
	E2.add(p9);
	E2.add(p10);
	E2.add(p11);

	List<CollectiveAdaptationRole> E3 = new ArrayList<CollectiveAdaptationRole>();

	CollectiveAdaptationRole p13 = new CollectiveAdaptationRole();
	p13.setRole("D5");

	CollectiveAdaptationRole p14 = new CollectiveAdaptationRole();
	p14.setRole("D6");

	CollectiveAdaptationRole p15 = new CollectiveAdaptationRole();
	p15.setRole("H3");

	CollectiveAdaptationRole p16 = new CollectiveAdaptationRole();
	p16.setRole("M3");

	CollectiveAdaptationRole p17 = new CollectiveAdaptationRole();
	p17.setRole("C3");

	E3.add(p13);
	E3.add(p14);
	E3.add(p15);
	E3.add(p16);
	E3.add(p17);

	List<CollectiveAdaptationRole> E4 = new ArrayList<CollectiveAdaptationRole>();

	CollectiveAdaptationRole p19 = new CollectiveAdaptationRole();
	p19.setRole("H1");

	CollectiveAdaptationRole p20 = new CollectiveAdaptationRole();
	p20.setRole("H2");

	CollectiveAdaptationRole p21 = new CollectiveAdaptationRole();
	p21.setRole("H3");

	E4.add(p19);
	E4.add(p20);
	E4.add(p21);

	List<CollectiveAdaptationRole> E5 = new ArrayList<CollectiveAdaptationRole>();

	CollectiveAdaptationRole p22 = new CollectiveAdaptationRole();
	p22.setRole("G1");

	CollectiveAdaptationRole p23 = new CollectiveAdaptationRole();
	p23.setRole("G2");

	CollectiveAdaptationRole p24 = new CollectiveAdaptationRole();
	p24.setRole("G3");

	E5.add(p22);
	E5.add(p23);
	E5.add(p24);

	List<CollectiveAdaptationEnsemble> ensemblesCAP = new ArrayList<CollectiveAdaptationEnsemble>();
	ensemblesCAP.add(new CollectiveAdaptationEnsemble("E1", E1));
	ensemblesCAP.add(new CollectiveAdaptationEnsemble("E2", E2));
	ensemblesCAP.add(new CollectiveAdaptationEnsemble("E3", E3));
	ensemblesCAP.add(new CollectiveAdaptationEnsemble("E4", E4));
	ensemblesCAP.add(new CollectiveAdaptationEnsemble("E5", E5));

	CollectiveAdaptationProblem cap = new CollectiveAdaptationProblem("CAP_1", ensemblesCAP, null, null, null,
		null);

	DemoManagementSystem dms = DemoManagementSystem.initializeSystem("scenario/Drones/");
	List<Treatment> treatments = createTreatmentsDrones();

	// Ensemble Creation - Instance of Ensemble 1
	Ensemble e1 = dms.getEnsemble("E1", cap);
	EnsembleManager e1Manager = new EnsembleManager(e1);

	// Ensemble Creation - Instance of Ensemble 2
	Ensemble e2 = dms.getEnsemble("E2", cap);
	EnsembleManager e2Manager = new EnsembleManager(e2);

	// Ensemble Creation - Instance of Ensemble 3
	Ensemble e3 = dms.getEnsemble("E3", cap);
	EnsembleManager e3Manager = new EnsembleManager(e3);

	// Ensemble Creation - Instance of Ensemble 4
	Ensemble e4 = dms.getEnsemble("E4", cap);
	EnsembleManager e4Manager = new EnsembleManager(e4);

	// Ensemble Creation - Instance of Ensemble 5
	Ensemble e5 = dms.getEnsemble("E5", cap);
	EnsembleManager e5Manager = new EnsembleManager(e5);

	List<EnsembleManager> ensembles = new ArrayList<EnsembleManager>();
	ensembles.add(e1Manager);
	ensembles.add(e2Manager);
	ensembles.add(e3Manager);
	ensembles.add(e4Manager);
	ensembles.add(e5Manager);

	Utilities.buildSolversMapDrones(ensembles);

	System.gc();
	try {
	    System.out.println("Experiment starting in 5 seconds...");
	    Thread.sleep(5000);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	runTreatments(cap, treatments, ensembles, "drones");

	treatments.remove(0);
	Utilities.genericWriteFile(treatments, "treatmentsDrones.csv");
	System.out.println("END SIMULATION");
	System.exit(1);

    }

    static List<Treatment> createTreatmentsDrones() {

	List<Treatment> result = new ArrayList<Treatment>();
	int treatmentsForSubject = 100;

	int[] v1Values = { 1, 250, 500, 750, 1000 };

	int[] othersValues = { 0, 20, 40, 60, 80, 100 };
	boolean fullyRandom = true;

	int currentTreatmentId = 1;

	if (fullyRandom) {
	    for (int i = 0; i < v1Values.length; i++) {
		for (int t = 1; t <= treatmentsForSubject; t++) {
		    result.add(createRandomTreatmentDrones(currentTreatmentId++, v1Values[i]));
		}
		System.gc();
	    }
	} else {
	    // othersValues.length
	    for (int i = 0; i < v1Values.length; i++) {
		for (int issueIndex = 0; issueIndex <= 4; issueIndex++) {
		    for (int j = 0; j < othersValues.length; j++) {
			for (int t = 1; t <= treatmentsForSubject; t++) {
			    result.add(createTreatmentDrones(currentTreatmentId++, v1Values[i], issueIndex,
				    othersValues[j]));
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

    static Treatment createTreatmentDrones(int id, int v1Value, int issueIndex, int othersValue) {
	Treatment result = new Treatment(id, v1Value, issueIndex, othersValue, "Drones");
	result.populateDrones();
	return result;
    }

    static Treatment createRandomTreatmentDrones(int id, int v1Value) {
	Treatment result = new Treatment(id, v1Value, "Drones");
	result.populateDrones();
	return result;
    }

    static void runTreatments(CollectiveAdaptationProblem cap, List<Treatment> treatments,
	    List<EnsembleManager> ensembles, String scenario) {
	ListIterator<Treatment> iterator = treatments.listIterator();
	List<ExperimentResult> results = new ArrayList<ExperimentResult>();
	CAWindow window = null;
	// CAWindow window = new CAWindow();
	int id = 1;
	while (iterator.hasNext()) {
	    results.add(runTreatment(cap, iterator.next(), ensembles, window, id, scenario));
	    id = id + 1;
	}
	// remove the first result

	results.remove(0);
	Utilities.genericWriteFile(results, "resultsDrones.csv");
    }

    static ExperimentResult runTreatment(CollectiveAdaptationProblem cap, Treatment treatment,
	    List<EnsembleManager> ensembles, CAWindow window, int id, String scenario) {
	System.out.println("" + id + " - " + treatment.toString());
	HashMap<RoleManager, HashMap<String, ArrayList<Integer>>> GlobalResult = new HashMap<RoleManager, HashMap<String, ArrayList<Integer>>>();
	return ExperimentRunner.getInstance().run(cap, treatment, ensembles, window, scenario, GlobalResult);

    }

    public Ensemble getEnsemble(String type, CollectiveAdaptationProblem cap) {
	Ensemble ei = null;
	ClassLoader classloader = Thread.currentThread().getContextClassLoader();

	if (ensembleInstances == null) {
	    ensembleInstances = new ArrayList<Ensemble>();
	}

	File dir = new File(REPO_PATH);
	if (!dir.isDirectory()) {
	    throw new NullPointerException("Impossibile to load the ensemble type, mainDir not found " + dir);
	}
	File f = new File(REPO_PATH + File.separator + type + ".xml");

	// retrieve the type from file
	EnsembleParser parser = new EnsembleParser();
	ei = parser.parseEnsemble(f);

	ensembleInstances.add(ei);

	return ei;
    }

    private ArrayList<Ensemble> ensembleInstances;

    public ArrayList<Ensemble> getEnsembleInstances() {

	if (ensembleInstances == null) {
	    ensembleInstances = new ArrayList<Ensemble>();
	    return ensembleInstances;
	} else {
	    return ensembleInstances;
	}
    }

    public void setEnsembleInstances(ArrayList<Ensemble> ensembleInstances) {
	this.ensembleInstances = ensembleInstances;
    }

    public Ensemble getEnsembleInstance(String type, CollectiveAdaptationProblem cap) {
	Ensemble en = null;

	if (ensembleInstances == null) {
	    ensembleInstances = new ArrayList<Ensemble>();

	    en = this.getEnsemble(type, cap);

	    idEnsembles++;
	    ensembleInstances.add(en);

	} else {

	    en = this.getEnsemble(type, cap);

	    idEnsembles++;
	    ensembleInstances.add(en);
	}

	return en;
    }

}
