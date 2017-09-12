package allow.simulator.parking;

public final class FixedPriceParking extends Parking {

  public FixedPriceParking(String name, double defaultPricePerHour, int numberOfParkingSpots) {
    super(name, defaultPricePerHour, numberOfParkingSpots);
  }

  @Override
  protected double getCurrentPrice() {
    return defaultPricePerHour;
  }

  public String toString() {
    return "[FixedPriceParking " + name + "]";
  }
}
