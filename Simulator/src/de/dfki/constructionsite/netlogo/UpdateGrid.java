package de.dfki.constructionsite.netlogo;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.AgentSet.Iterator;
import org.nlogo.agent.Patch;
import org.nlogo.agent.World;
import org.nlogo.api.AgentException;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Syntax;

import allow.simulator.entity.Person;
import allow.simulator.netlogo.agent.IAgentAdapter;
import allow.simulator.netlogo.agent.NetLogoSimulationModelWrapper;

public class UpdateGrid extends DefaultCommand {

	@Override
	public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {
		// Parameters
		int runId = args[0].getIntValue();
		int cellSizeX = args[1].getIntValue();
		int cellSizeY = args[2].getIntValue();
		boolean referenceAll = args[3].getString().equals("all");
		int minAgents = args[4].getIntValue();
		
		// Get world
		NetLogoSimulationModelWrapper wrapper = NetLogoSimulationModelWrapper.Instance(runId);
		World world = wrapper.getWorld();
		
		int gridWidth = (world.worldWidth() % cellSizeX == 0) ? (world.worldWidth() / cellSizeX) : (world.worldWidth() / cellSizeX + 1);
		int gridHeight = (world.worldHeight() % cellSizeY == 0) ? (world.worldHeight() / cellSizeY) : (world.worldHeight() / cellSizeY + 1);
		int[] totalNumber = new int[gridWidth * gridHeight];
		double[] informed = new double[gridWidth * gridHeight];
		
		AgentSet patches = world.patches();
		
		// First passing: Count total number of agents and informed agents per cell
		for (Iterator it = patches.iterator(); it.hasNext(); ) {
			Patch p = (Patch) it.next();
			int px = (p.pxcor - world.minPxcor()) / cellSizeX;
			int py = (p.pycor - world.minPycor()) / cellSizeY;
			int index = gridHeight * py + px;
			
			if (index >= totalNumber.length)
				System.out.println("Index error");
			
			AgentSet agents = p.turtlesHereAgentSet();
			
			for (Iterator jt = agents.iterator(); jt.hasNext(); ) {
				Agent agent = jt.next();
				
				if (!(agent instanceof IAgentAdapter))
					continue;
				
				IAgentAdapter<?> wrapped = (IAgentAdapter<?>) agent;
				
				if (!(wrapped.getEntity() instanceof Person))
					continue;
				
				Person person = (Person)wrapped.getEntity();
				
				// Count reference
				if (referenceAll)
					totalNumber[index]++;
				else if (person.isReceiving())
					totalNumber[index]++;
				
				// Count informed persons
				if (person.isInformed())
					informed[index]++;
			}
		}
		
		// Calculate percentages		
		for (int i = 0; i < informed.length; i++)
			informed[i] = (totalNumber[i] != 0) ? (informed[i] / totalNumber[i]) : 0;
		
		// Second pass: Color patches according to percentage
		for (Iterator it = patches.iterator(); it.hasNext(); ) {
			Patch p = (Patch) it.next();
			int px = (p.pxcor - world.minPxcor()) / cellSizeX;
			int py = (p.pycor - world.minPycor()) / cellSizeY;
			int index = gridHeight * py + px;
			
			try {
				if (totalNumber[index] < Math.max(minAgents, 1)) {
					p.pcolor(9.9);
				} else {
					p.pcolor(getGradientColor(informed[index]));
				}
			} catch (AgentException e) {
				e.printStackTrace();
			}
		}
		
	}
	private static final double[][] COLORS = { { 255, 0, 0 }, { 255, 255, 0 }, { 0, 255, 0 } };
	
	private LogoList getGradientColor(double value) {
		LogoListBuilder bldr = new LogoListBuilder();
		
		if (value <= 0) {
			bldr.add(COLORS[0][0]);
			bldr.add(COLORS[0][1]);
			bldr.add(COLORS[0][2]);
			
		} else if (value >= 1) {
			bldr.add(COLORS[COLORS.length - 1][0]);
			bldr.add(COLORS[COLORS.length - 1][1]);
			bldr.add(COLORS[COLORS.length - 1][2]);
			
		} else {
			value = value * (COLORS.length - 1);
			int idx1  = (int)value;                
			int idx2  = idx1 + 1;                    
			double fractBetween = value - idx1;
			bldr.add((COLORS[idx2][0] - COLORS[idx1][0]) * fractBetween + COLORS[idx1][0]);
			bldr.add((COLORS[idx2][1] - COLORS[idx1][1]) * fractBetween + COLORS[idx1][1]);
			bldr.add((COLORS[idx2][2] - COLORS[idx1][2]) * fractBetween + COLORS[idx1][2]);
		}
		return bldr.toLogoList();
	}
	
	public Syntax getSyntax() {
		int right[] = { Syntax.NumberType(), Syntax.NumberType(), Syntax.NumberType(), Syntax.StringType(), Syntax.NumberType() };
		return Syntax.commandSyntax(right);
	}

}
