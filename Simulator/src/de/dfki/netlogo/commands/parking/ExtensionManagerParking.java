package de.dfki.netlogo.commands.parking;

import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.PrimitiveManager;

import allow.simulator.netlogo.commands.ExecuteActivities;
import allow.simulator.netlogo.commands.ExecuteBus;
import allow.simulator.netlogo.commands.ExecuteTransportAgencies;
import allow.simulator.netlogo.commands.ShowActivity;
import allow.simulator.netlogo.commands.ShowNotAtHome;


public class ExtensionManagerParking extends DefaultClassManager {

	public static final String EXTENSION_NAME = "mobility";
	
	@Override
	public void load(PrimitiveManager primitiveManager) throws ExtensionException {
		primitiveManager.addPrimitive("execute-activities", new ExecuteActivities());
		primitiveManager.addPrimitive("execute-bus", new ExecuteBus());
		primitiveManager.addPrimitive("show-activity", new ShowActivity());
		primitiveManager.addPrimitive("showNotAtHome", new ShowNotAtHome());
		primitiveManager.addPrimitive("execute-transport-agency", new ExecuteTransportAgencies());
		primitiveManager.addPrimitive("setup-simulator", new Setup());
		primitiveManager.addPrimitive("shutdown-simulator", new Finish());
		primitiveManager.addPrimitive("tick", new Tick());
		primitiveManager.addPrimitive("has-sensor-car", new HasSensorCar());
    primitiveManager.addPrimitive("is-user", new IsUser());
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
