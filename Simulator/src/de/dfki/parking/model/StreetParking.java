package de.dfki.parking.model;

import java.util.List;

import allow.simulator.world.StreetNode;

public final class StreetParking extends Parking {

  private final List<StreetNode> nodes;
  
  public StreetParking(int id, String name, String address, double defaultPricePerHour, 
      int numberOfParkingSpots, List<StreetNode> nodes) {
    super(id, Type.STREET, name, address, defaultPricePerHour, numberOfParkingSpots);
    this.nodes = nodes;
  }

  public List<StreetNode> getNodes() {
    return nodes;
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
