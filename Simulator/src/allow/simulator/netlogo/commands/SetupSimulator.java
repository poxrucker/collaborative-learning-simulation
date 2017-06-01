package allow.simulator.netlogo.commands;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.nlogo.agent.World;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Syntax;

import allow.simulator.core.Configuration;
import allow.simulator.core.SimulationParameter;
import allow.simulator.core.Simulator;
import allow.simulator.netlogo.agent.NetLogoWrapper;
import allow.simulator.statistics.Statistics;

public class SetupSimulator extends DefaultReporter {
		
	@Override
	public Object report(Argument[] args, Context context) throws ExtensionException, LogoException {
		// Get String containing path to configuration file
		Path configPath = Paths.get(args[0].getString());

		// Read configuration
		Configuration config = null;
		
		try {
			config = Configuration.fromJSON(configPath);
			
		} catch (IOException e) {
			throw new ExtensionException(e.getMessage());
		}
		
		// Parse simulation model parameters
		SimulationParameter params = new SimulationParameter();
		params.BehaviourSpaceRunNumber = args[1].getIntValue();
	
		LogoList settings = args[4].getList();
		params.Scenario = (String) settings.get(0);
		params.PercentParticipating = (int) (double) settings.get(1);
		params.MaximumVisitedTime = (int) (double) (settings.get(2));
		org.nlogo.api.World w = context.getAgent().world();
		params.GridResX = w.worldWidth();
		params.GridResY = w.worldHeight();

		// Create simulator and NetLogo binding
		Simulator simulator = null;
		
		try {
			simulator = new Simulator();
			simulator.setup(config, params);
			NetLogoWrapper.initialize(params.BehaviourSpaceRunNumber, simulator, (World) context.getAgent().world());
			
		} catch (IOException e) {
			throw new ExtensionException(e.getMessage());
		}

		// List buffer
		LogoListBuilder listBuilder = new LogoListBuilder();
		allow.simulator.core.Context ctx = simulator.getContext();
		ctx.getStatistics().updateGlobalStatistics(ctx);
		listBuilder.add(ctx.getTime().toString());
		listBuilder.add(ctx.getWeather().getCurrentState().getDescription());
		Statistics s = ctx.getStatistics();
		listBuilder.add((double)s.getTotalStreetNetworkLength());
		listBuilder.add((double)s.getVisitedStreetNetworkLength());
		return listBuilder.toLogoList();
	}

	@Override
	public Syntax getSyntax() {
		int right[] = new int[] { Syntax.StringType(), Syntax.NumberType(), Syntax.StringType(), Syntax.NumberType(), Syntax.ListType() };
		return Syntax.reporterSyntax(right, Syntax.ListType() );
	}
}
