package de.dfki.parking.model;

import java.util.List;

import allow.simulator.world.StreetNode;

public final class ParkingFactory {
  // Id counter providing locally unique ids to newly created Parking instances
  private int ids;
  
  // Scaling factor to adjust the number of parking spots of newly created Parking instances
  private final double parkingSpotScalingFactor;
  
  // Offset added to the price of newly created Parking instances
  private final double pricePerHourOffset;
  
  /**
   * Creates a new ParkingFactory instance scaling a given number of parking spots
   * by the given scaling factor and adapting the price per hour by the given offset.
   * 
   * @param parkingSpotScalingFactor Factor to scale the number of parking spots
   * @param pricePerHourOffset Offset to add to default price per hour
   */
  public ParkingFactory(double parkingSpotScalingFactor, double pricePerHourOffset) {
    this.parkingSpotScalingFactor = parkingSpotScalingFactor;
    this.pricePerHourOffset = pricePerHourOffset;
  }
  
  /**
   * Creates a new Parking associated with a given list of Street instances.
   * nParkingSpots is scaled by the scaling factor specified during construction.
   * pricePerHour is adjusted by the offset specified during construction.
   * 
   * @param name Name of the parking
   * @param pricePerHour Price per hour
   * @param nParkingSpots Number of parking spots
   * @param nodes List of StreetNode instances
   * @return Parking instance
   */
  public Parking createStreetParking(String name, double pricePerHour, int nParkingSpots, List<StreetNode> nodes) {
    double adjustedPricePerHour = adjust(pricePerHour, pricePerHourOffset);
    int scaledNParkingSpots = (int) Math.ceil(scale(nParkingSpots, parkingSpotScalingFactor));
    return new StreetParking(ids++, name, name, adjustedPricePerHour, scaledNParkingSpots, nodes);
  }
  
  /**
   * Creates a new Parking associated with a given list of StreetNode instances.
   * nParkingSpots is scaled by the scaling factor specified during construction.
   * pricePerHour is adjusted by the offset specified during construction.
   * 
   * @param name Name of the parking
   * @param pricePerHour Price per hour
   * @param nParkingSpots Number of parking spots
   * @param nodes List of StreetNode instances
   * @return Parking instance
   */
  public Parking createGarageParking(String name, double pricePerHour, int nParkingSpots, List<StreetNode> nodes) {
    double adjustedPricePerHour = adjust(pricePerHour, pricePerHourOffset);
    int scaledNParkingSpots = (int) Math.ceil(scale(nParkingSpots, parkingSpotScalingFactor));
    return new GarageParking(ids++, name, name, adjustedPricePerHour, scaledNParkingSpots, nodes);
  }
  
  private static double scale(double input, double factor) {
    return input * factor;    
  }
  
  private static double adjust(double input, double offset) {
    return input + offset;
  }
}
