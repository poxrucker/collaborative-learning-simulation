package de.dfki.parking.model;

import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.strtree.STRtree;

import allow.simulator.util.Coordinate;
import allow.simulator.world.Street;
import allow.simulator.world.StreetMap;
import allow.simulator.world.StreetNode;
import de.dfki.parking.model.Parking.Type;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public final class ParkingIndex {

  public static final class ParkingIndexEntry {
    // Referenced Parking instance
    private final Parking parking;

    // List of positions through which Parking is accessible
    private final List<Coordinate> accessPositions;

    // Reference position of Parking instance in index
    private final Coordinate referencePosition;

    public ParkingIndexEntry(Parking parking, List<Coordinate> accessPositions) {
      this.parking = parking;
      this.accessPositions = accessPositions;
      this.referencePosition = updateReferencePosition(accessPositions);
    }

    public Parking getParking() {
      return parking;
    }

    public List<Coordinate> getNodes() {
      return accessPositions;
    }

    public Coordinate getMeanPosition() {
      return referencePosition;
    }

    private static Coordinate updateReferencePosition(List<Coordinate> positions) {
      Coordinate mean = new Coordinate();

      for (Coordinate pos : positions) {
        mean.x += pos.x;
        mean.y += pos.y;
      }
      mean.x /= positions.size();
      mean.y /= positions.size();
      return mean;
    }
  }

  private static final class DistanceFilter implements ItemVisitor {
    // List of ParkingMapEntry instances filtered by distance
    private final List<ParkingIndexEntry> results;

    // Reference position
    private final Coordinate reference;

    // Maximum allowed distance from reference position
    private final double maxDistance;

    public DistanceFilter(List<ParkingIndexEntry> results, Coordinate reference, double maxDistance) {
      this.results = results;
      this.reference = reference;
      this.maxDistance = maxDistance;
    }

    @Override
    public void visitItem(Object item) {
      // Get entry
      ParkingIndexEntry entry = (ParkingIndexEntry) item;

      // If distance to reference position is bigger than maxDistance, filter current entry
      if (allow.simulator.util.Geometry.haversineDistance(reference, entry.referencePosition) > maxDistance)
        return;

      results.add(entry);
    }
  }

  // Parking ids to ParkingIndexEntry
  private final Int2ObjectMap<ParkingIndexEntry> parkingToParkingIndexEntry;

  // Street ids to ParkingIndexEntries 
  private final Int2ObjectMap<List<ParkingIndexEntry>> streetToParkingIndexEntries;

  // Spatial index mapping positions to ParkingMapEntry instances for bounding box lookups
  private final STRtree spatialIndex;

  // Polygon containing all data points
  private final Geometry spatialIndexHull;
  
  // Total number of garage parking spots (calculated during initialization)
  private final int totalNumberOfGarageParkingSpots;

  // Total number of street parking spots (calculated during initialization)
  private final int totalNumberOfStreetParkingSpots;

  private ParkingIndex(Int2ObjectMap<ParkingIndexEntry> parkingToParkingMapEntry, Int2ObjectMap<List<ParkingIndexEntry>> streetToParkingMapEntries) {
    this.parkingToParkingIndexEntry = parkingToParkingMapEntry;
    this.streetToParkingIndexEntries = streetToParkingMapEntries;
    this.spatialIndex = buildSpatialIndex(getAllParkings());
    this.spatialIndexHull = convexHull(getAllParkings());
    this.totalNumberOfGarageParkingSpots = countTotalNumberOfFreeParkingSpots(parkingToParkingMapEntry.values(), Type.GARAGE);
    this.totalNumberOfStreetParkingSpots = countTotalNumberOfParkingSpots(parkingToParkingMapEntry.values(), Type.STREET);
  }

  public ParkingIndexEntry getForParking(Parking parking) {
    return parkingToParkingIndexEntry.get(parking.getId());
  }

  public Collection<ParkingIndexEntry> getAllParkings() {
    return parkingToParkingIndexEntry.values();
  }

  public List<ParkingIndexEntry> getParkingsWithMaxDistance(Coordinate position, double maxDistance) {
    // Create a query envelope centered around position
    Envelope queryEnv = createQueryEnvelope(position, maxDistance);

    // Query spatial index and filter results by distance
    List<ParkingIndexEntry> results = new ObjectArrayList<>();
    spatialIndex.query(queryEnv, new DistanceFilter(results, position, maxDistance));
    return results;
  }

  public List<ParkingIndexEntry> getAllGarageParkingEntries() {
    return findEntriesOfType(parkingToParkingIndexEntry.values(), Type.GARAGE);
  }

  public List<ParkingIndexEntry> getAllStreetParkingEntries() {
    return findEntriesOfType(parkingToParkingIndexEntry.values(), Type.STREET);
  }

  public List<ParkingIndexEntry> getParkingsInStreet(Street street) {
    return streetToParkingIndexEntries.get(street.getId());
  }

  public boolean containedInSpatialIndex(Coordinate pos) {
    return spatialIndexHull.contains(geometryFactory.createPoint(convert(pos)));
  }
  
  public int getTotalNumberOfFreeStreetParkingSpots() {
    return countTotalNumberOfFreeParkingSpots(parkingToParkingIndexEntry.values(), Type.STREET);
  }

  public int getTotalNumberOfFreeGarageParkingSpots() {
    return countTotalNumberOfFreeParkingSpots(parkingToParkingIndexEntry.values(), Type.GARAGE);
  }

  public int getTotalNumberOfGarageParkingSpots() {
    return totalNumberOfGarageParkingSpots;
  }

  public int getTotalNumberOfStreetParkingSpots() {
    return totalNumberOfStreetParkingSpots;
  }

  private static List<ParkingIndexEntry> findEntriesOfType(Collection<ParkingIndexEntry> entries, Type type) {
    List<ParkingIndexEntry> ret = new ObjectArrayList<>();

    for (ParkingIndexEntry entry : entries) {

      if (entry.getParking().getType() != type)
        continue;
      ret.add(entry);
    }
    return ret;
  }

  private static int countTotalNumberOfParkingSpots(Collection<ParkingIndexEntry> entries, Type type) {
    int numberOfSpots = 0;

    for (ParkingIndexEntry entry : entries) {

      if (entry.getParking().getType() != type)
        continue;
      numberOfSpots += entry.parking.getNumberOfParkingSpots();
    }
    return numberOfSpots;
  }

  private static int countTotalNumberOfFreeParkingSpots(Collection<ParkingIndexEntry> entries, Type type) {
    int numberOfFreeSpots = 0;

    for (ParkingIndexEntry entry : entries) {

      if (entry.getParking().getType() != type)
        continue;
      numberOfFreeSpots += entry.parking.getNumberOfFreeParkingSpots();
    }
    return numberOfFreeSpots;
  }

  private static STRtree buildSpatialIndex(Collection<ParkingIndexEntry> entries) {
    STRtree ret = new STRtree();

    for (ParkingIndexEntry parking : entries) {
      ret.insert(geometryFactory.createPoint(convert(parking.getMeanPosition())).getEnvelopeInternal(), parking);
    }
    ret.build();
    return ret;
  }

  private static Geometry convexHull(Collection<ParkingIndexEntry> entries) {
    ObjectArrayList<com.vividsolutions.jts.geom.Coordinate> entryPositions = new ObjectArrayList<>();
    
    for (ParkingIndexEntry entry : entries) {
      
      if (entry.getParking().getNumberOfParkingSpots() == 0)
        continue;
      
      entryPositions.add(convert(entry.getMeanPosition()));
    }
    com.vividsolutions.jts.geom.Coordinate[] temp = entryPositions.toArray(new com.vividsolutions.jts.geom.Coordinate[entryPositions.size()]);
    ConvexHull hull = new ConvexHull(temp, geometryFactory);
    return hull.getConvexHull();
  }
  
  public static ParkingIndex build(StreetMap map, ParkingRepository parkingRepository) {
    Int2ObjectMap<Parking> parkingIdToParking = new Int2ObjectOpenHashMap<>();
    Int2ObjectMap<List<Coordinate>> parkingIdToPositions = new Int2ObjectOpenHashMap<>();
    Int2ObjectMap<ObjectSet<StreetNode>> parkingIdToStreetNodes = new Int2ObjectOpenHashMap<>();
    Int2ObjectMap<IntArrayList> parkingIdToStreetIds = new Int2ObjectOpenHashMap<>();
    
    for (Street street : map.getStreets()) {
      // Get all parking possibilities in street from repository
      List<Parking> parkings = parkingRepository.getParking(street.getName());

      if (parkings.size() == 0 && isRelevantStreet(street)) {
        // If none exists, create Parking with zero parking spots
        // parkings.add(parkingRepository.addStreetParking(street.getName(), 0, 0));
      }
      
      for (Parking parking : parkings) {
        // Record parking id
        parkingIdToParking.put(parking.getId(), parking);
        
        // Record Street to parking mapping
        IntArrayList streetMapping = parkingIdToStreetIds.get(parking.getId());
        
        if (streetMapping == null) {
          streetMapping = new IntArrayList();
          parkingIdToStreetIds.put(parking.getId(), streetMapping);
        }
        streetMapping.add(street.getId());
        
        // Record Street to positions mapping
        List<Coordinate> positions = parkingIdToPositions.get(parking.getId());
        
        if (positions == null) {
          positions = new ObjectArrayList<>();
          parkingIdToPositions.put(parking.getId(), positions);
        }
        
        // Check if positions of starting and end nodes needs to be added to spatial index
        ObjectSet<StreetNode> streetNodes = parkingIdToStreetNodes.get(parking.getId());

        if (streetNodes == null) {
          streetNodes = new ObjectOpenHashSet<>();
          parkingIdToStreetNodes.put(parking.getId(), streetNodes);
        }

        // Check start node
        StreetNode start = street.getStartingNode();

        if (!streetNodes.contains(start)) {
          streetNodes.add(start);
          positions.add(start.getPosition());
        }
        // Check end node
        StreetNode end = street.getEndNode();

        if (!streetNodes.contains(end)) {
          streetNodes.add(end);
          positions.add(end.getPosition());
        }
      }
    }    
    // Mapping of ParkingId to respective ParkingMapEntry instance
    Int2ObjectMap<ParkingIndexEntry> parkingIdToParkingIndexEntry = new Int2ObjectOpenHashMap<>();

    // Mapping of StreetId to ParkingMapEntry instances
    Int2ObjectMap<List<ParkingIndexEntry>> streetIdToParkingIndexEntries = new Int2ObjectOpenHashMap<>();
    
    for (int parkingId : parkingIdToParking.keySet()) {
      Parking parking = parkingIdToParking.get(parkingId);
      List<Coordinate> positions = parkingIdToPositions.get(parkingId);
      ParkingIndexEntry indexEntry = new ParkingIndexEntry(parking, positions);
      parkingIdToParkingIndexEntry.put(parkingId, indexEntry);
      
      for (int streetId : parkingIdToStreetIds.get(parkingId)) {
        List<ParkingIndexEntry> entries = streetIdToParkingIndexEntries.get(streetId);
        
        if (entries == null) {
          entries = new ObjectArrayList<>();
          streetIdToParkingIndexEntries.put(streetId, entries);
        }
        entries.add(indexEntry);
      }
    }
    return new ParkingIndex(parkingIdToParkingIndexEntry, streetIdToParkingIndexEntries);
  }

  private static final GeometryFactory geometryFactory = new GeometryFactory();

  private static final double EARTH_CIRCUMFERENCE_IN_M = 40074 * 1000;
  private static final double LAT_M_TO_DEG = 360.0 / EARTH_CIRCUMFERENCE_IN_M;

  private static com.vividsolutions.jts.geom.Coordinate convert(Coordinate c) {
    return new com.vividsolutions.jts.geom.Coordinate(c.x, c.y);
  }

  private static Envelope createQueryEnvelope(Coordinate center, double maxDistance) {
    // Half maximum distance in every direction
    double halfDistance = 0.5 * maxDistance;

    // Sample four points from circle around center with radius maxDistance to
    // obtain query envelope
    Coordinate north = new Coordinate(center.x, center.y + LAT_M_TO_DEG * halfDistance);
    Coordinate south = new Coordinate(center.x, center.y - LAT_M_TO_DEG * halfDistance);
    double circumferenceCenter = Math.cos(Math.toRadians(center.y)) * EARTH_CIRCUMFERENCE_IN_M;
    double lonMToDegCenter = 360.0 / circumferenceCenter;
    Coordinate east = new Coordinate(center.x - lonMToDegCenter * halfDistance, center.y);
    Coordinate west = new Coordinate(center.x + lonMToDegCenter * halfDistance, center.y);

    // Create point sequence to build geometry and get Envelope instance
    com.vividsolutions.jts.geom.Coordinate[] points = new com.vividsolutions.jts.geom.Coordinate[] {
        new com.vividsolutions.jts.geom.Coordinate(north.x, north.y), new com.vividsolutions.jts.geom.Coordinate(east.x, east.y),
        new com.vividsolutions.jts.geom.Coordinate(south.x, south.y), new com.vividsolutions.jts.geom.Coordinate(west.x, west.y) };
    return geometryFactory.createMultiPoint(points).getEnvelopeInternal();
  }

  private static boolean isRelevantStreet(Street s) {
    return !((s.getName().equals("Fußweg") || s.getName().equals("Bürgersteig") || s.getName().equals("Stufen") || s.getName().equals("Weg")
        || s.getName().equals("Gasse") || s.getName().equals("Auffahrrampe") || s.getName().equals("Fußgängertunnel")
        || s.getName().equals("Fahrradweg") || s.getName().equals("Fußgängerbrücke")) || s.getName().contains("Autostrada")
        || s.getName().equals("Via Bolzano") || s.getName().contains("Strada Statale") || s.getName().contains("Tangenziale"));
  }
}
