package allow.simulator.utility;

import allow.simulator.mobility.planner.TType;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 
 * @author UOC
 *
 */
public class ExponentialUtility implements IUtility<ItineraryParams, Preferences> {

	@JsonCreator
	public ExponentialUtility(Preferences preferences) { }

	@Override
	public double computeUtility(ItineraryParams input, Preferences preferences) {
		long diff = (long) (preferences.getTmax() - input.travelTime);
		double u = preferences.getTTweight() * (Math.exp(diff - Math.abs(diff)));
		u += preferences.getCweight() * (Math.exp(-input.costs / preferences.getCmax()));
		double pref = 0.5;
		
		if (input.type == TType.CAR || input.type == TType.TAXI || input.type == TType.SHARED_TAXI) {
			pref = preferences.getCarPreference();
			
		} else if (input.type == TType.BUS) {
			pref = preferences.getBusPreference();	
		}
		pref = 1 - (pref - 0.5);		
		return pref * Math.max(u, 0);
	}

	@Override
	public boolean ascendingOrder() {
		return true;
	}
}
