package allow.adaptation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import allow.adaptation.ensemble.Issue;

public class Treatment extends Loggable {

    private int id;

    private String scenario;

    private int iv1 = 0; // number of issues raised during the execution of the
    // mission
    private int[] ivIssuesDrones = { 0, 0, 0, 0, 0 }; // array containing the
    // number
    // of Ii issues
    private int[] ivIssuesMobility = { 0, 0, 0, 0, 0 }; // array containing the
    // number

    private int[] ivIssues = { 0, 0, 0, 0, 0 };

    private String[] ivIssueNamesDrones = { "Drone1Fault", "Drone3Fault", "IntruderDetected", "ObstacleFound",
	    "CameraFault" }; // array

    private String[] ivIssueNamesMobility = { "IntenseTraffic", "RouteBlocked", "PassengerDelay", "DriversStrike",
	    "CPAPassengerDelay" }; // array

    // containing
    // the
    // names
    // of Ii
    // issues

    private List<Issue> issues = new ArrayList<Issue>();

    public Treatment() {
	super();
    }

    public void populateDrones() {
	Issue currentIssue;
	for (int i = 0; i < this.ivIssuesDrones.length; i++) {
	    int currentIssueTotal = this.ivIssuesDrones[i];
	    for (int j = 0; j < currentIssueTotal; j++) {
		currentIssue = new Issue();
		currentIssue.setIssueType(this.ivIssueNamesDrones[i]);
		this.issues.add(currentIssue);
	    }
	}
	this.shuffleIssues();
    }

    public void populateMobility() {
	Issue currentIssue;
	for (int i = 0; i < this.ivIssuesMobility.length; i++) {
	    int currentIssueTotal = this.ivIssuesMobility[i];
	    for (int j = 0; j < currentIssueTotal; j++) {
		currentIssue = new Issue();

		currentIssue.setIssueType(this.ivIssueNamesMobility[i]);
		this.issues.add(currentIssue);
	    }
	}
	this.shuffleIssues();
    }

    private void shuffleIssues() {
	long seed = System.nanoTime();
	Collections.shuffle(this.issues, new Random(seed));
    }

    public Treatment(int id, int v1Value, String scenario) {
	this.id = id;
	this.iv1 = v1Value;
	this.scenario = scenario;
	Random random = new Random();
	if (v1Value == 1) {
	    if (this.scenario.equalsIgnoreCase("Mobility")) {
		this.ivIssuesMobility[random.nextInt(4)] = 1;
		// System.out.println(this.toString());
	    } else {
		// qui l'indice rappresenta il range del numero di issue che si
		// possono
		// generare
		this.ivIssuesDrones[random.nextInt(4)] = 1;
		// System.out.println(this.toString());
	    }

	    return;
	}

	List<Integer> randomValues = Utilities.generateRandomValues((int) ((100) * ((double) v1Value / 100)), 5);
	int currentIndexRandomValues = 0;
	if (scenario.equalsIgnoreCase("Mobility")) {
	    for (int i = 0; i < this.ivIssuesMobility.length; i++) {
		this.ivIssuesMobility[i] = randomValues.get(currentIndexRandomValues);
		currentIndexRandomValues++;
	    }
	} else {
	    for (int i = 0; i < this.ivIssuesDrones.length; i++) {
		this.ivIssuesDrones[i] = randomValues.get(currentIndexRandomValues);
		currentIndexRandomValues++;
	    }
	}

	//// System.out.println(this.toString());
    }

