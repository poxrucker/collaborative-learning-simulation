package allow.simulator.netlogo.commands;

import java.util.List;

import org.nlogo.agent.Agent;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Syntax;

import allow.simulator.core.AllowSimulationModel;
import allow.simulator.netlogo.agent.IAgentAdapter;
import allow.simulator.world.overlay.Area;
import allow.simulator.world.overlay.DistrictArea;
import allow.simulator.world.overlay.DistrictOverlay;

public class GetRegion extends DefaultReporter
{
	@Override
	public Object report(Argument[] args, Context context) throws ExtensionException, LogoException {
		Agent a = (Agent) context.getAgent();
		
		if (a instanceof IAgentAdapter) {
			IAgentAdapter<?> temp = (IAgentAdapter<?>) a;
			DistrictOverlay l = (DistrictOverlay) temp.getEntity().getContext().getWorld().getOverlay(AllowSimulationModel.OVERLAY_DISTRICTS);
			List<Area> areas = l.getAreasContainingPoint(temp.getEntity().getPosition());
			
			LogoListBuilder bldr = new LogoListBuilder();
			boolean added = false;
			
			for (Area area : areas) {
				if (area.getName().equals("default")) {
					continue;
				}
				DistrictArea t = (DistrictArea) area;
				bldr.add(t.getType().toString());
				added = true;
				break;
			}
			
			if (!added) {
				bldr.add("UNKNOWN");
			}
			return bldr.toLogoList();
			
		} else {
			throw new ExtensionException("Error: Calling agent must be an extension agent instance.");
		}
	}
	
	public Syntax getSyntax() {
		return Syntax.reporterSyntax(Syntax.StringType());
	}
}
