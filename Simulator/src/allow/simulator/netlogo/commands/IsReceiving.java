package allow.simulator.netlogo.commands;

import org.nlogo.agent.Agent;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;

import allow.simulator.entity.Entity;
import allow.simulator.entity.Person;
import allow.simulator.netlogo.agent.IAgentAdapter;

public class IsReceiving extends DefaultReporter {

	@Override
	public Object report(Argument[] args, Context context) throws ExtensionException, LogoException {
		Agent a = (Agent) context.getAgent();
		
		if (!(a instanceof IAgentAdapter))
			throw new ExtensionException("Error: Calling agent must be an extension agent.");
		
		IAgentAdapter temp = (IAgentAdapter) a;
		Entity entity = temp.getEntity();
		
		if (!(entity instanceof Person))
			throw new ExtensionException("Error: Calling agent must be a Person agent.");
		
		Person p = (Person)entity;
		return p.isReceiving();
	}
	
	public Syntax getSyntax() {
		return Syntax.reporterSyntax(Syntax.BooleanType());
	}
	
}
