package de.dfki.parking.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import allow.simulator.world.Street;
import allow.simulator.world.StreetMap;
import allow.simulator.world.StreetNode;
import de.dfki.parking.data.ParkingData;
import de.dfki.parking.data.ParkingDataRepository;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class ParkingRepository {
  // Parking in streets
  private final List<Parking> streetParking;
  
  // Parking in garages
  private final List<Parking> garageParking;
  
  private ParkingRepository(List<Parking> streetParking, List<Parking> garageParking) {
    this.streetParking = streetParking;
    this.garageParking = garageParking;
  }
  
  /**
   * Returns a read-only collection of all street parking possibilities.
   * 
   * @return Read-only collection of all street parking possibilities
   */
  public Collection<Parking> getStreetParking() {
    return Collections.unmodifiableCollection(streetParking);
  }
 
  /**
   * Returns a read-only collection of all garage parking possibilities.
   * 
   * @return Read-only collection of all garage parking possibilities
   */
  public Collection<Parking> getGarageParking() {
    return garageParking;
  }
  
  public static ParkingRepository initialize(ParkingDataRepository parkingDataRepository, StreetMap streetMap, ParkingFactory parkingFactory) {
    // Create street parking
    List<ParkingData> streetParkingData = parkingDataRepository.getStreetParkingData();
    List<Parking> streetParking = new ObjectArrayList<>(streetParkingData.size());
    
    for (ParkingData data : streetParkingData) {
      List<Street> streets = streetMap.getStreetByName(data.getAddress());
      
      if (streets == null)
        streets = new ObjectArrayList<>(0);
      
      Parking parking = parkingFactory.createStreetParking(data.getAddress(), data.getPricePerHour(), data.getNumberOfParkingSpots(), streets);
      streetParking.add(parking);
    }
    
    // Create garage parking
    List<ParkingData> garageParkingData = parkingDataRepository.getGarageParkingData();
    List<Parking> garageParking = new ObjectArrayList<>(garageParkingData.size());
    
    for (ParkingData data : garageParkingData) {
      // Find all nodes in StreetMap by node id
      List<StreetNode> accessNodes = new ObjectArrayList<>(data.getOSMNodes().size());
      
      for (String accessNode : data.getOSMNodes()) {
        StreetNode node = streetMap.getStreetNodeReduced(accessNode);
        
        if (node == null)
          continue;
        
        accessNodes.add(node);
      }    
      Parking parking = parkingFactory.createGarageParking(data.getAddress(), data.getPricePerHour(), data.getNumberOfParkingSpots(), accessNodes);
      garageParking.add(parking);
    }
    return new ParkingRepository(streetParking, garageParking);
  }
}