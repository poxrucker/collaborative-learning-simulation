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
		// Get String containing path to configuration file.
		Path configPath = Paths.get(args[0].getString());
	
		// Read configuration.
		Configuration config = null;
		
		try {
			config = Configuration.fromJSON(configPath);
			
		} catch (IOException e) {
			throw new ExtensionException(e.getMessage());
		}
		
		// Setup simulator from configuration.
		SimulationParameter params = new SimulationParameter();
		params.BehaviourSpaceRunNumber = args[1].getIntValue();
		params.PercentInitiallyInformed = args[2].getIntValue();
		params.PercentParticipating = args[3].getIntValue();
		params.PercentSharing = args[4].getIntValue();
		
		org.nlogo.api.World w = context.getAgent().world();
		params.GridResX = w.worldWidth();
		params.GridResY = w.worldHeight();

		try {
			Simulator.Instance().setup(config, params);
			NetLogoWrapper.initialize(Simulator.Instance(), (World) context.getAgent().world());
			
		} catch (IOException e) {
			throw new ExtensionException(e.getMessage());
		}

		// List buffer.
		LogoListBuilder listBuilder = new LogoListBuilder();
		allow.simulator.core.Context ctx = Simulator.Instance().getContext();
		ctx.getStatistics().updateGlobalStatistics(ctx);
		listBuilder.add(ctx.getTime().toString());
		listBuilder.add(ctx.getWeather().getCurrentState().getDescription());
		Statistics s = ctx.getStatistics();
		listBuilder.add(s.getMeanPriorCarTravelTime());
		listBuilder.add(s.getMeanPosteriorCarTravelTime());
		listBuilder.add(s.getMeanPriorBusTravelTime());
		listBuilder.add(s.getMeanPosteriorBusTravelTime());
		listBuilder.add((double)s.getNumberOfReplanings());
		listBuilder.add((double)s.getNumberOfDiscoveries());
		return listBuilder.toLogoList();
	}

	@Override
	public Syntax getSyntax() {
		int right[] = new int[] { Syntax.StringType(), Syntax.NumberType(), Syntax.NumberType(), Syntax.NumberType(), Syntax.NumberType() };
		return Syntax.reporterSyntax(right, Syntax.ListType() );
	}
}
