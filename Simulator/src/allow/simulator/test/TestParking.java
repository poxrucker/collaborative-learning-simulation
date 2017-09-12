package allow.simulator.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import allow.simulator.parking.ParkingMap;
import allow.simulator.world.StreetMap;

public final class TestParking {

  public static void main(String[] args) throws IOException {
    Path streetMapPath = Paths.get("/Users/Andi/Documents/DFKI/VW simulation/models/data/world/trento_merged.world");
    Path streetParkingPath = Paths.get("/Users/Andi/Documents/DFKI/VW simulation/models/coverage/street_parking.csv");
    Path garageParkingPath = Paths.get("/Users/Andi/Documents/DFKI/VW simulation/models/coverage/garage_parking.csv");

    StreetMap streetMap = new StreetMap(streetMapPath);
    ParkingMap.load(streetMap, streetParkingPath, garageParkingPath);
  }
}
