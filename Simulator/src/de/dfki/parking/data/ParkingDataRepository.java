package de.dfki.parking.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public final class ParkingDataRepository {  
  // List of street parking possibilities
  private final List<ParkingData> streetParkingData;
  
  // List of garage parking possibilities
  private final List<ParkingData> garageParkingData;
  
  private ParkingDataRepository(List<ParkingData> streetParking, List<ParkingData> garageParking) {
    this.streetParkingData = streetParking;
    this.garageParkingData = garageParking;
  }
  
  public List<ParkingData> getStreetParkingData() {
    return Collections.unmodifiableList(streetParkingData);
  }
  
  public List<ParkingData> getGarageParkingData() {
    return Collections.unmodifiableList(garageParkingData);
  }
  
  public static ParkingDataRepository load(Path streetParkingFqfn, Path garageParkingFqfn) throws IOException {
    // Load street parking possibilities from file
    IParkingDataFileParser streetFileParser = new StreetParkingFileParser();
    List<ParkingData> streetParking = streetFileParser.parseFile(streetParkingFqfn);
    
    // Load garage parking possibilities from file
    IParkingDataFileParser garageFileParser = new GarageParkingFileParser();
    List<ParkingData> garageParking = garageFileParser.parseFile(garageParkingFqfn);
    return new ParkingDataRepository(streetParking, garageParking);
  }
}