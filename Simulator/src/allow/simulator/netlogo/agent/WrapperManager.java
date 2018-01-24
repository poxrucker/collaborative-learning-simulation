package allow.simulator.netlogo.agent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.nlogo.agent.World;

import de.dfki.simulation.AbstractSimulationModel;

public final class WrapperManager {
  // Instances to manage
  private final Map<Integer, ISimulationModelWrapper> instances;
  
  private WrapperManager() { 
    instances = new ConcurrentHashMap<Integer, ISimulationModelWrapper>();
  }
  
  public ISimulationModelWrapper initialize(int runId, AbstractSimulationModel simulationModel, World world) {
    ISimulationModelWrapper instance = new NetLogoSimulationModelWrapper(world);
    instance.wrap(simulationModel);
    instances.put(runId, instance);
    return instance;
  }
  
  public void delete(int runId) throws Exception {
    ISimulationModelWrapper wrapper = instances.remove(runId);
    wrapper.getSimulationModel().finish();
  }

  public ISimulationModelWrapper get(int runId) {
    ISimulationModelWrapper instance = instances.get(runId);
    
    if (instance == null)
      throw new UnsupportedOperationException();
    
    return instance;
  }
  
  private static WrapperManager instance = new WrapperManager();
  
  public static WrapperManager getInstance() {
    return instance;
  }
}
