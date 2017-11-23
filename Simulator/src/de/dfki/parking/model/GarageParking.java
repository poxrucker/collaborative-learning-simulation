package de.dfki.parking.model;

import java.util.List;

import allow.simulator.world.StreetNode;

public final class GarageParking extends Parking {
  // StreetNodes the GarageParking can be accessed by
  private final List<StreetNode> accessNodes;
  
  public GarageParking(int id, String name, String address, double defaultPricePerHour, 
      int numberOfParkingSpots, List<StreetNode> accessNodes) {
    super(id, Type.GARAGE, name, address, defaultPricePerHour, numberOfParkingSpots);
    this.accessNodes = accessNodes;
  }

  public List<StreetNode> getAccessNodes() {
    return accessNodes;
  }
  
  @Override
  public double getCurrentPricePerHour() {
    return defaultPricePerHour;
  }
  
  @Override
  public String toString() {
    return "[GarageParking " + name + "]";
  }
}
