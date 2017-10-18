package de.dfki.parking.model;

public final class DynamicPriceParking extends Parking {

  public DynamicPriceParking(Type type, String name, String address, double defaultPricePerHour, int numberOfParkingSpots) {
    super(type, name, address, defaultPricePerHour, numberOfParkingSpots);
  }

  @Override
  protected double getCurrentPrice() {
    return defaultPricePerHour;
  }
  
  public String toString() {
    return "[DynamicPriceParking " + name + "]";
  }
}
