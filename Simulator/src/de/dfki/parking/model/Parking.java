package de.dfki.parking.model;

import java.util.HashSet;
import java.util.Set;

import allow.simulator.entity.Person;

public abstract class Parking {
  
  public enum Type {
    
    /**
     * Street parking
     */
    STREET,
    
    /**
     * Garage parking
     */
    GARAGE
    
  }
  // Unique id of parking
  protected final int id;
  
  // Type of parking
  protected final Type type;
  
  // Name of the parking (e.g. street name, garage...)
  protected final String name;
  
  // Address of the parking
  protected final String address;
  
  // Default price per hour, can be dynamically changed depending on e.g. the number of cars currently parking
  protected final double defaultPricePerHour;
  
  // Total number of available parking spots
  protected final int numberOfParkingSpots;
  
  // Currently parking cars
  protected final Set<Person> parkingCars;
  
  public Parking(int id, Type type, String name, String address, double defaultPricePerHour, int numberOfParkingSpots) {
    this.id = id;
    this.type = type;
    this.name = name;
    this.address = address;
    this.defaultPricePerHour = defaultPricePerHour;
    this.numberOfParkingSpots = numberOfParkingSpots;
    parkingCars = new HashSet<>(numberOfParkingSpots);
  }
  
  /**
   * Returns the id of the Parking instance.
   * 
   * @return Id of Parking instance
   */
  public final int getId() {
    return id;
  }
  
  /**
   * Returns the type of the Parking instance (one of Street or Garage).
   * 
   * @return Type of Parking instance
   */
  public Type getType() {
    return type;
  }
  
  /**
   * Returns the name of the Parking instance (e.g. street or garage name).
   * 
   * @return Name of the Parking instance
   */
  public final String getName() {
    return name;
  }
  
  /**
   * Returns the address of the Parking instance.
   * 
   * @return Address of the Parking instance
   */
  public final String getAddress() {
    return address;
  }
  
  /**
   * Returns the default price per hour of the Parking instance.
   * 
   * @return Default price per hour of the Parking instance
   */
  public final double getDefaultPricePerHour() {
    return defaultPricePerHour;
  }
  
  /**
   * Returns the current price per hour of the Parking instance.
   * May be different from default price per hour.
   * 
   * @return Current price per hour of the Parking instance
   */
  public abstract double getCurrentPricePerHour();
  
  /**
   * Returns the total number of parking spots of the Parking instance.
   * 
   * @return Total number of parking spots of the Parking instance
   */
  public final int getNumberOfParkingSpots() {
    return numberOfParkingSpots;
  }
  
  /**
   * Returns the number of free parking spots of the Parking instance.
   * 
   * @return Number of free parking spots of the Parking instance
   */
  public final int getNumberOfFreeParkingSpots() {
    return numberOfParkingSpots - parkingCars.size();
  }
  
  /**
   * Indicates if the Parking instance has free parking spots.
   * 
   * @return True if Parking instance has free parking spots, false otherwise.
   */
  public boolean hasFreeParkingSpot() {
    return getNumberOfFreeParkingSpots() > 0;
  }
  
  /**
   * Adds a Person to the set of entities currently parking at the Parking instance.
   * Reduces the number of available parking spots by one. If there is no free 
   * parking spot, false is returned.
   * 
   * @param person Person parking at the Parking instance
   * @return True if parking was successful, false otherwise
   */
  public final boolean park(Person person) {
    
    if (getNumberOfFreeParkingSpots() == 0)
        return false;
    
    parkingCars.add(person);
    return true;
  }
  
  /**
   * Removes a Person from the set of entities currently parking at the Parking instance.
   * Increases the number of available parking spots by one.
   * 
   * @param person Person to remove from the Parking instance
   * @return True if person is removed from the set of currently parking entities, false
   * if person is not parking at the Parking instance
   */
  public final boolean leave(Person person) {
    return parkingCars.remove(person);
  }
}