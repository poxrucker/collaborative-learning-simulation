package de.dfki.netlogo.commands.parking;

import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Syntax;

import allow.simulator.netlogo.agent.NetLogoWrapper;
import allow.simulator.statistics.Statistics;
import de.dfki.parking.simulation.ParkingSimulationModel;

public class Tick extends DefaultReporter {

	@Override
	public Object report(Argument[] args, Context context) throws ExtensionException, LogoException {		
		// Get runId
		int runId = args[0].getIntValue();
		
		// NetLogoWrapper
		NetLogoWrapper wrapper = NetLogoWrapper.Instance(runId);
		ParkingSimulationModel simulator = (ParkingSimulationModel)wrapper.getSimulator();
		
		try {
			simulator.tick();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Return context and statistics.		
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

	public Syntax getSyntax() {
		int right[] = { Syntax.NumberType(), Syntax.NumberType() };
		return Syntax.reporterSyntax(right, Syntax.ListType());
	}
}
