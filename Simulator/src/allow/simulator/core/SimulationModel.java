package allow.simulator.core;

import java.util.Map;

public abstract class SimulationModel {

	public abstract void load(Map<String, Object> parameters);
	
	public abstract void step();
	
	public abstract void finish();
	
}
