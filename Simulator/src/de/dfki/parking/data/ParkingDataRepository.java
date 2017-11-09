package de.dfki.parking.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class ParkingDataRepository {  
  // Mapping of address to street parking possibilities
  private final Map<String, List<ParkingData>> streetParkingData;
  
  // Mapping of address to garage parking possibilities
  private final Map<String, List<ParkingData>> garageParkingData;
  
  private ParkingDataRepository(Map<String, List<ParkingData>> streetParking, Map<String, List<ParkingData>> garageParking) {
    this.streetParkingData = streetParking;
    this.garageParkingData = garageParking;
  }
  
  public Map<String, List<ParkingData>> getStreetParkingData() {
    return Collections.unmodifiableMap(streetParkingData);
  }
  
  public Map<String, List<ParkingData>> getGarageParkingData() {
    return Collections.unmodifiableMap(garageParkingData);
  }
  
  public static ParkingDataRepository load(Path streetParkingFqfn, Path garageParkingFqfn) throws IOException {
    // Load fixed price street parking
    System.out.print("Loading street parking...");
    Map<String, List<ParkingData>> streetParking = new Object2ObjectOpenHashMap<>();  
    int nStreetParking = loadStreetParking(streetParkingFqfn, streetParking);
    System.out.println("found " + nStreetParking);
    
    for (String key : streetParking.keySet()) {
      System.out.println("  " + key + ": " + streetParking.get(key));
    }
    // Load dynamic price street parking
    System.out.print("Loading garage parking...");
    Map<String, List<ParkingData>> garageParking = new Object2ObjectOpenHashMap<>();  
    int nGarageParking = loadGarageParking(garageParkingFqfn, garageParking);
    System.out.println("found " + nGarageParking);
    
    for (String key : garageParking.keySet()) {
      System.out.println("  " + key + ": " + garageParking.get(key));
    }
    return new ParkingDataRepository(streetParking, garageParking);
  }

  private static int loadStreetParking(Path fqfn, Map<String, List<ParkingData>> buffer) throws NumberFormatException, IOException {
    int parsed = 0;
    
    try (BufferedReader reader = Files.newBufferedReader(fqfn)) {
      String line = null;
      
      while ((line = reader.readLine()) != null) {
        // Parse line
        ParkingData parking = parseStreetParking(line);
        
        // Add to list
        List<ParkingData> temp = buffer.get(parking.address);

        if (temp == null) {
          temp = new ObjectArrayList<ParkingData>();
          buffer.put(parking.address, temp);
        }
        temp.add(parking);
        parsed++;
        }
      }
    return parsed;
  }
  
  private static int loadGarageParking(Path fqfn, Map<String, List<ParkingData>> parkings) throws NumberFormatException, IOException {
    int parsed = 0;
    
    try (BufferedReader reader = Files.newBufferedReader(fqfn)) {
      String line = null;

      while ((line = reader.readLine()) != null) {
        // Parse line
        ParkingData parking = parseGarageParking(line);
        
        // Add to list
        List<ParkingData> temp = parkings.get(parking.address);

        if (temp == null) {
          temp = new ObjectArrayList<>();
          parkings.put(parking.address, temp);
        }
        temp.add(parking);
        parsed++;
      }
    }
    return parsed;
  }
  
  private static ParkingData parseStreetParking(String str) {
    String[] tokens = str.split(";");
    String name = tokens[0];
    name = name.substring(0, 1).toUpperCase() + name.substring(1);
    int numberOfParkingSpots = !tokens[1].equals("") ? Integer.parseInt(tokens[1]) : 0;
    double pricePerHour = !tokens[2].equals("") ? Double.parseDouble(tokens[2].replaceAll(",", ".")) : 0;
    return new ParkingData(name, name, pricePerHour, numberOfParkingSpots);
  }
  
  private static ParkingData parseGarageParking(String str) {
    String[] tokens = str.split(";");
    String name = tokens[0];
    name = name.substring(0, 1).toUpperCase() + name.substring(1);
    String address = tokens[1].substring(0, 1).toUpperCase() + tokens[1].substring(1);
    int numberOfParkingSpots = !tokens[2].equals("") ? Integer.parseInt(tokens[2]) : 0;
    double pricePerHour = !tokens[3].equals("") ? Double.parseDouble(tokens[3].replaceAll(",", ".")) : 0;
    return new ParkingData(name, address, pricePerHour, numberOfParkingSpots);
  }
}
