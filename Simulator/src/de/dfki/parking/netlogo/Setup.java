package de.dfki.parking.netlogo;

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

import allow.simulator.core.Configuration;
import allow.simulator.core.SimulationParameter;
import allow.simulator.netlogo.agent.NetLogoSimulationModelWrapper;
import allow.simulator.statistics.Statistics;
import de.dfki.parking.simulation.ParkingSimulationModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class Setup extends DefaultReporter {
	// Constant maximum values and weights for preferences
  // (could be read from file or obtained from Netlogo)
  private static final double C_WEIGHT_HOMEMAKER = 35.0;
  private static final double WD_WEIGHT_HOMEMAKER = 50.0;
  private static final double ST_WEIGHT_HOMEMAKER = 15.0;
  private static final double C_WEIGHT_STUDENT = 50.0;
  private static final double WD_WEIGHT_STUDENT = 15.0;
  private static final double ST_WEIGHT_STUDENT = 35.0;
  private static final double C_WEIGHT_WORKER = 15.0;
  private static final double WD_WEIGHT_WORKER = 35.0;
  private static final double ST_WEIGHT_WORKER = 50.0;
  private static final double C_MAX = 2.8;
  private static final double WD_MAX = 500;
  private static final double ST_MAX = 10 * 60;
  
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
    LogoList settings = args[4].getList();

		SimulationParameter params = new SimulationParameter();
		params.BehaviourSpaceRunNumber = args[1].getIntValue();
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
		params.CMax = C_MAX;
		params.WdMax = WD_MAX;
		params.StMax = ST_MAX;
		params.CWeightHomemaker = C_WEIGHT_HOMEMAKER;
		params.WdWeightHomemaker = WD_WEIGHT_HOMEMAKER;
		params.StWeightHomemaker = ST_WEIGHT_HOMEMAKER;
		params.CWeightStudent = C_WEIGHT_STUDENT;
    params.WdWeightStudent = WD_WEIGHT_STUDENT;
    params.StWeightStudent = ST_WEIGHT_STUDENT;
    params.CWeightWorker = C_WEIGHT_WORKER;
    params.WdWeightWorker = WD_WEIGHT_WORKER;
    params.StWeightWorker = ST_WEIGHT_WORKER;
		
		// Create simulator and NetLogo binding
		ParkingSimulationModel simulator = null;
		Map<String, Object> parameters = new Object2ObjectOpenHashMap<>();
		parameters.put("config", config);
		parameters.put("params", params);
		
		try {
			simulator = new ParkingSimulationModel();
			simulator.setup(parameters);
			NetLogoSimulationModelWrapper.initialize(params.BehaviourSpaceRunNumber, simulator, (World) context.getAgent().world());
			
		} catch (Exception e) {
			throw new ExtensionException(e.getMessage());		
		} 
		
		// List buffer
		LogoListBuilder listBuilder = new LogoListBuilder();
		allow.simulator.core.Context ctx = simulator.getContext();
		ctx.getStatistics().updateGlobalStatistics(ctx);
		
		listBuilder.add(ctx.getTime().toString());
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
		return listBuilder.toLogoList();
	}

	@Override
	public Syntax getSyntax() {
		int right[] = new int[] { Syntax.StringType(), Syntax.NumberType(), Syntax.StringType(), Syntax.NumberType(), Syntax.ListType() };
		return Syntax.reporterSyntax(right, Syntax.ListType() );
	}
}
