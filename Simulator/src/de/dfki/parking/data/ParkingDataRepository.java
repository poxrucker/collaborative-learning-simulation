package de.dfki.parking.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    // Load street parking possibilities from file
    IParkingDataFileParser streetFileParser = new StreetParkingFileParser();
    Map<String, List<ParkingData>> streetParking = streetFileParser.parseFile(streetParkingFqfn);
    
    // Load garage parking possibilities from file
    IParkingDataFileParser garageFileParser = new GarageParkingFileParser();
    Map<String, List<ParkingData>> garageParking = garageFileParser.parseFile(garageParkingFqfn);
    return new ParkingDataRepository(streetParking, garageParking);
  }
}