package de.dfki.netlogo.commands.constructionsite;

import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.PrimitiveManager;

import allow.simulator.netlogo.commands.ExchangeKnowledge;
import allow.simulator.netlogo.commands.ExecuteActivities;
import allow.simulator.netlogo.commands.ExecuteBus;
import allow.simulator.netlogo.commands.ExecuteTransportAgencies;
import allow.simulator.netlogo.commands.GetPersonRole;
import allow.simulator.netlogo.commands.GetPersonRoles;
import allow.simulator.netlogo.commands.GetRegion;
import allow.simulator.netlogo.commands.GetRegions;
import allow.simulator.netlogo.commands.ShowActivity;
import allow.simulator.netlogo.commands.ShowNotAtHome;


public class ExtensionManagerConstructionSite extends DefaultClassManager {

	public static final String EXTENSION_NAME = "mobility";
	
	@Override
	public void load(PrimitiveManager primitiveManager) throws ExtensionException {
		primitiveManager.addPrimitive("execute-activities", new ExecuteActivities());
		primitiveManager.addPrimitive("exchange-knowledge", new ExchangeKnowledge());
		primitiveManager.addPrimitive("execute-bus", new ExecuteBus());
		primitiveManager.addPrimitive("show-activity", new ShowActivity());
		primitiveManager.addPrimitive("showNotAtHome", new ShowNotAtHome());
		primitiveManager.addPrimitive("execute-transport-agency", new ExecuteTransportAgencies());
		primitiveManager.addPrimitive("setup-simulator", new Setup());
		primitiveManager.addPrimitive("shutdown-simulator", new Finish());
		primitiveManager.addPrimitive("tick", new Tick());
		primitiveManager.addPrimitive("get-regions", new GetRegions());
		primitiveManager.addPrimitive("get-region", new GetRegion());
		primitiveManager.addPrimitive("get-person-roles", new GetPersonRoles());
		primitiveManager.addPrimitive("get-person-role", new GetPersonRole());
		primitiveManager.addPrimitive("update-grid", new UpdateGrid());
		primitiveManager.addPrimitive("is-informed", new IsInformed());
		primitiveManager.addPrimitive("is-sharing", new IsSharing());
		primitiveManager.addPrimitive("is-receiving", new IsReceiving());
	}
	
	@Override
    public java.util.List<String> additionalJars() {
		return java.util.Arrays.asList( new String[] {
            "httpclient-4.3.2.jar",
            "httpclient-cache-4.3.2.jar",
            "httpcore-4.3.1.jar"
			});
    }

}
