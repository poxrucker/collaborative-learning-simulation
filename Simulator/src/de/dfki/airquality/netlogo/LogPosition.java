package de.dfki.airquality.netlogo;

import java.io.IOException;

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
import allow.simulator.netlogo.agent.ISimulationModelWrapper;
import allow.simulator.netlogo.agent.WrapperManager;
import de.dfki.airquality.simulation.AirQualitySimulationModel;

public class LogPosition extends DefaultCommand {
  
  @Override
  public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {
    int runId = args[0].getIntValue();
    ISimulationModelWrapper wrapper = WrapperManager.getInstance().get(runId);
    AirQualitySimulationModel simulator = (AirQualitySimulationModel) wrapper.getSimulationModel();
    
    AgentSet agents = (AgentSet) args[1].getAgentSet();
    
    if (agents.count() == 0)
      return;
    
    StringBuilder bldr = new StringBuilder();
    int i = 0;
    
    for (org.nlogo.api.Agent agent : agents.agents()) {
      Agent a = (Agent) agent;
      
      if (!(a instanceof IAgentAdapter))
        throw new ExtensionException("Error: Calling agent must be an extension agent.");
      
      IAgentAdapter<?> temp = (IAgentAdapter<?>) a;
      Entity entity = temp.getEntity();
      
      if (!(entity instanceof Person))
        throw new ExtensionException("Error: Calling agent must be a Person agent.");
      
      Person p = (Person)entity;
      bldr.append(p.getPosition().x + "," + p.getPosition().y);
      i++;
      
      if (i < agents.count()) {
        bldr.append(",");
      } else {
        bldr.append("\n");
      }
      p.setLastMeasurement(p.getContext().getTime().getTimestamp());
    }
    try {
      simulator.logPosition(bldr.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public Syntax getSyntax() {
    return Syntax.commandSyntax(new int[] { Syntax.NumberType(), Syntax.AgentsetType() });
  }
}
