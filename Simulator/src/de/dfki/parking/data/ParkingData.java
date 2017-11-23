package de.dfki.parking.data;

import java.util.List;

public final class ParkingData {
  // Name of the parking (e.g. street name, garage...)
  private final String name;
  
  // Address of the parking
  private final String address;
  
  // Price per hour
  private final double pricePerHour;
  
  // Number of available parking spots
  private final int nParkingSpots;
  
  // List of associated OSM nodes
  private final List<String> osmNodes;
  
  public ParkingData(String name, String address, double pricePerHour, int nParkingSpots, List<String> osmNodes) {
    this.name = name;
    this.address = address;
    this.pricePerHour = pricePerHour;
    this.nParkingSpots = nParkingSpots;
    this.osmNodes = osmNodes;
  }
  
  /**
   * Returns the name of the parking possibility.
   * 
   * @return Name of the parking possibility
   */
  public String getName() {
    return name;
  }
  
  /**
   * Returns the address of the parking possibility.
   * 
   * @return Address of the parking possibility
   */
  public String getAddress() {
    return address;
  }
  
  /**
   * Returns the price per hour of the parking possibility.
   * 
   * @return Price per hour of the parking possibility
   */
  public double getPricePerHour() {
    return pricePerHour;
  }

  /**
   * Returns the number of parking spots of the parking possibility.
   * 
   * @return Number of parking spots of the parking possibility
   */
  public int getNumberOfParkingSpots() {
    return nParkingSpots;
  }
  
  /**
   * Returns the OSM node associated with the parking possibility.
   * 
   * @return OSM nodes associated with the parking possibility
   */
  public List<String> getOSMNodes() {
    return osmNodes;
  }
}