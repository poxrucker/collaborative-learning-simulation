package de.dfki.simulator;

import java.util.Map;

public abstract class SimulationModel {

	public abstract void setup(Map<String, Object> configuration);
	
	public abstract void step();
	
	public abstract void finish();
	
}
