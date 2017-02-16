package allow.simulator.utility;

import java.util.Comparator;
import java.util.List;

import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.mobility.planner.TType;

public final class JourneyRankingFunction implements IDecisionFunction<List<Itinerary>, List<Itinerary>>{

	private final Preferences preferences;
	private final IUtility<ItineraryParams, Preferences> utility;
	
	private static Comparator<Itinerary> ascUtility = new Comparator<Itinerary>() {
        @Override
        public int compare(Itinerary rt1, Itinerary rt2) {
        	double diff = rt1.utility - rt2.utility;
        	
        	if (diff < 0) {
        		return -1;
        		
        	} else if (diff == 0) {
        		return 0;
        		
        	} else {
        		return 1;
        	}
        }
	};
	
	private static Comparator<Itinerary> descUtility = new Comparator<Itinerary>() {
        @Override
        public int compare(Itinerary rt1, Itinerary rt2) {
            return -ascUtility.compare(rt1, rt2);
        }
	};
	
	public JourneyRankingFunction(Preferences preferences, IUtility<ItineraryParams, Preferences> utility) {
		this.preferences = preferences;
		this.utility = utility;
	}
	
	@Override
	public List<Itinerary> reason(List<Itinerary> itineraries) {
		
		for (Itinerary it : itineraries) {
			ItineraryParams params = new ItineraryParams(it.itineraryType, (it.duration /*+ it.initialWaitingTime*/),
					it.costs, it.maxFillingLevel, it.walkDistance, it.transfers, 0.0);
			it.utility = utility.computeUtility(params, preferences);
			double pref = 0.5;
			
			if (it.itineraryType == TType.CAR || it.itineraryType == TType.TAXI || it.itineraryType == TType.SHARED_TAXI) {
				pref = preferences.getCarPreference();
				
			} else if (it.itineraryType == TType.BUS) {
				pref = preferences.getBusPreference();	
			}
			pref = 1 - (pref - 0.5);
			it.utility = it.utility * pref;
		}
		
		if (utility.ascendingOrder()) {
			itineraries.sort(descUtility);
			
		} else {
			itineraries.sort(ascUtility);
		}
		return itineraries;
	}

	public Preferences getPreferences() {
		return preferences;
	}
	
	public IUtility<ItineraryParams, Preferences> getUtilityFunction() {
		return utility;
	}
}
