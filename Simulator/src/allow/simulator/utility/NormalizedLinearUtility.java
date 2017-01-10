package allow.simulator.utility;

import allow.simulator.mobility.planner.TType;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 
 * @author UOC
 *
 */
public class NormalizedLinearUtility implements IUtility<ItineraryParams, Preferences> {

	private static final double TFACTOR = 1.0 / 1500.0;
	private static final double CFACTOR = 1.0 / 2.5;
	private static final double FFACTOR = 1.0;
	private static final double WFACTOR = 1.0 / 1000.0;
	
	@JsonCreator
	public NormalizedLinearUtility() { }

	@Override
	public double computeUtility(ItineraryParams input, Preferences preferences) {
		final double ut = TFACTOR * input.travelTime; // (input.duration + input.initialWaitingTime);
		final double uc = CFACTOR * input.costs;
		final double uf = ((input.maxBusFillingLevel < 0.7) ? 0.0 : input.maxBusFillingLevel * FFACTOR);
		final double uw = (input.walkingDistance < 500) ? 0.0 : input.walkingDistance * WFACTOR;
		double pref = 0.5;
	
		if (input.type == TType.CAR || input.type == TType.TAXI || input.type == TType.SHARED_TAXI) {
			pref = preferences.getCarPreference();
			
		} else if (input.type == TType.BUS) {
			pref = preferences.getBusPreference();	
		}
		pref = 1 - (pref - 0.5);		
		return pref * (ut + uc + uf + uw);
	}

	@Override
	public boolean ascendingOrder() {
		return false;
	}

}
