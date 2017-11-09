package de.dfki.parking.model;

import java.util.List;
import java.util.Map;

import de.dfki.parking.data.ParkingData;
import de.dfki.parking.data.ParkingDataRepository;
import de.dfki.parking.model.Parking.Type;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class ParkingRepository {

  private int ids;
  private final Map<String, List<Parking>> streetParkings;
  private final Map<String, List<Parking>> garageParkings;
  
  private ParkingRepository() {
    this.streetParkings = new Object2ObjectOpenHashMap<>();
    this.garageParkings = new Object2ObjectOpenHashMap<>();
  }
  
  public Parking addStreetParking(String street, double pricePerHour, int nParkingSpots) {
    Parking newParking = new FixedPriceParking(ids++, Type.STREET, street, street, pricePerHour, nParkingSpots);
    
    List<Parking> streetParking = streetParkings.get(street);
    
    if (streetParking == null) {
      streetParking = new ObjectArrayList<>();
      streetParkings.put(street, streetParking);
    }
    streetParking.add(newParking);
    return newParking;
  }
  
  public Parking addGarageParking(String street, double pricePerHour, int nParkingSpots) {
    Parking newParking = new DynamicPriceParking(ids++, Type.GARAGE, street, street, pricePerHour, nParkingSpots);
    
    List<Parking> garageParking = garageParkings.get(street);
    
    if (garageParking == null) {
      garageParking = new ObjectArrayList<>();
      garageParkings.put(street, garageParking);
    }
    garageParking.add(newParking);
    return newParking;
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
  
  public static ParkingRepository initialize(ParkingDataRepository parkingDataRepository, double scalingFactor) {
    ParkingRepository ret = new ParkingRepository();
    
    // Create street parking
    Map<String, List<ParkingData>> streetParkingData = parkingDataRepository.getStreetParkingData();
    
    for (Map.Entry<String, List<ParkingData>> entry : streetParkingData.entrySet()) {
      
      for (ParkingData data : entry.getValue()) {
        ret.addStreetParking(entry.getKey(), data.getPricePerHour(), scale(data.getNumberOfParkingSpots(), scalingFactor));
      }
    }
    
    // Create garage parking
    Map<String, List<ParkingData>> garageParkingData = parkingDataRepository.getGarageParkingData();
    
    for (Map.Entry<String, List<ParkingData>> entry : garageParkingData.entrySet()) {
      
      for (ParkingData data : entry.getValue()) {
        ret.addGarageParking(entry.getKey(), data.getPricePerHour() + 1.0, scale(data.getNumberOfParkingSpots(), scalingFactor));
      }
    }
    return ret;
  }
 
  private static int scale(int input, double scalingFactor) {
    return (input != 0) ? (int) Math.ceil(input * scalingFactor) : 0;    
  }
}
