package allow.simulator.world;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class ParkingMap {

  private final StreetMap streetMap;
  private final Map<String, Parking> parkingMap;
  
  public ParkingMap(StreetMap streetMap, Map<String, Parking> parkingMap) {
    this.streetMap = streetMap;
    this.parkingMap = parkingMap;
  }
  
  public static ParkingMap load(StreetMap streetMap, Path path) throws IOException {
    // Read file line by line
    Map<String, Parking> parkingMap = new Object2ObjectOpenHashMap<>();
    int total = 0;
    int matched = 0;
    
    try (BufferedReader reader = Files.newBufferedReader(path)) {
      // Headline
      String line = reader.readLine();
      
      while ((line = reader.readLine()) != null) {
        String[] tokens = line.split(";");
        String name = tokens[0];
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        int numberOfParkingSpots = !tokens[1].equals("") ? Integer.parseInt(tokens[1]) : 0;
        double pricePerHour = !tokens[2].equals("") ? Double.parseDouble(tokens[2].replaceAll(",", ".")) : 0;
        
        List<Street> byName = streetMap.getStreetsByName(name);
        
        if (byName == null) {
          System.out.println(name);
          
        } else {
          matched++;
        }
        total++;
      }
    }
    System.out.println("Matched " + matched + " of " + total + " street parkings");
    
    return new ParkingMap(streetMap, parkingMap);
  }
 }
