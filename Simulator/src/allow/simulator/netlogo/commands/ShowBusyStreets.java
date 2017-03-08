package allow.simulator.netlogo.commands;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.nlogo.agent.Link;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;

import allow.simulator.netlogo.agent.NetLogoWrapper;
import allow.simulator.world.Street;
import allow.simulator.world.StreetMap;
import allow.simulator.world.StreetSegment;

public class ShowBusyStreets extends DefaultCommand {
	
	@Override
	public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {
		int runId = args[0].getIntValue();
		int maxNumber = args[1].getIntValue();
		
		NetLogoWrapper wrapper = NetLogoWrapper.Instance(runId);
		StreetMap map = (StreetMap) wrapper.getSimulator().getContext().getWorld();
		PriorityQueue<Street> sorted = new PriorityQueue<Street>(new Comparator<Street>() {

			@Override
			public int compare(Street o1, Street o2) {
				return o2.getUsageStatistics()[0] - o1.getUsageStatistics()[0];
			}
		});
		//sorted.addAll(wrapper.getSimulator().getStreetsInROI());
		sorted.addAll(wrapper.getSimulator().getStreetsInROI());
		
		// Reset NetLogo link
		resetLinks(wrapper.getLinkMapping());
		
		// Highlight busy streets
		highlightBusyStreets(sorted, maxNumber, wrapper.getLinkMapping());
		
		for (int i = 0; i < Math.min(maxNumber, sorted.size()); i++) {
			Street s = sorted.poll();
			int[] usage = s.getUsageStatistics();
			System.out.println(s.getName() + ";" + usage[0] + ";" + s.getStartingNode().getLabel() + ";" + s.getEndNode().getLabel());
		}
		System.out.println();
		
	}
	
	private void resetLinks(Map<String, Link> linkMapping) {
		
		for (Link l : linkMapping.values()) {
			l.colorDouble(NetLogoWrapper.LINK_COLOR_DEFAULT);
		}
	}
	
	private void highlightBusyStreets(PriorityQueue<Street> streets, int maxNumber, Map<String, Link> linkMapping) {
		int number = Math.min(maxNumber, streets.size());
		int i = 0;
		
		for (Street street : streets) {
			List<StreetSegment> segs = street.getSubSegments();
			
			for (StreetSegment seg : segs) {
				// Get corresponding link
				Link l = linkMapping.get(seg.getStartingNode().getId() + "," + seg.getEndingNode().getId());
				l.colorDouble(NetLogoWrapper.LINK_COLOR_BUSY);
				
				// Link l2 = linkMapping.get(seg.getEndingNode().getId() + "," + seg.getStartingNode().getId());
				//l2.colorDouble(NetLogoWrapper.LINK_COLOR_BUSY);
			}
			i++;
			
			if (i > number)
				break;
		}
	}
	
	public Syntax getSyntax() {
		int right[] = new int[] { Syntax.NumberType(), Syntax.NumberType() };
		return Syntax.commandSyntax(right);
	}
}
