package allow.simulator.netlogo.commands;

import org.nlogo.agent.Agent;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;

import allow.simulator.entity.EntityTypes;
import allow.simulator.entity.Person;
import allow.simulator.netlogo.agent.IAgentAdapter;

public class GetPersonRole extends DefaultReporter
{
	@Override
	public Object report(Argument[] args, Context context) throws ExtensionException, LogoException {
		Agent a = (Agent) context.getAgent();
		
		if (!(a instanceof IAgentAdapter))
			throw new ExtensionException("Error: Calling agent must be an extension agent instance.");
		
		IAgentAdapter<?> temp = (IAgentAdapter<?>) a;
		
		if (!(temp.getEntity().getType() == EntityTypes.PERSON))
			throw new ExtensionException("Error: Calling agent must wrap a person entity.");
		Person p = (Person) temp.getEntity();
		return p.getProfile().toString();
	}
	
	public Syntax getSyntax() {
		return Syntax.reporterSyntax(Syntax.StringType());
	}
}
