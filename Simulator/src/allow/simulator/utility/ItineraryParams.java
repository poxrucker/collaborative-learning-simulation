package allow.simulator.utility;

import allow.simulator.mobility.planner.TType;

public class ItineraryParams {

	public TType type;
	public long travelTime;
	public double costs;
	public double maxBusFillingLevel;
	public double walkingDistance;
	public int numberOfTransfers;
	
	public ItineraryParams(TType type, long travelTime, double costs, double maxBusFillingLevel,
			double walkingDistance, int numberOfTransfers) {
		this.type = type;
		this.travelTime = travelTime;
		this.costs = costs;
		this.maxBusFillingLevel = maxBusFillingLevel;
		this.walkingDistance = walkingDistance;
		this.numberOfTransfers = numberOfTransfers;
	}
}
