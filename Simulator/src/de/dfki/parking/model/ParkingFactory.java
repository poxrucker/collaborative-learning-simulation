package de.dfki.parking.model;

import de.dfki.parking.model.Parking.Type;

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
  
  public Parking createStreetParking(String street, double pricePerHour, int nParkingSpots) {
    double adjustedPricePerHour = adjust(pricePerHour, pricePerHourOffset);
    int scaledNParkingSpots = (int) Math.ceil(scale(nParkingSpots, parkingSpotScalingFactor));
    return new FixedPriceParking(ids++, Type.STREET, street, street, adjustedPricePerHour, scaledNParkingSpots);
  }
  
  public Parking createGarageParking(String street, double pricePerHour, int nParkingSpots) {
    double adjustedPricePerHour = adjust(pricePerHour, pricePerHourOffset);
    int scaledNParkingSpots = (int) Math.ceil(scale(nParkingSpots, parkingSpotScalingFactor));
    return new DynamicPriceParking(ids++, Type.GARAGE, street, street, adjustedPricePerHour, scaledNParkingSpots);
  }
  
  private static double scale(double input, double factor) {
    return input * factor;    
  }
  
  private static double adjust(double input, double offset) {
    return input + offset;
  }
}
