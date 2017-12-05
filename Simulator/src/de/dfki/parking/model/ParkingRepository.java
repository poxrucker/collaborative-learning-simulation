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
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
    return Collections.unmodifiableCollection(garageParking);
  }

  public static ParkingRepository initialize(ParkingDataRepository parkingDataRepository, StreetMap streetMap, ParkingFactory parkingFactory) {
    // Create street parking
    List<ParkingData> streetParkingData = parkingDataRepository.getStreetParkingData();
    List<Parking> streetParking = new ObjectArrayList<>(streetParkingData.size());

    for (ParkingData data : streetParkingData) {
      if (data.getNumberOfParkingSpots() == 0)
        continue;
      // Get all streets from StreetMap using OSM nodes
      List<Street> streets = getStreetsFromNodes(data.getOSMNodes(), streetMap);

      if (streets == null)
        streets = new ObjectArrayList<>(0);

      // Find inverse Street pairs and add a Parking for each
      Map<String, List<Street>> streetPairs = getStreetPairs(streets);
      Map<String, Integer> parkingSpotsPerPair = distributeParkingSpots(data.getNumberOfParkingSpots(), streetPairs);
      
      for (String key : streetPairs.keySet()) {
        List<Street> streetPair = streetPairs.get(key);
        int nSpots = parkingSpotsPerPair.get(key);
        Parking parking = parkingFactory.createStreetParking(data.getAddress(), data.getPricePerHour(), nSpots, streetPair);
        streetParking.add(parking);
      }     
    }

    // Create garage parking
    List<ParkingData> garageParkingData = parkingDataRepository.getGarageParkingData();
    List<Parking> garageParking = new ObjectArrayList<>(garageParkingData.size());

    for (ParkingData data : garageParkingData) {
      if (data.getNumberOfParkingSpots() == 0)
        continue;
      
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
      boolean splitStreet = s1.startsWith("split") || s2.startsWith("split");
      Street street = splitStreet ? findStreetSplit(s1, s2, map) : findStreet(s1, s2, map);

      if (street != null)
        ret.add(street);

      street = splitStreet ? findStreetSplit(s2, s1, map) : findStreet(s2, s1, map);

      if (street != null)
        ret.add(street);
    }
    return ret;
  }

  private static Street findStreet(String startNode, String endNode, StreetMap map) {
    StreetNode n1 = map.getStreetNodeReduced(startNode);

    if (n1 == null)
      return null;

    StreetNode n2 = map.getStreetNodeReduced(endNode);

    if (n2 == null)
      return null;
    return map.getStreetReduced(n1, n2);
  }

  private static Street findStreetSplit(String startNode, String endNode, StreetMap map) {
    String[] pStart = startNode.startsWith("split")
        ? new String[] { startNode.substring(0, 5) + "1" + startNode.substring(6), startNode.substring(0, 5) + "2" + startNode.substring(6) }
        : new String[] { startNode };

    String[] pEnd = endNode.startsWith("split")
        ? new String[] { endNode.substring(0, 5) + "1" + endNode.substring(6), endNode.substring(0, 5) + "2" + endNode.substring(6) }
        : new String[] { endNode };
        
    for (int i = 0; i < pStart.length; i++) {
      
      for (int j = 0; j < pEnd.length; j++) {
        Street ret = findStreet(pStart[i], pEnd[j], map);
        
        if (ret != null)
          return ret;
      }
    }
    return null;
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
      l1 = l1.substring(0, 5) + l1.substring(6);

    String l2 = end.getLabel();

    if (l2.startsWith("split"))
      l2 = l2.substring(0, 5) + l2.substring(6);

    return l1 + "->" + l2;
  }
  
  private static Map<String, Integer> distributeParkingSpots(int nSpots, Map<String, List<Street>> streetPairs) {
    double totalLength = 0.0;
    Map<String, Double> streetPairLengths = new Object2DoubleOpenHashMap<>();
    
    for (String key : streetPairs.keySet()) {
      double length = streetPairs.get(key).get(0).getLength();
      streetPairLengths.put(key, length);
      totalLength += length;
    }
    
    Map<String, Integer> ret = new Object2IntOpenHashMap<>();

    for (String key : streetPairs.keySet()) {
      double length = streetPairLengths.get(key);
      int spots = (int) Math.round(nSpots * length / totalLength);
      ret.put(key, spots);
    }
    return ret;
  }
}