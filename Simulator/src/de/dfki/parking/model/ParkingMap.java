package de.dfki.parking.model;

import java.util.Collection;
import java.util.List;

import org.jboss.netty.util.internal.ThreadLocalRandom;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.strtree.STRtree;

import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.world.Street;
import allow.simulator.world.StreetMap;
import allow.simulator.world.StreetNode;
import de.dfki.parking.model.Parking.Type;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public final class ParkingMap {

  public static final class ParkingMapEntry {
    // Parking this entry refers to
    private final Parking parking;
    
    // List of StreetNodes associated with the Parking
    private final List<StreetNode> nodes;
    
    // Mean position associated with the Parking
    private Coordinate meanPosition;
     
    public ParkingMapEntry(Parking parking) {
      this.parking = parking;
      this.nodes = new ObjectArrayList<>();
    }
    
    public Parking getParking() {
      return parking;
    }
    
    public List<StreetNode> getNodes() {
      return nodes;
    }
    
    public Coordinate getMeanPosition() {
      return meanPosition;
    }
    
    public void addNode(StreetNode node) {
      nodes.add(node);
      meanPosition = calculateMeanPosition(nodes);
    }
      
    private static Coordinate calculateMeanPosition(List<StreetNode> nodes) {
      Coordinate mean = new Coordinate();
      
      for (StreetNode node : nodes) {
        mean.x += node.getPosition().x;
        mean.y += node.getPosition().y;
      }
      mean.x /= nodes.size();
      mean.y /= nodes.size();
      return mean;
    }
  }
  
  private static final class DistanceFilter implements ItemVisitor {
    // List of ParkingMapEntry instances filtered by distance
    private final List<ParkingMapEntry> results;
    
    // Reference position
    private final Coordinate position;
    
    // Maximum allowed distance from reference position
    private final double maxDistance;
    
    public DistanceFilter(List<ParkingMapEntry> results, Coordinate position, double maxDistance) {
      this.results = results;
      this.position = position;
      this.maxDistance = maxDistance;
    }
    
    @Override
    public void visitItem(Object item) {
      // Get entry
      ParkingMapEntry entry = (ParkingMapEntry)item;
      
      // If distance to current position is bigger than maxDistance, filter current entry
      if (Geometry.haversineDistance(position, entry.meanPosition) > maxDistance)
        return;
      
      results.add(entry);
    }
  }
  
  // Spatial index mapping positions to ParkingMapEntry instances for bounding box lookups
  private final Int2ObjectMap<ParkingMapEntry> parkingToParkingMapEntry;
  private final Int2ObjectMap<List<ParkingMapEntry>> streetToParkingMapEntries;
  private final STRtree spatialIndex;
  
  // Total number of garage parking spots (calculated during initialization)
 private final int totalNumberOfGarageParkingSpots;
 
 // Total number of street parking spots (calculated during initialization)
 private final int totalNumberOfStreetParkingSpots;
 
  private ParkingMap(Int2ObjectMap<ParkingMapEntry> parkingToParkingMapEntry, 
      Int2ObjectMap<List<ParkingMapEntry>> streetToParkingMapEntries) {
    this.parkingToParkingMapEntry = parkingToParkingMapEntry;
    this.streetToParkingMapEntries = streetToParkingMapEntries;
    this.spatialIndex = buildSpatialIndex();
    this.totalNumberOfGarageParkingSpots = countTotalNumberOfFreeParkingSpots(parkingToParkingMapEntry.values(), Type.GARAGE);
    this.totalNumberOfStreetParkingSpots = countTotalNumberOfParkingSpots(parkingToParkingMapEntry.values(), Type.STREET);
  }
 
  public ParkingMapEntry getForParking(Parking parking) {
    return parkingToParkingMapEntry.get(parking.getId());
  }
  
  public Collection<ParkingMapEntry> getAllParkings() {
    return parkingToParkingMapEntry.values();
  }
  
  public List<ParkingMapEntry> getParkingsWithMaxDistance(Coordinate position, double maxDistance) {
    // Create a query envelope centered around position
    Envelope queryEnv = createQueryEnvelope(position, maxDistance);
    
    // Query spatial index and filter results by distance
    List<ParkingMapEntry> results = new ObjectArrayList<>();
    spatialIndex.query(queryEnv, new DistanceFilter(results, position, maxDistance));
    return results;
  }
  
  public List<ParkingMapEntry> getParkingsInStreet(Street street) {
    return streetToParkingMapEntries.get(street.getId());
  }
  
  public int getTotalNumberOfFreeStreetParkingSpots() {
    return countTotalNumberOfFreeParkingSpots(parkingToParkingMapEntry.values(), Type.STREET);
  }
  
  public int getTotalNumberOfFreeGarageParkingSpots() {
    return countTotalNumberOfFreeParkingSpots(parkingToParkingMapEntry.values(), Type.GARAGE);
  }
  
  public int getTotalNumberOfGarageParkingSpots() {
    return totalNumberOfGarageParkingSpots;
  }
  
  public int getTotalNumberOfStreetParkingSpots() {
    return totalNumberOfStreetParkingSpots;
  }
  
  private static int countTotalNumberOfParkingSpots(Collection<ParkingMapEntry> entries, Type type) {
    int numberOfSpots = 0;
    
    for (ParkingMapEntry entry : entries) {
      
      if (entry.getParking().getType() != type)
        continue;
      numberOfSpots += entry.parking.getNumberOfParkingSpots();
    }
    return numberOfSpots;
  }
 
  private static int countTotalNumberOfFreeParkingSpots(Collection<ParkingMapEntry> entries, Type type) {
    int numberOfFreeSpots = 0;
    
    for (ParkingMapEntry entry : entries) {
      
      if (entry.getParking().getType() != type)
        continue;
      numberOfFreeSpots += entry.parking.getNumberOfFreeParkingSpots();
    }
    return numberOfFreeSpots;
  }
  
  private STRtree buildSpatialIndex() {
    STRtree index = new STRtree();
    
    for (ParkingMapEntry parking : getAllParkings()) {
      index.insert(createPointEnvelope(parking.getMeanPosition()), parking);
    }
    index.build();
    return index;
  }
  
  public static ParkingMap build(StreetMap map, ParkingRepository parkingRepository) {
    // Mapping of ParkingId to respective ParkingMapEntry instance
    Int2ObjectMap<ParkingMapEntry> parkingToParkingMapEntry = new Int2ObjectOpenHashMap<>();
    
    // Mapping of ParkingId to StreetNode instances
    Int2ObjectMap<ObjectSet<StreetNode>> parkingToStreetNodes = new Int2ObjectOpenHashMap<>();
    
    // Mapping of StreetId to ParkingMapEntry instances
    Int2ObjectMap<List<ParkingMapEntry>> streetToParkingMapEntries = new Int2ObjectOpenHashMap<>();
    
    for (Street street : map.getStreets()) {
      // Get all parking possibilities in street from repository
      List<Parking> parkings = parkingRepository.getParking(street);
      
      if (((parkings == null) || (parkings.size() == 0)) && isRelevantStreet(street)) {
        
        int maxNumberOfSpots = (int)(street.getLength() / 15.0);
        
        if (maxNumberOfSpots == 0)
          continue;
        
        int nSpots = ThreadLocalRandom.current().nextInt(maxNumberOfSpots);
        
        if (nSpots == 0)
          continue;
        
        Parking p = new FixedPriceParking(Type.STREET, street.getName(), street.getName(), 1.0, nSpots);
        parkings.add(p);
      }
      
      // List of entries to add to street to StreetId to ParkingMapEntry mapping
      List<ParkingMapEntry> entries = new ObjectArrayList<>(parkings.size());
      
      for (Parking parking : parkings) {
        // Check if an entry has already been created for this Parking instance
        ParkingMapEntry entry = parkingToParkingMapEntry.get(parking.getId());
        
        if (entry == null) {
          entry = new ParkingMapEntry(parking);
          parkingToParkingMapEntry.put(parking.getId(), entry);
        }
        entries.add(entry);
        
        // Check if positions of starting and end nodes needs to be added to spatial index
        ObjectSet<StreetNode> streetNodes = parkingToStreetNodes.get(parking.getId());
        
        if (streetNodes == null) {
          streetNodes = new ObjectOpenHashSet<>();
          parkingToStreetNodes.put(parking.getId(), streetNodes);
        }
        
        // Check start node
        StreetNode start = street.getStartingNode();
        
        if (!streetNodes.contains(start))   {
          streetNodes.add(start);
          entry.addNode(start);
        }
        // Check end node
        StreetNode end = street.getEndNode();
        
        if (!streetNodes.contains(end)) {
          streetNodes.add(end);
          entry.addNode(end);
        }
      }
      streetToParkingMapEntries.put(street.getId(), entries);
    }    
    return new ParkingMap(parkingToParkingMapEntry, streetToParkingMapEntries);
  }
  
  private static final GeometryFactory geometryFactory = new GeometryFactory();
  
  private static final double EARTH_CIRCUMFERENCE_IN_M = 40074 * 1000;
  private static final double LAT_M_TO_DEG = 360.0 / EARTH_CIRCUMFERENCE_IN_M;
  
  private static Envelope createPointEnvelope(Coordinate c) {   
    return geometryFactory.createPoint(new com.vividsolutions.jts.geom.Coordinate(c.x, c.y)).getEnvelopeInternal();
  }
  
  private static Envelope createQueryEnvelope(Coordinate center, double maxDistance) {
    // Half maximum distance in every direction
    double halfDistance = 0.5 * maxDistance;
    
    // Sample four points from circle around center with radius maxDistance to obtain query envelope
    Coordinate north = new Coordinate(center.x, center.y + LAT_M_TO_DEG * halfDistance);
    Coordinate south = new Coordinate(center.x, center.y - LAT_M_TO_DEG * halfDistance);    
    double circumferenceCenter = Math.cos(Math.toRadians(center.y)) * EARTH_CIRCUMFERENCE_IN_M;
    double lonMToDegCenter = 360.0 / circumferenceCenter;
    Coordinate east = new Coordinate(center.x - lonMToDegCenter * halfDistance, center.y);
    Coordinate west = new Coordinate(center.x + lonMToDegCenter * halfDistance, center.y);
    
    // Create point sequence to build geometry and get Envelope instance
    com.vividsolutions.jts.geom.Coordinate[] points = new com.vividsolutions.jts.geom.Coordinate[] {
        new com.vividsolutions.jts.geom.Coordinate(north.x, north.y),
        new com.vividsolutions.jts.geom.Coordinate(east.x, east.y),
        new com.vividsolutions.jts.geom.Coordinate(south.x, south.y),
        new com.vividsolutions.jts.geom.Coordinate(west.x, west.y)     
    };
    return geometryFactory.createMultiPoint(points).getEnvelopeInternal();
  }
  
  private static boolean isRelevantStreet(Street s) {
    return !((s.getName().equals("Fußweg") 
        || s.getName().equals("Bürgersteig") 
        || s.getName().equals("Stufen") 
        || s.getName().equals("Weg")
        || s.getName().equals("Gasse")
        || s.getName().equals("Auffahrrampe")
        || s.getName().equals("Fußgängertunnel") 
        || s.getName().equals("Fahrradweg")
        || s.getName().equals("Fußgängerbrücke"))
        || s.getName().contains("Autostrada")
        || s.getName().equals("Via Bolzano")
        || s.getName().contains("Strada Statale")
        || s.getName().contains("Tangenziale"));
  }
}
