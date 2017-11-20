package de.dfki.parking.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class GarageParkingFileParser implements IParkingDataFileParser {

  @Override
  public Map<String, List<ParkingData>> parseFile(Path fqfn) throws IOException {
    Map<String, List<ParkingData>> ret = new Object2ObjectOpenHashMap<>();

    try (BufferedReader reader = Files.newBufferedReader(fqfn)) {
      String line = null;

      while ((line = reader.readLine()) != null) {
        // Parse line
        ParkingData parking = parseGarageParking(line);
        
        // Add to list
        List<ParkingData> temp = ret.get(parking.getAddress());

        if (temp == null) {
          temp = new ObjectArrayList<>();
          ret.put(parking.getAddress(), temp);
        }
        temp.add(parking);
      }
    }
    return ret;
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
