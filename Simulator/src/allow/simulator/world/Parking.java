package allow.simulator.world;

import java.util.HashSet;
import java.util.Set;

import allow.simulator.entity.Person;
import allow.simulator.util.Coordinate;

public final class Parking {
  // Name of the parking (e.g. street name, garage...)
  private final String name;
  
  // Position
  private final Coordinate position;
  
  // Price per hour
  private final double pricePerHour;
  
  // Total number of available parking spots
  private final int numberOfParkingSpots;
  
  // Currently parking cars
  private final Set<Person> parkingCars;
  
  public Parking(String name, Coordinate position,
      double pricePerHour, int numberOfParkingSpots) {
    this.name = name;
    this.position = position;
    this.pricePerHour = pricePerHour;
    this.numberOfParkingSpots = numberOfParkingSpots;
    parkingCars = new HashSet<>(numberOfParkingSpots);
  }
  
  public String getName() {
    return name;
  }
  
  public Coordinate getPosition() {
    return position;
  }
  
  public double getPricePerHour() {
    return pricePerHour;
  }
  
  public int getNumberOfParkingSpots() {
    return numberOfParkingSpots;
  }
  
  public int getNumberOfFreeParkingSpots() {
    return (numberOfParkingSpots - parkingCars.size());
  }
  
  public boolean park(Person person) {
    
    if (getNumberOfFreeParkingSpots() == 0)
        return false;
    
    parkingCars.add(person);
    return true;
  }
  
  public void leave(Person person) {
    parkingCars.remove(person);
  }
}
