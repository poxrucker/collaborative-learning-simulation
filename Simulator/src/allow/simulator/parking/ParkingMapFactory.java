package allow.simulator.parking;


public final class ParkingMapFactory {
  // Geographic bounds of the ParkingMap
  private final double[] bounds;
  
  // Number of grid rows
  private final int nRows;
  
  // Number of grid columns
  private final int nCols;
  
  public ParkingMapFactory(double[] bounds, int nRows, int nCols) {
    this.bounds = bounds;
    this.nRows = nRows;
    this.nCols = nCols;
  }
  
  public ParkingMap createParkingMap() {
    return new ParkingMap(bounds, nRows, nCols);
  }
}
