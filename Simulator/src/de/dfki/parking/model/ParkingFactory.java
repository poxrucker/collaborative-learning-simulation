package de.dfki.parking.model;

import java.util.List;

import allow.simulator.world.Street;
import allow.simulator.world.StreetNode;

public final class ParkingFactory {
  // Id counter providing locally unique ids to newly created Parking instances
  private int ids;
  
  // Scaling factor to adjust the number of parking spots of newly created Parking instances
  private final double parkingSpotScalingFactor;
  
  // Offset added to the price of newly created Parking instances
  private final double pricePerHourOffset;
  
  public ParkingFactory(double parkingSpotScalingFactor, double pricePerHourOffset) {
    this.parkingSpotScalingFactor = parkingSpotScalingFactor;
    this.pricePerHourOffset = pricePerHourOffset;
  }
  
  public Parking createStreetParking(String street, double pricePerHour, int nParkingSpots, List<Street> streets) {
    double adjustedPricePerHour = adjust(pricePerHour, pricePerHourOffset);
    int scaledNParkingSpots = (int) Math.ceil(scale(nParkingSpots, parkingSpotScalingFactor));
    return new StreetParking(ids++, street, street, adjustedPricePerHour, scaledNParkingSpots, streets);
  }
  
  public Parking createGarageParking(String street, double pricePerHour, int nParkingSpots, List<StreetNode> accessNodes) {
    double adjustedPricePerHour = adjust(pricePerHour, pricePerHourOffset);
    int scaledNParkingSpots = (int) Math.ceil(scale(nParkingSpots, parkingSpotScalingFactor));
    return new GarageParking(ids++, street, street, adjustedPricePerHour, scaledNParkingSpots, accessNodes);
  }
  
  private static double scale(double input, double factor) {
    return input * factor;    
  }
  
  private static double adjust(double input, double offset) {
    return input + offset;
  }
}
