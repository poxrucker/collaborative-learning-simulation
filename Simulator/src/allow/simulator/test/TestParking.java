package allow.simulator.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import allow.simulator.world.StreetMap;
import de.dfki.parking.model.ParkingMap;
import de.dfki.parking.model.ParkingRepository;

public final class TestParking {

  public static void main(String[] args) throws IOException {
    Path streetMapPath = Paths.get("/Users/Andi/Documents/DFKI/VW simulation/models/data/world/trento_merged.world");
    Path streetParkingPath = Paths.get("/Users/Andi/Documents/DFKI/VW simulation/models/parking_spot/street_parking.csv");
    Path garageParkingPath = Paths.get("/Users/Andi/Documents/DFKI/VW simulation/models/parking_spot/garage_parking.csv");

    StreetMap streetMap = new StreetMap(streetMapPath);
    ParkingRepository parkingRepository = ParkingRepository.load(streetParkingPath, garageParkingPath);
    
    ParkingMap parkingMap = ParkingMap.build(streetMap, parkingRepository);
    
  }
  
  
}
