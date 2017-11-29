package de.dfki.parking.model;

import java.util.List;

import allow.simulator.world.Street;

public final class StreetParking extends Parking {

  private final List<Street> streets;
  
  public StreetParking(int id, String name, String address, double defaultPricePerHour, 
      int numberOfParkingSpots, List<Street> streets) {
    super(id, Type.STREET, name, address, defaultPricePerHour, numberOfParkingSpots);
    this.streets = streets;
  }

  public List<Street> getStreets() {
    return streets;
  }
  
  @Override
  public double getCurrentPricePerHour() {
    return defaultPricePerHour;
  }

  @Override
  public String toString() {
    return "[StreetParking " + name + "]";
  }
}
