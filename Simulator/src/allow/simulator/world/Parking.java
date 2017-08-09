package allow.simulator.world;

import java.util.HashSet;
import java.util.Set;

import allow.simulator.entity.Person;
import allow.simulator.util.Coordinate;

public final class Parking {
  private final String name;
  private final Coordinate position;
  private final double pricePerHour;
  private final int maxCapacity;
  private final Set<Person> cars;
  
  public Parking(String name, Coordinate position,
      double pricePerHour, int maxCapacity) {
    this.name = name;
    this.position = position;
    this.pricePerHour = pricePerHour;
    this.maxCapacity = maxCapacity;
    cars = new HashSet<>(maxCapacity);
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
  
  public boolean park(Person person) {
    
    if (cars.size() == maxCapacity)
        return false;
    cars.add(person);
    return true;
  }
  
  public void leave(Person person) {
    cars.remove(person);
  }
}
