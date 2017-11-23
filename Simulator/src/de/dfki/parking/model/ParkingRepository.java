package de.dfki.parking.model;

import java.util.List;
import java.util.Map;

import allow.simulator.world.Street;
import allow.simulator.world.StreetMap;
import allow.simulator.world.StreetNode;
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
  
  public static ParkingRepository initialize(ParkingDataRepository parkingDataRepository, StreetMap streetMap, ParkingFactory parkingFactory) {
    ParkingRepository ret = new ParkingRepository();
    
    // Create street parking
    List<ParkingData> streetParkingData = parkingDataRepository.getStreetParkingData();
    
    for (ParkingData data : streetParkingData) {
      List<Street> streets = streetMap.getStreetByName(data.getAddress());
      
      if (streets == null)
        streets = new ObjectArrayList<>(0);
      
      Parking parking = parkingFactory.createStreetParking(data.getAddress(), data.getPricePerHour(), data.getNumberOfParkingSpots(), streets);
      ret.addStreetParking(parking);
    }
    
    // Create garage parking
    List<ParkingData> garageParkingData = parkingDataRepository.getGarageParkingData();
    
    for (ParkingData data : garageParkingData) {
      List<StreetNode> accessNodes = new ObjectArrayList<>(data.getOSMNodes().size());
      
      for (String accessNode : data.getOSMNodes()) {
        StreetNode node = streetMap.getStreetNodeReduced(accessNode);
        
        if (node == null) {
          continue;
        }
        accessNodes.add(node);
      }    
      Parking parking = parkingFactory.createGarageParking(data.getAddress(), data.getPricePerHour(), data.getNumberOfParkingSpots(), accessNodes);
      ret.addGarageParking(parking);
    }
    return ret;
  }
}
