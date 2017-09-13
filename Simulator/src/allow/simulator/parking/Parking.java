package allow.simulator.parking;

import java.util.HashSet;
import java.util.Set;

import allow.simulator.entity.Person;

public abstract class Parking {
  // Name of the parking (e.g. street name, garage...)
  protected final String name;
  
  // Default price per hour, can be dynamically changed depending on e.g. the
  // number of cars currently parking) 
  protected final double defaultPricePerHour;
  
  // Total number of available parking spots
  protected final int numberOfParkingSpots;
  
  // Currently parking cars
  protected final Set<Person> parkingCars;
  
  public Parking(String name, double defaultPricePerHour, int numberOfParkingSpots) {
    this.name = name;
    this.defaultPricePerHour = defaultPricePerHour;
    this.numberOfParkingSpots = numberOfParkingSpots;
    parkingCars = new HashSet<>(numberOfParkingSpots);
  }
  
  public final String getName() {
    return name;
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
