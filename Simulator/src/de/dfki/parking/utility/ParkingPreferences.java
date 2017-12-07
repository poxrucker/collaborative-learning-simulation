package de.dfki.parking.utility;


public final class ParkingPreferences {
  // Weight for parking cost
  private double cweight;
  
  // Weight for walking distance
  private double wdweight;
  
  // Weight for search time
  private double stweight;
  
  // Maximum acceptable cost for parking
  private double cmax;
  
  // Maximum acceptable walking distance from parking to final destination
  private double wdmax;
  
  // Maximum acceptable searching time for parking
  private double stmax;
  
  public ParkingPreferences(double cweight, double wdweight, double stweight, 
      double cmax, double wdmax, double stmax) {
    this.cweight = cweight;
    this.wdweight = wdweight;
    this.stweight = stweight;
    this.cmax = cmax;
    this.wdmax = wdmax;
    this.stmax = stmax;
  }

  /**
   * Returns the weight for costs.
   * 
   * @return Weight for costs
   */
  public double getCWeight() {
    return cweight;
  }

  /**
   * Sets the weight for costs.
   * 
   * @param cweight Weight for costs
   */
  public void setCWeight(double cweight) {
    this.cweight = cweight;
  }

  /**
   * Returns the weight for walking distance from parking spot to destination.
   * 
   * @return Weight for walking distance
   */
  public double getWdWeight() {
    return wdweight;
  }

  /**
   * Sets the weight for walking distance from parking spot to destination.
   * 
   * @param wdweight Weight for walking distance
   */
  public void setWdWeight(double wdweight) {
    this.wdweight = wdweight;
  }

  /**
   * Returns the weight for parking spot search time.
   * 
   * @return Weight for parking spot search time
   */
  public double getStWeight() {
    return stweight;
  }

  /**
   * Sets the weight for parking spot search time.
   * 
   * @param stweight Weight for search time
   */
  public void setStWeight(double stweight) {
    this.stweight = stweight;
  }

  /**
   * Returns the preferred maximum costs for parking.
   * 
   * @return Preferred maximum costs
   */
  public double getCMax() {
    return cmax;
  }

  /**
   * Sets the preferred maximum costs for parking.
   * 
   * @param cmax Preferred maximum costs
   */
  public void setCMax(double cmax) {
    this.cmax = cmax;
  }

  /**
   * Returns the preferred maximum walking distance from parking spot to destination.
   * 
   * @return Preferred maximum walking distance
   */
  public double getWdMax() {
    return wdmax;
  }

  /**
   * Sets the preferred maximum walking distance from parking spot to destination.
   * 
   * @param cmax Preferred maximum walking distance
   */
  public void setWdMax(double wdmax) {
    this.wdmax = wdmax;
  }

  /**
   * Returns the preferred maximum search time for parking spot.
   * 
   * @return Preferred maximum search time
   */
  public double getStMax() {
    return stmax;
  }

  /**
   * Sets the preferred maximum search time for parking spot.
   * 
   * @param cmax Preferred maximum search time
   */
  public void setStMax(double stmax) {
    this.stmax = stmax;
  } 
}