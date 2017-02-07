package allow.simulator.netlogo.commands;

import org.nlogo.agent.Agent;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;

import allow.simulator.netlogo.agent.IAgentAdapter;

/**
 * NetLogo command to initiate knowledge exchange.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public class ExchangeKnowledge extends DefaultCommand {

	@Override
	public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {
		Agent a = (Agent) context.getAgent();
		
		if (!(a instanceof IAgentAdapter)) 
			throw new ExtensionException("Error: Calling agent must be an extension agent.");
		
		IAgentAdapter p = (IAgentAdapter) a;
			
		/*if (p.getEntity().getFlow().isIdle())
			return;
		
		ActivityType type = p.getEntity().getFlow().getCurrentActivity().getType();
				
		if (type == ActivityType.DRIVE)*/
			p.exchangeKnowledge();	
	}

}