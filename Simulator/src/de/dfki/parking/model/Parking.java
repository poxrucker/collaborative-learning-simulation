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
  
  private static int ids = 0;
  
  private static int nextId() {
    int id = ids;
    ids++;
    return id;
  }
  
  // Unique id of parking
  protected final int id;
  
  // Type of parking
  protected final Type type;
  
  // Name of the parking (e.g. street name, garage...)
  protected final String name;
  
  // Address of the parking
  protected final String address;
  
  // Default price per hour, can be dynamically changed depending on e.g. the
  // number of cars currently parking) 
  protected final double defaultPricePerHour;
  
  // Total number of available parking spots
  protected final int numberOfParkingSpots;
  
  // Currently parking cars
  protected final Set<Person> parkingCars;
  
  public Parking(Type type, String name, String address, double defaultPricePerHour, int numberOfParkingSpots) {
    this.id = nextId();
    this.type = type;
    this.name = name;
    this.address = address;
    this.defaultPricePerHour = defaultPricePerHour;
    this.numberOfParkingSpots = numberOfParkingSpots;
    parkingCars = new HashSet<>(numberOfParkingSpots);
  }
  
  public final int getId() {
    return id;
  }
  
  public Type getType() {
    return type;
  }
  
  public final String getName() {
    return name;
  }
  
  public final String getAddress() {
    return address;
  }
  
  public final double getDefaultPricePerHour() {
    return defaultPricePerHour;
  }
  
  public double getCurrentPricePerHour() {
    return getCurrentPrice();
  }
  
  public final int getNumberOfParkingSpots() {
    return numberOfParkingSpots;
  }
  
  public final int getNumberOfFreeParkingSpots() {
    return numberOfParkingSpots - parkingCars.size();
  }
  
  public boolean hasFreeParkingSpot() {
    return getNumberOfFreeParkingSpots() > 0;
  }
  
  public final boolean park(Person person) {
    
    if (getNumberOfFreeParkingSpots() == 0)
        return false;
    
    parkingCars.add(person);
    return true;
  }
  
  public final void leave(Person person) {
    parkingCars.remove(person);
  }
  
  public String toString() {
    return name;
  }
  
  protected abstract double getCurrentPrice();
  
}
