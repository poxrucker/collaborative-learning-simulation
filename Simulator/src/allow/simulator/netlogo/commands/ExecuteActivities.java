package allow.simulator.netlogo.commands;

import org.nlogo.agent.Agent;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;

import allow.simulator.netlogo.agent.IAgentAdapter;

/**
 * NetLogo command to execute one step of a person.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public class ExecuteActivities extends DefaultReporter {
	
	@Override
	public Object report(Argument[] args, Context context) throws ExtensionException, LogoException {
		Agent agent = (Agent) context.getAgent();
		
		if (agent instanceof IAgentAdapter) {
			IAgentAdapter<?> p = (IAgentAdapter<?>) agent;
			return p.execute();
			
			/*if (!(p.getEntity() instanceof Person))
				return false;
			
			Person person = (Person)p.getEntity();
			SimulationParameter param = person.getContext().getSimulationParameters();
			Activity a = person.getFlow().getCurrentActivity();
			
			if (a == null) 
				return false;
			
			return (param.Car && (a.getType() == ActivityType.DRIVE))
					|| (param.Bus && (a.getType() == ActivityType.USE_PUBLIC_TRANSPORT) || a.getType() == ActivityType.WAIT)
					|| (param.Walk && (a.getType() == ActivityType.WALK))
					|| (param.Bike && (a.getType() == ActivityType.WALK));*/

		} else {
			throw new ExtensionException("Error: Calling agent must be an extension agent instance.");
		}	
	}
	
	public Syntax getSyntax() {
		return Syntax.reporterSyntax(Syntax.BooleanType());
	}
}
