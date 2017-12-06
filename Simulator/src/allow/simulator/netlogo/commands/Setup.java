package allow.simulator.netlogo.commands;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.nlogo.agent.World;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Syntax;

import allow.simulator.core.AllowSimulationModel;
import allow.simulator.core.Configuration;
import allow.simulator.core.SimulationParameter;
import allow.simulator.netlogo.agent.NetLogoWrapper;
import allow.simulator.statistics.Statistics;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class Setup extends DefaultReporter {
		
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
		params.Model = (String) settings.get(0);
		params.PercentUsers = (int) (double) settings.get(1);
		params.PercentSensorCars = (int) (double) (settings.get(2));
		org.nlogo.api.World w = context.getAgent().world();
		params.GridResX = w.worldWidth();
		params.GridResY = w.worldHeight();
		params.StreetParkingPath = (String) settings.get(3);
		params.GarageParkingPath = (String) settings.get(4);
		params.DataScalingFactor = (double) settings.get(5);
		params.ValidTime = (int) (double) settings.get(6);
		
		// Create simulator and NetLogo binding
		AllowSimulationModel simulator = null;
		Map<String, Object> parameters = new Object2ObjectOpenHashMap<>();
		parameters.put("config", config);
		parameters.put("params", parameters);
		
		try {
			simulator = new AllowSimulationModel();
			simulator.setup(parameters);
			NetLogoWrapper.initialize(params.BehaviourSpaceRunNumber, simulator, (World) context.getAgent().world());
			
		} catch (Exception e) {
			throw new ExtensionException(e.getMessage());		
		} 
		
		// List buffer
		LogoListBuilder listBuilder = new LogoListBuilder();
		allow.simulator.core.Context ctx = simulator.getContext();
		ctx.getStatistics().updateGlobalStatistics(ctx);
		
		listBuilder.add(ctx.getTime().toString());
		// listBuilder.add(ctx.getWeather().getCurrentState().getDescription());
		Statistics s = ctx.getStatistics();
    listBuilder.add((double)s.getSuccessfulParking());
    listBuilder.add((double)s.getFailedParking());
    listBuilder.add((double)s.getMeanSearchTimeParking());
    listBuilder.add((double)s.getReasonMaxSearchTimeExceeded());
    listBuilder.add((double)s.getReasonNoPath());
    listBuilder.add((double)ctx.getParkingMap().getTotalNumberOfStreetParkingSpots());
    listBuilder.add((double)ctx.getParkingMap().getTotalNumberOfGarageParkingSpots());
    listBuilder.add((double)ctx.getParkingMap().getTotalNumberOfFreeStreetParkingSpots());
    listBuilder.add((double)ctx.getParkingMap().getTotalNumberOfFreeGarageParkingSpots());
    listBuilder.add((double)s.getMeanUtilityParking());
    listBuilder.add((double) s.getCarJourneyRatio());
    listBuilder.add((double) s.getTransitJourneyRatio());
    listBuilder.add((double) s.getWalkJourneyRatio());
    listBuilder.add((double)s.getMeanParkingCosts());
    listBuilder.add((double)s.getMeanParkingWalkingDistance());
		// listBuilder.add((double)s.getTotalStreetNetworkLength());
		// listBuilder.add((double)s.getVisitedStreetNetworkLength());
		return listBuilder.toLogoList();
	}

	@Override
	public Syntax getSyntax() {
		int right[] = new int[] { Syntax.StringType(), Syntax.NumberType(), Syntax.StringType(), Syntax.NumberType(), Syntax.ListType() };
		return Syntax.reporterSyntax(right, Syntax.ListType() );
	}
}
