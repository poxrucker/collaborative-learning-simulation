package de.dfki.parking.model;

import java.util.List;
import java.util.Map;

import de.dfki.parking.data.ParkingData;
import de.dfki.parking.data.ParkingDataRepository;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class ParkingRepository {

  private final Map<String, List<Parking>> streetParkings;
  private final Map<String, List<Parking>> garageParkings;
  
  private ParkingRepository() {
    this.streetParkings = new Object2ObjectOpenHashMap<>();
    this.garageParkings = new Object2ObjectOpenHashMap<>();
  }
  
  public void addStreetParking(Parking parking) {    
    addParking(parking, streetParkings);
  }
  
  public void addGarageParking(Parking parking) {    
    addParking(parking, garageParkings);
  }
  
  public List<Parking> getParking(String street) {
    List<Parking> garageParking = garageParkings.get(street);
    List<Parking> streetParking = streetParkings.get(street);
    
    List<Parking> ret = new ObjectArrayList<>();
    
    if (garageParking != null)
      ret.addAll(garageParking);
    
    if (streetParking != null)
      ret.addAll(streetParking);
    
    return ret;
  }
  
  private static void addParking(Parking parking, Map<String, List<Parking>> parkings) {
    List<Parking> temp = parkings.get(parking.getAddress());
    
    if (temp == null) {
      temp = new ObjectArrayList<>();
      parkings.put(parking.getAddress(), temp);
    }
    temp.add(parking);
  }
  
  public static ParkingRepository initialize(ParkingDataRepository parkingDataRepository, ParkingFactory parkingFactory) {
    ParkingRepository ret = new ParkingRepository();
    
    // Create street parking
    Map<String, List<ParkingData>> streetParkingData = groupByAddress(parkingDataRepository.getStreetParkingData());
    
    for (Map.Entry<String, List<ParkingData>> entry : streetParkingData.entrySet()) {
      
      for (ParkingData data : entry.getValue()) {
        Parking parking = parkingFactory.createStreetParking(entry.getKey(), data.getPricePerHour(), data.getNumberOfParkingSpots());
        ret.addStreetParking(parking);
      }
    }
    
    // Create garage parking
    Map<String, List<ParkingData>> garageParkingData = groupByAddress(parkingDataRepository.getGarageParkingData());
    
    for (Map.Entry<String, List<ParkingData>> entry : garageParkingData.entrySet()) {
      
      for (ParkingData data : entry.getValue()) {
        Parking parking = parkingFactory.createGarageParking(entry.getKey(), data.getPricePerHour(), data.getNumberOfParkingSpots());
        ret.addGarageParking(parking);
      }
    }
    return ret;
  }
 
  private static Map<String, List<ParkingData>> groupByAddress(List<ParkingData> parkingData) {
    Map<String, List<ParkingData>> ret = new Object2ObjectOpenHashMap<>();
    
    for (ParkingData data : parkingData) {
      List<ParkingData> temp = ret.get(data.getAddress());
      
      if (temp == null) {
        temp = new ObjectArrayList<>();
        ret.put(data.getAddress(), temp);
      }
      temp.add(data);
    }
    return ret;
  }
}
