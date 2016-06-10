package allow.adaptation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import allow.adaptation.ensemble.Issue;
import allow.adaptation.model.IssueCommunication;
import allow.adaptation.model.IssueResolution;

public class Test {

    public static void main(String args[]) {
	Map<IssueResolution, List<IssueCommunication>> map = new HashMap<IssueResolution, List<IssueCommunication>>();

	Issue issue = new Issue();
	issue.setIssueType("DriverNotifyRouteInterrupted");

	IssueResolution resolution1 = new IssueResolution(1, "ISSUE_TRIGGERED", null, null, issue, null);
	List<IssueCommunication> relatedComs = new ArrayList<IssueCommunication>();
	map.put(resolution1, relatedComs);

	Iterator<IssueResolution> iterate = map.keySet().iterator();

	while (iterate.hasNext()) {
	    List<IssueCommunication> i = map.get(iterate.next());

	    if (i.size() > 1) {
		iterate.remove();
		Issue issue1 = new Issue();
		issue1.setIssueType("DriverNotifyRouteInterrupted");

		IssueResolution resolution2 = new IssueResolution(1, "ISSUE_TRIGGERED", null, null, issue1, null);
		List<IssueCommunication> relatedComs1 = new ArrayList<IssueCommunication>();
		map.put(resolution2, relatedComs1);

	    }
	}

	System.out.println(map);
    }
}
