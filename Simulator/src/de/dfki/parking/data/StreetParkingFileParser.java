package de.dfki.parking.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class StreetParkingFileParser implements IParkingDataFileParser {

  @Override
  public List<ParkingData> parseFile(Path fqfn) throws IOException {
    List<ParkingData> ret = new ObjectArrayList<>();
    
    try (BufferedReader reader = Files.newBufferedReader(fqfn)) {
      String line = null;
      
      while ((line = reader.readLine()) != null) {
        // Parse line
        ParkingData parking = parseLine(line);
        
        if (parking.getNumberOfParkingSpots() > 0)
          System.out.println(parking.getName() + ": " + parking.getNumberOfParkingSpots());
        // Add to list
        ret.add(parking);
        }
      }
    return ret;
  }
  
  private static ParkingData parseLine(String str) {
    String[] tokens = str.split(";");
    String name = tokens[0];
    name = name.substring(0, 1).toUpperCase() + name.substring(1);
    int numberOfParkingSpots = !tokens[1].equals("") ? Integer.parseInt(tokens[1]) : 0;
    double pricePerHour = !tokens[2].equals("") ? Double.parseDouble(tokens[2].replaceAll(",", ".")) : 0;
    return new ParkingData(name, name, pricePerHour, numberOfParkingSpots);
  }
}
