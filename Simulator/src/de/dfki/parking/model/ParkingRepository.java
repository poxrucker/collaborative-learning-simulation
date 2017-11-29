package de.dfki.parking.model;

import java.util.Collection;
import java.util.Collections;
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
      // Get all streets from StreetMap using OSM nodes
      List<Street> streets = getStreetsFromNodes(data.getOSMNodes(), streetMap);
      
      if (streets == null)
        streets = new ObjectArrayList<>(0);
      
      // Find inverse Street pairs and add a Parking for each
      Map<String, List<Street>> pairs = getStreetPairs(streets);

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
  
  private static List<Street> getStreetsFromNodes(List<String> nodes, StreetMap map) {
    
    if (nodes.size() < 2)
      return null;
    
    List<Street> ret = new ObjectArrayList<>(nodes.size() * 2);
    
    for (int i = 0; i < nodes.size() - 1; i++) {
      String s1 = nodes.get(i);
      String s2 = nodes.get(i + 1);
      
      StreetNode n1 = map.getStreetNodeReduced(s1);
      
      if (n1 == null)
        continue;
      
      StreetNode n2 = map.getStreetNodeReduced(s2);
      
      if (n2 == null)
        continue;
      
      Street street = map.getStreetReduced(n1, n2);
      
      if (street != null)
        ret.add(street);
      
      street = map.getStreetReduced(n2, n1);
      
      if (street != null)
        ret.add(street);
    }
    return ret;
  }
  
  private static Map<String, List<Street>> getStreetPairs(List<Street> streets) {
    Map<String, List<Street>> ret = new Object2ObjectOpenHashMap<>();
    
    for (Street street : streets) {
      String key = getKey(street.getStartingNode(), street.getEndNode());
      List<Street> temp = ret.get(key);
      
      if (temp == null) {
        String keyRev = getKey(street.getEndNode(), street.getStartingNode());
        temp = ret.get(keyRev);
        
        if (temp != null) {
          temp.add(street);
          continue;
        }
        temp = new ObjectArrayList<>(2);
        temp.add(street);
        ret.put(key, temp);
      } 
    }
    return ret;
  }
  
  private static String getKey(StreetNode start, StreetNode end) {
    String l1 = start.getLabel();
    
    if (l1.startsWith("split"))
      l1 = l1.substring(0,5) + l1.substring(6);
    
    String l2 = end.getLabel();
    
    if (l2.startsWith("split"))
      l2 = l2.substring(0,5) + l2.substring(6);
    
    return l1 + "->" + l2;
  }
}