package de.dfki.parking.model;

public final class FixedPriceParking extends Parking {

  public FixedPriceParking(Type type, String name, String address, double defaultPricePerHour, int numberOfParkingSpots) {
    super(type, name, address, defaultPricePerHour, numberOfParkingSpots);
  }

  @Override
  protected double getCurrentPrice() {
    return defaultPricePerHour;
  }

  public String toString() {
    return "[FixedPriceParking " + name + "]";
  }
}
