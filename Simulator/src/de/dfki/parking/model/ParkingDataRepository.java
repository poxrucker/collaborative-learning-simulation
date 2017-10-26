package de.dfki.parking.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import allow.simulator.world.Street;
import de.dfki.parking.model.Parking.Type;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class ParkingDataRepository {
  // Mapping of address to garage parking possibilities
  private final Map<String, List<Parking>> garageParkings;
  
  // Mapping of address to street parking possibilities
  private final Map<String, List<Parking>> streetParkings;
  
  // Total number of garage parking spots (calculated during initialization)
  private final int totalNumberOfGarageParkingSpots;
  
  // Total number of street parking spots (calculated during initialization)
  private final int totalNumberOfStreetParkingSpots;
  
  public ParkingDataRepository(Map<String, List<Parking>> streetParking, Map<String, List<Parking>> garageParking) {
    this.streetParkings = streetParking;
    this.garageParkings = garageParking;
    totalNumberOfGarageParkingSpots = countTotalNumberOfParkingSpots(garageParking);
    totalNumberOfStreetParkingSpots = countTotalNumberOfParkingSpots(streetParking);
  }

  public List<Parking> getStreetParking(Street street) {
    return Collections.unmodifiableList(streetParkings.get(street.getName()));
  }
  
  public List<Parking> getGarageParking(Street street) {
    return Collections.unmodifiableList(garageParkings.get(street.getName()));
  }
  
  public List<Parking> getParking(Street street) {
    List<Parking> garageParking = garageParkings.get(street.getName());
    List<Parking> streetParking = streetParkings.get(street.getName());
    
    List<Parking> ret = new ObjectArrayList<>();
    
    if (garageParking != null)
      ret.addAll(garageParking);
    
    if (streetParking != null)
      ret.addAll(streetParking);
    
    return ret;
  }
  
  public int getTotalNumberOfFreeStreetParkingSpots() {
    return countTotalNumberOfFreeParkingSpots(streetParkings);
  }
  
  public int getTotalNumberOfFreeGarageParkingSpots() {
    return countTotalNumberOfFreeParkingSpots(garageParkings);
  }
  
  public int getTotalNumberOfGarageParkingSpots() {
    return totalNumberOfGarageParkingSpots;
  }
  
  public int getTotalNumberOfStreetParkingSpots() {
    return totalNumberOfStreetParkingSpots;
  }
  
  private static int countTotalNumberOfParkingSpots(Map<String, List<Parking>> parkings) {
    int numberOfSpots = 0;
    
    for (List<Parking> parking : parkings.values()) {
      
      for (Parking p : parking) {
        numberOfSpots += p.getNumberOfParkingSpots();
      }
    }
    return numberOfSpots;
  }
  
  private static int countTotalNumberOfFreeParkingSpots(Map<String, List<Parking>> parkings) {
    int numberOfFreeSpots = 0;
    
    for (List<Parking> parking : parkings.values()) {
      
      for (Parking p : parking) {
        numberOfFreeSpots += p.getNumberOfFreeParkingSpots();
      }
    }
    return numberOfFreeSpots;
  }
  
  public static ParkingDataRepository load(Path streetParkingFqfn, Path garageParkingFqfn, double scalingFactor) throws IOException {
    // Load fixed price street parking
    System.out.print("Loading street parking...");
    Map<String, List<Parking>> streetParking = new Object2ObjectOpenHashMap<>();  
    int nStreetParkings = loadStreetParking(streetParkingFqfn, streetParking, scalingFactor);
    System.out.println("found " + nStreetParkings);
    
    for (String key : streetParking.keySet()) {
      System.out.println("  " + key + ": " + streetParking.get(key));
    }
    // Load dynamic price street parking
    System.out.print("Loading garage parking...");
    Map<String, List<Parking>> garageParking = new Object2ObjectOpenHashMap<>();  
    int nGarageParkings = loadGarageParking(garageParkingFqfn, garageParking, scalingFactor);
    System.out.println("found " + nGarageParkings);
    
    for (String key : garageParking.keySet()) {
      System.out.println("  " + key + ": " + garageParking.get(key));
    }
    return new ParkingDataRepository(streetParking, garageParking);
  }

  private static int loadStreetParking(Path streetParkingFqfn, Map<String, List<Parking>> buffer, double scalingFactor) throws NumberFormatException, IOException {
    int added = 0;
    
    try (BufferedReader reader = Files.newBufferedReader(streetParkingFqfn)) {
      String line = null;
      
      while ((line = reader.readLine()) != null) {
        // Parse parking
        Parking parking = parseFixedPriceParking(line, Type.STREET, scalingFactor);
        
        // Add to list
        List<Parking> temp = buffer.get(parking.address);

        if (temp == null) {
          temp = new ObjectArrayList<Parking>();
          buffer.put(parking.address, temp);
        }
        temp.add(parking);
        added++;
        }
      }
    return added;
  }
  
  private static int loadGarageParking(Path garageParkingFqfn, Map<String, List<Parking>> parkings, double scalingFactor) throws NumberFormatException, IOException {
    int added = 0;
    
    try (BufferedReader reader = Files.newBufferedReader(garageParkingFqfn)) {
      String line = null;

      while ((line = reader.readLine()) != null) {
        // Parse parking
        Parking parking = parseDynamicPriceParking(line, Type.GARAGE, scalingFactor);
        
        // Add to list
        List<Parking> temp = parkings.get(parking.address);

        if (temp == null) {
          temp = new ObjectArrayList<>();
          parkings.put(parking.address, temp);
        }
        temp.add(parking);
        added++;
      }
    }
    return added;
  }
  
  private static Parking parseFixedPriceParking(String str, Type type, double scalingFactor) {
    String[] tokens = str.split(";");
    String name = tokens[0];
    name = name.substring(0, 1).toUpperCase() + name.substring(1);
    int numberOfParkingSpots = !tokens[1].equals("") ? Integer.parseInt(tokens[1]) : 0;
    double pricePerHour = !tokens[2].equals("") ? Double.parseDouble(tokens[2].replaceAll(",", ".")) : 0;
    return new FixedPriceParking(type, name, name, pricePerHour, scale(numberOfParkingSpots, scalingFactor));
  }
  
  private static Parking parseDynamicPriceParking(String str, Type type, double scalingFactor) {
    String[] tokens = str.split(";");
    String name = tokens[0];
    name = name.substring(0, 1).toUpperCase() + name.substring(1);
    String address = tokens[1].substring(0, 1).toUpperCase() + tokens[1].substring(1);
    int numberOfParkingSpots = !tokens[2].equals("") ? Integer.parseInt(tokens[2]) : 0;
    double pricePerHour = !tokens[3].equals("") ? Double.parseDouble(tokens[3].replaceAll(",", ".")) : 0;
    return new DynamicPriceParking(type, name, address, pricePerHour, scale(numberOfParkingSpots, scalingFactor));
  }
  
  private static int scale(int input, double scalingFactor) {
    if (input == 0)
      return 0;
    
    return (int) Math.ceil(input * scalingFactor);    
  }
}
