package allow.simulator.netlogo.agent;

import org.nlogo.agent.World;

import allow.simulator.world.Transformation;
import de.dfki.simulation.AbstractSimulationModel;

public interface ISimulationModelWrapper {

	void wrap(AbstractSimulationModel simulationModel);
	
	Transformation getTransformation();
  
  World getWorld();
  
  AbstractSimulationModel getSimulationModel();
  
}
