package allow.simulator.parking;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import allow.simulator.world.Street;
import allow.simulator.world.StreetMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class ParkingMap {

  private final StreetMap streetMap;
  private final Map<String, List<Parking>> parkingMap;

  public ParkingMap(StreetMap streetMap, Map<String, List<Parking>> parkingMap) {
    this.streetMap = streetMap;
    this.parkingMap = parkingMap;
  }

  public static ParkingMap load(StreetMap streetMap, Path streetParking, Path garageParking) throws IOException {
    // Destination to store loaded parking
    Map<String, List<Parking>> parkings = new Object2ObjectOpenHashMap<>();

    // Load fixed price street parking
    System.out.println("Loading street parking...");
    loadStreetParking(streetParking, streetMap.getStreetsByName(), parkings);
    
    // Load dynamic price street parking
    System.out.println("Loading garage parking...");
    loadGarageParking(garageParking, streetMap.getStreetsByName(), parkings);
    
    for (String name : parkings.keySet()) {
      System.out.println(name + " -> " + parkings.get(name));
    }
    return new ParkingMap(streetMap, parkings);
  }

  private static void loadStreetParking(Path streetParking, Map<String, List<Street>> streets, Map<String, List<Parking>> parkings) throws NumberFormatException, IOException {
   
    try (BufferedReader reader = Files.newBufferedReader(streetParking)) {
      // Headline
      String line = reader.readLine();

      while ((line = reader.readLine()) != null) {
        String[] tokens = line.split(";");
        String name = tokens[0];
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        int numberOfParkingSpots = !tokens[1].equals("") ? Integer.parseInt(tokens[1]) : 0;
        double pricePerHour = !tokens[2].equals("") ? Double.parseDouble(tokens[2].replaceAll(",", ".")) : 0;

        List<Street> byName = streets.get(name);
        
        if (byName == null) {
          System.out.println(name);
         
        } else {
          
          List<Parking> temp = parkings.get(name);
          
          if (temp == null) {
            temp = new ObjectArrayList<>();
            parkings.put(name, temp);
          }
          temp.add(new FixedPriceParking(name, pricePerHour, numberOfParkingSpots));
        }
      }
    }
  }
  
  private static void loadGarageParking(Path garageParking, Map<String, List<Street>> streets, Map<String, List<Parking>> parkings) throws NumberFormatException, IOException {
    
    try (BufferedReader reader = Files.newBufferedReader(garageParking)) {
      // Headline
      String line = reader.readLine();

      while ((line = reader.readLine()) != null) {
        String[] tokens = line.split(";");
        String name = tokens[0];
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        String address = tokens[1];
        address = address.substring(0, 1).toUpperCase() + address.substring(1);

        int numberOfParkingSpots = !tokens[2].equals("") ? Integer.parseInt(tokens[2]) : 0;
        double pricePerHour = !tokens[3].equals("") ? Double.parseDouble(tokens[3].replaceAll(",", ".")) : 0;

        List<Street> byName = streets.get(address);
        
        if (byName == null) {
          System.out.println(address);
         
        } else {
          
          List<Parking> temp = parkings.get(address);
          
          if (temp == null) {
            temp = new ObjectArrayList<>();
            parkings.put(address, temp);
          }
          temp.add(new DynamicPriceParking(name, pricePerHour, numberOfParkingSpots));
        }
      }
    }
  }

}
