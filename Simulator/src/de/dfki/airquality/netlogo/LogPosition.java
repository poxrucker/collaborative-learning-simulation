package de.dfki.airquality.netlogo;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;

import allow.simulator.entity.Entity;
import allow.simulator.entity.Person;
import allow.simulator.netlogo.agent.IAgentAdapter;

public class LogPosition extends DefaultCommand {
  
  @Override
  public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {
    AgentSet agents = (AgentSet) args[0].getAgentSet();
    
    for (org.nlogo.api.Agent agent : agents.agents()) {
      Agent a = (Agent) agent;
      
      if (!(a instanceof IAgentAdapter))
        throw new ExtensionException("Error: Calling agent must be an extension agent.");
      
      IAgentAdapter<?> temp = (IAgentAdapter<?>) a;
      Entity entity = temp.getEntity();
      
      if (!(entity instanceof Person))
        throw new ExtensionException("Error: Calling agent must be a Person agent.");
      
      Person p = (Person)entity;
    }
  }
  
  public Syntax getSyntax() {
    return Syntax.commandSyntax(new int[] { Syntax.AgentsetType() });
  }
}
