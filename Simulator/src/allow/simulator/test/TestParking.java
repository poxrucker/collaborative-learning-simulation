package allow.simulator.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import allow.simulator.world.StreetMap;
import de.dfki.parking.data.ParkingDataRepository;
import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.index.ParkingIndexEntry;
import de.dfki.parking.model.ParkingFactory;
import de.dfki.parking.model.ParkingRepository;

public final class TestParking {

  public static void main(String[] args) throws IOException {
    Path streetMapPath = Paths.get("/Users/Andi/Documents/DFKI/VW simulation/models/data/world/trento_merged.world");
    Path streetParkingPath = Paths.get("/Users/Andi/Documents/DFKI/VW simulation/models/parking_spot/street_parking_with_nodes.csv");
    Path garageParkingPath = Paths.get("/Users/Andi/Documents/DFKI/VW simulation/models/parking_spot/garage_parking_with_nodes.csv");
    Path output = Paths.get("/Users/Andi/Documents/DFKI/VW simulation/models/parking_spot/spatial");
    
    StreetMap streetMap = new StreetMap(streetMapPath);
    ParkingDataRepository parkingDataRepository = ParkingDataRepository.load(streetParkingPath, garageParkingPath);
    ParkingRepository parkingRepository = ParkingRepository.initialize(parkingDataRepository, streetMap, new ParkingFactory(1.0, 0.0), false);
    ParkingIndex index = ParkingIndex.build(parkingRepository);
    
    try (BufferedWriter writer = Files.newBufferedWriter(output.resolve("bounds.txt"))) {
      double[] dim = streetMap.getDimensions();
      writer.write(dim[0] + "," + dim[2] + "\n");
      writer.write(dim[1] + "," + dim[2] + "\n");
      writer.write(dim[1] + "," + dim[3] + "\n");
      writer.write(dim[0] + "," + dim[3] + "\n");
      writer.write(dim[0] + "," + dim[2] + "\n");
    }
    
    try (BufferedWriter writer = Files.newBufferedWriter(output.resolve("parking_positions.txt"))) {
      Collection<ParkingIndexEntry> all = index.getAllEntries();
      
      for (ParkingIndexEntry entry : all) {
        writer.write(entry.getReferencePosition().x + "," + entry.getReferencePosition().y + "\n");
      }
    }
    
    try (BufferedWriter writer = Files.newBufferedWriter(output.resolve("parking_names.txt"))) {
      Collection<ParkingIndexEntry> all = index.getAllEntries();
      
      for (ParkingIndexEntry entry : all) {
        writer.write(entry.getParking().getAddress() + "\n");
      }
    }
  }
  
  
}
