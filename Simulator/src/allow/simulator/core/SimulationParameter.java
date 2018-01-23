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
  public double CMax;
  public double WdMax;
  public double StMax;
  public double CWeightHomemaker;
  public double WdWeightHomemaker;
  public double StWeightHomemaker;
  public double CWeightStudent;
  public double WdWeightStudent;
  public double StWeightStudent;
  public double CWeightWorker;
  public double WdWeightWorker;
  public double StWeightWorker;
}
