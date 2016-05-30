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
		Agent a = (Agent) context.getAgent();
		
		if (a instanceof IAgentAdapter) {
			IAgentAdapter p = (IAgentAdapter) a;
			return p.execute();

		} else {
			throw new ExtensionException("Error: Calling agent must be an extension agent instance.");
		}	
	}
	
	public Syntax getSyntax() {
		return Syntax.reporterSyntax(Syntax.BooleanType());
	}
}
