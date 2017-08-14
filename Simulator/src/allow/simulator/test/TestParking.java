package allow.simulator.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import allow.simulator.world.ParkingMap;
import allow.simulator.world.StreetMap;

public final class TestParking {

  public static void main(String[] args) throws IOException {
    Path streetMapPath = Paths.get("/Users/Andi/Documents/DFKI/VW simulation/models/data/world/trento_merged.world");
    Path parkingMapPath = Paths.get("/Users/Andi/Documents/DFKI/VW simulation/models/coverage/parking.csv");
    
    StreetMap streetMap = new StreetMap(streetMapPath);
    writeStreetNames(streetMap.getStreetNames());
    ParkingMap parkingMap = ParkingMap.load(streetMap, parkingMapPath);
  }
  
  private static void writeStreetNames(Collection<String> names) throws IOException {
    
    try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("/Users/Andi/Documents/DFKI/VW simulation/models/coverage/streets.txt"))) {
      
      for (String name : names) {
        writer.write(name + "\n");
      }    
    }
  }
}
