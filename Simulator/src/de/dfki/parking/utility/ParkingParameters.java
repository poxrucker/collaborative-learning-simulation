package de.dfki.parking.utility;


public final class ParkingParameters {
  // Costs
  private final double c;
  
  // Walking distance
  private final double wd;
  
  // Search time
  private final double st;
  
  public ParkingParameters(double c, double wd, double st) {
   this.c = c;
   this.wd = wd;
   this.st = st;
  }
    
  /**
   * Returns the costs for parking.
   * 
   * @return Costs for parking
   */
  public double getC() {
    return c;
  }
  
  /**
   * Returns the walking distance from parking spot to destination.
   * 
   * @return Walking distance to destination
   */
  public double getWd() {
    return wd;
  }
  
  /**
   * Returns the search time for parking spot
   * 
   * @return Search time for parking
   */
  public double getSt() {
    return st;
  }
}