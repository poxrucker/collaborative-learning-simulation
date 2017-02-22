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
				
		LogoList settings = args[2].getList();
		params.Scenario = (String) settings.get(0);
		params.PercentInitiallyInformed = (int) (double) settings.get(1);
		params.PercentParticipating = (int) (double) settings.get(2);
		params.PercentSharing = (int) (double) settings.get(3);
		params.WithWorkers = (boolean) settings.get(4);
		params.WithStudents = (boolean) settings.get(5);
		params.WithChildren = (boolean) settings.get(6);
		params.WithHomemaker = (boolean) settings.get(7);
		params.Car = (boolean) settings.get(8);
		params.Bus = (boolean) settings.get(9);
		params.Walk = (boolean) settings.get(10);
		params.Bike = (boolean) settings.get(11);
		params.Idle = (boolean) settings.get(12);
		params.EarlyShiftWorkers = (boolean) settings.get(13);
		params.PercentEarlyShiftWorkers = (int) (double) settings.get(14);
		params.BackShiftWorkers = (boolean) settings.get(15);
		params.PercentBackShiftWorkers = (int) (double) settings.get(16);
		params.ExtraHomemaker = (boolean) settings.get(17);
		params.PercentExtraHomemaker = (int) (double) settings.get(18);
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
		listBuilder.add(s.getMeanPriorCarTravelTime());
		listBuilder.add(s.getMeanPosteriorCarTravelTime());
		listBuilder.add(s.getMeanPriorBusTravelTime());
		listBuilder.add(s.getMeanPosteriorBusTravelTime());
		listBuilder.add((double)s.getNumberOfPlannings());
		listBuilder.add((double)s.getInformedPlannings());
		listBuilder.add((double)s.getNumberOfAffectedPlannings());
		listBuilder.add((double)s.getInformedPlanningsAffected());
		listBuilder.add((double)s.getIntermediateReplannings());
		listBuilder.add((double)s.getConstructionSiteReplannings());
		listBuilder.add((double)s.getMeanPriorCarTravelTimeConstructionSite());
		listBuilder.add((double)s.getMeanPosteriorCarTravelTimeConstructionSite());
		listBuilder.add((double)s.getMeanPriorTripDistance());
		listBuilder.add((double)s.getMeanPosteriorPriorTripDistance());
		return listBuilder.toLogoList();
	}

	@Override
	public Syntax getSyntax() {
		int right[] = new int[] { Syntax.StringType(), Syntax.NumberType(), Syntax.ListType() };
		return Syntax.reporterSyntax(right, Syntax.ListType() );
	}
}
