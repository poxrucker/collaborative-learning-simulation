package de.dfki.parking.data;

public final class ParkingData {
  // Name of the parking (e.g. street name, garage...)
  protected final String name;
  
  // Address of the parking
  protected final String address;
  
  // Price per hour
  protected final double pricePerHour;
  
  // Number of available parking spots
  protected final int nParkingSpots;
  
  public ParkingData(String name, String address, double pricePerHour, int nParkingSpots) {
    this.name = name;
    this.address = address;
    this.pricePerHour = pricePerHour;
    this.nParkingSpots = nParkingSpots;
  }
  
  public String getName() {
    return name;
  }
  
  public String getAddress() {
    return address;
  }
  
  public double getPricePerHour() {
    return pricePerHour;
  }

  public int getNumberOfParkingSpots() {
    return nParkingSpots;
  }
   
  public String toString() {
    return name;
  }  
}
