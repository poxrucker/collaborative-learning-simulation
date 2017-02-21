package allow.simulator.utility;

import allow.simulator.mobility.planner.TType;

public final class ItineraryParams {
	
	public TType type;
	public long travelTime;
	public long travelTimeRaw;
	public double costs;
	public double maxBusFillingLevel;
	public double walkingDistance;
	public int numberOfTransfers;
	public double totalDistance;

	public ItineraryParams(TType type, long travelTime, long travelTimeRaw, double costs, 
			double maxBusFillingLevel,double walkingDistance, int numberOfTransfers, 
			double totalDistance) {
		this.type = type;
		this.travelTime = travelTime;
		this.travelTimeRaw = travelTimeRaw;
		this.costs = costs;
		this.maxBusFillingLevel = maxBusFillingLevel;
		this.walkingDistance = walkingDistance;
		this.numberOfTransfers = numberOfTransfers;
		this.totalDistance = totalDistance;
	}
}