    public Treatment(int id, int v1Value, int issueIndex, int othersValue, String scenario) {
	this.id = id;
	this.iv1 = v1Value;
	this.scenario = scenario;

	if (this.scenario.equalsIgnoreCase("Mobility")) {
	    this.ivIssuesMobility[issueIndex] = (int) (((double) v1Value / 100) * othersValue);
	} else {
	    this.ivIssuesDrones[issueIndex] = (int) (((double) v1Value / 100) * othersValue);

	}

	if (v1Value == 1) {
	    if (this.scenario.equalsIgnoreCase("Mobility")) {
		this.ivIssuesMobility[issueIndex] = 1;

	    } else {
		this.ivIssuesDrones[issueIndex] = 1;
	    }

	    //// System.out.println(this.toString());
	    return;
	}
	if (othersValue != 100) {
	    List<Integer> randomValues = Utilities
		    .generateRandomValues((int) ((100 - othersValue) * ((double) v1Value / 100)), 4);
	    int currentIndexRandomValues = 0;

	    if (this.scenario.equalsIgnoreCase("Mobility")) {
		for (int i = 0; i < this.ivIssuesMobility.length; i++) {
		    if (i != issueIndex) {
			this.ivIssuesMobility[i] = randomValues.get(currentIndexRandomValues);
			currentIndexRandomValues++;
		    }
		}

	    } else {
		for (int i = 0; i < this.ivIssuesDrones.length; i++) {
		    if (i != issueIndex) {
			this.ivIssuesDrones[i] = randomValues.get(currentIndexRandomValues);
			currentIndexRandomValues++;
		    }
		}

	    }

	}
	System.out.println(this.toString());
    }

    public List<Issue> getIssues() {
	return issues;
    }

    /*
     * public String toStringExtended(String scenario) { String result = ""; if
     * (scenario.equalsIgnoreCase("Mobility")) { ivIssues = ivIssuesMobility;
     * result = toString(); } else { ivIssues = ivIssuesDrones; result =
     * toString(); } return result; }
     */
    @Override
    public String toString() {

	if (this.scenario.equalsIgnoreCase("Drones")) {
	    return "Treatment [id=" + this.id + ", iv1=" + iv1 + ", ivIssuesDrones=" + Arrays.toString(ivIssuesDrones)
		    + "]";

	} else {
	    return "Treatment [id=" + this.id + ", iv1=" + iv1 + ", ivIssuesMobility="
		    + Arrays.toString(ivIssuesMobility) + "]";

	}

    }

    public String getCsvFileHeader(String commaDelimiter) {
	String result = "id" + commaDelimiter + "iv1" + commaDelimiter + "iv2" + commaDelimiter + "iv3" + commaDelimiter
		+ "iv4" + commaDelimiter + "iv5" + commaDelimiter + "iv6";
	return result;
    }

    /*
     * public String toCsvExtended(String commaDelimiter, String scenario) {
     * String result = ""; if (scenario.equalsIgnoreCase("Mobility")) { ivIssues
     * = ivIssuesMobility; result = toCsv(commaDelimiter); } else { ivIssues =
     * ivIssuesDrones; result = toCsv(commaDelimiter); } return result; }
     */
    public String toCsv(String commaDelimiter) {
	String result = "";
	result += this.id + commaDelimiter;
	result += this.iv1 + commaDelimiter;
	if (this.scenario.equalsIgnoreCase("Drones")) {
	    result += Arrays.toString(ivIssuesDrones).replace(" ", "").replace("[", "").replace("]", "").replace(",",
		    commaDelimiter);
	} else {
	    result += Arrays.toString(ivIssuesMobility).replace(" ", "").replace("[", "").replace("]", "").replace(",",
		    commaDelimiter);
	}

	return result;
    }

    public int getIv1() {
	return iv1;
    }

    public void setIv1(int iv1) {
	this.iv1 = iv1;
    }

    public int[] getIvIssuesDrones() {
	return this.ivIssuesDrones;
    }

    public int[] getIvIssuesMobility() {
	return this.ivIssuesMobility;
    }

    public void setIvIssuesMobility(int[] ivIssues) {
	this.ivIssuesMobility = ivIssues;
    }

    public void setIvIssuesDrones(int[] ivIssues) {
	this.ivIssuesDrones = ivIssues;
    }

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }

    public Treatment clone() {
	Treatment result = new Treatment();
	result.id = this.id;
	result.iv1 = this.iv1;
	result.scenario = this.scenario;
	result.ivIssuesDrones = this.ivIssuesDrones;
	result.ivIssuesMobility = this.ivIssuesMobility;
	result.ivIssueNamesDrones = this.ivIssueNamesDrones;
	result.ivIssueNamesMobility = this.ivIssueNamesMobility;
	result.issues = this.issues;
	return result;
    }

}
