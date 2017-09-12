package allow.simulator.parking;

public final class DynamicPriceParking extends Parking {

  public DynamicPriceParking(String name, double defaultPricePerHour, int numberOfParkingSpots) {
    super(name, defaultPricePerHour, numberOfParkingSpots);
  }

  @Override
  protected double getCurrentPrice() {
    return defaultPricePerHour;
  }
  
  public String toString() {
    return "[DynamicPriceParking " + name + "]";
  }
}
