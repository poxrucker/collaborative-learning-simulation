package allow.adaptation.test;

import java.io.FileNotFoundException;

import allow.simulator.adaptation.CollectiveAdaptation;
import allow.simulator.adaptation.Ensemble;
import allow.simulator.adaptation.Issue;
import allow.simulator.core.Context;
import allow.simulator.entity.Entity;
import allow.simulator.entity.EntityType;
import allow.simulator.entity.utility.IUtility;
import allow.simulator.entity.utility.Preferences;



public class AdaptationTest {
	 

	    public static void main(String[] args)  {
	    	
	    	CollectiveAdaptation ca = new CollectiveAdaptation();
	    	Entity BusDriver = null;
	    		
	    	Ensemble ensemble = new Ensemble(BusDriver);
	    	
			Issue issue = Issue.BUS_BREAKDOWN;
			try {
				ca.solveAdaptation(issue, ensemble);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	

	    }

}
