package allow.simulator.netlogo.commands;

import java.io.IOException;

import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Syntax;

import allow.simulator.core.Simulator;
import allow.simulator.netlogo.agent.NetLogoWrapper;
import allow.simulator.statistics.Statistics;

public class Tick extends DefaultReporter {

	@Override
	public Object report(Argument[] args, Context context) throws ExtensionException, LogoException {		
		// Get runId
		int runId = args[0].getIntValue();
		
		// NetLogoWrapper
		NetLogoWrapper wrapper = NetLogoWrapper.Instance(runId);
		Simulator simulator = wrapper.getSimulator();
		
		try {
			simulator.tick();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Return context and statistics.		
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
		listBuilder.add((double)s.getMeanPriorCarTravelTimeConstructionSiteRaw());
		listBuilder.add((double)s.getMeanPosteriorCarTravelTimeConstructionSiteRaw());
		listBuilder.add((double)s.getMeanPriorCarTravelTimeConstructionSiteActual());
		listBuilder.add((double)s.getMeanPosteriorCarTravelTimeConstructionSiteActual());
		listBuilder.add((double)s.getMeanPriorTripDistance());
		listBuilder.add((double)s.getMeanPosteriorPriorTripDistance());
		return listBuilder.toLogoList();
	}

	public Syntax getSyntax() {
		int right[] = { Syntax.NumberType(), Syntax.NumberType() };
		return Syntax.reporterSyntax(right, Syntax.ListType());
	}
}
