package allow.simulator.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import allow.simulator.world.StreetMap;
import de.dfki.parking.model.ParkingDataRepository;
import de.dfki.parking.model.ParkingIndex;

public final class TestParking {

  public static void main(String[] args) throws IOException {
    Path streetMapPath = Paths.get("/Users/Andi/Documents/DFKI/VW simulation/models/data/world/trento_merged.world");
    Path streetParkingPath = Paths.get("/Users/Andi/Documents/DFKI/VW simulation/models/parking_spot/street_parking.csv");
    Path garageParkingPath = Paths.get("/Users/Andi/Documents/DFKI/VW simulation/models/parking_spot/garage_parking.csv");

    StreetMap streetMap = new StreetMap(streetMapPath);
    ParkingDataRepository parkingRepository = ParkingDataRepository.load(streetParkingPath, garageParkingPath, 1.0);
    
    ParkingIndex.build(streetMap, parkingRepository);
    
  }
  
  
}
