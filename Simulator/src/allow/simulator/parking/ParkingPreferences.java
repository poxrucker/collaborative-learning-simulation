package allow.simulator.parking;


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
    this.setCweight(cweight);
    this.setWdweight(wdweight);
    this.setStweight(stweight);
    this.setCmax(cmax);
    this.setWdmax(wdmax);
    this.setStmax(stmax);
  }

  public double getCweight() {
    return cweight;
  }

  public void setCweight(double cweight) {
    this.cweight = cweight;
  }

  public double getWdweight() {
    return wdweight;
  }

  public void setWdweight(double wdweight) {
    this.wdweight = wdweight;
  }

  public double getStweight() {
    return stweight;
  }

  public void setStweight(double stweight) {
    this.stweight = stweight;
  }

  public double getCmax() {
    return cmax;
  }

  public void setCmax(double cmax) {
    this.cmax = cmax;
  }

  public double getWdmax() {
    return wdmax;
  }

  public void setWdmax(double wdmax) {
    this.wdmax = wdmax;
  }

  public double getStmax() {
    return stmax;
  }

  public void setStmax(double stmax) {
    this.stmax = stmax;
  }
  
}
