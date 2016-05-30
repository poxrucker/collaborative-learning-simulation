package allow.simulator.netlogo.agent;

import java.util.Observer;

import org.nlogo.api.AgentException;

import allow.simulator.entity.Entity;

/**
 * Interface connecting simulation entities and NetLogo agents.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public interface IAgentAdapter extends Observer {
	
	/**
	 * Returns the underlying entity.
	 * 
	 * @return Implementation of the entity
	 */
	public Entity getEntity();
	
	/**
	 * Calls the behavioral logic of the underlying entity.
	 * 
	 * @return True, if entity has actually performed an action (e.g. executed an activity), false otherwise
	 * @throws AgentException
	 */
	public boolean execute();
	
	/**
	 * Initiates knowledge exchange.
	 */
	public void exchangeKnowledge();
}
