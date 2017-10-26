package allow.simulator.core;

public final class SimulationParameter {
  
  // General simulation parameters
	public int BehaviourSpaceRunNumber;
	public int GridResX;
	public int GridResY;  
	  
	// Coverage simulation parameters
	public String Scenario;
	public int PercentParticipating;
	public int MaximumVisitedTime;
	
	// Parking spot simulation parameters
	public String Model;
	public String StreetParkingPath;
	public String GarageParkingPath;
	public int PercentUsers;
	public int PercentSensorCars;
	public int ValidTime;
	public double DataScalingFactor;
}
