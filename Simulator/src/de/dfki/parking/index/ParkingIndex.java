package de.dfki.parking.index;

import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.strtree.STRtree;

import allow.simulator.util.Coordinate;
import allow.simulator.world.Street;
import allow.simulator.world.StreetMap;
import allow.simulator.world.StreetNode;
import de.dfki.parking.model.Parking;
import de.dfki.parking.model.Parking.Type;
import de.dfki.parking.model.ParkingRepository;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public final class ParkingIndex {
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
    Envelope queryEnv = JTSUtil.createQueryEnvelope(position, maxDistance);

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
    return spatialIndexHull.contains(JTSUtil.createPoint(pos));
  }

  public Geometry getIndexHull() {
    return spatialIndexHull;
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
      numberOfSpots += entry.getParking().getNumberOfParkingSpots();
    }
    return numberOfSpots;
  }

  private static int countTotalNumberOfFreeParkingSpots(Collection<ParkingIndexEntry> entries, Type type) {
    int numberOfFreeSpots = 0;

    for (ParkingIndexEntry entry : entries) {

      if (entry.getParking().getType() != type)
        continue;
      numberOfFreeSpots += entry.getParking().getNumberOfFreeParkingSpots();
    }
    return numberOfFreeSpots;
  }

  private static STRtree buildSpatialIndex(Collection<ParkingIndexEntry> entries) {
    STRtree ret = new STRtree();

    for (ParkingIndexEntry parking : entries) {
      ret.insert(JTSUtil.createPoint(parking.getReferencePosition()).getEnvelopeInternal(), parking);
    }
    ret.build();
    return ret;
  }

  private static Geometry convexHull(Collection<ParkingIndexEntry> entries) {
    ObjectArrayList<Coordinate> entryPositions = new ObjectArrayList<>();

    for (ParkingIndexEntry entry : entries) {

      if (entry.getParking().getNumberOfParkingSpots() == 0)
        continue;

      entryPositions.add(entry.getReferencePosition());
    }
    return JTSUtil.getConvexHull(entryPositions);
  }
  
  public static ParkingIndex build(StreetMap map, ParkingRepository parkingRepository) {
    Int2ObjectMap<Parking> parkingIdToParking = new Int2ObjectOpenHashMap<>();
    Int2ObjectMap<List<Coordinate>> parkingIdToPositions = new Int2ObjectOpenHashMap<>();
    Int2ObjectMap<ObjectSet<StreetNode>> parkingIdToStreetNodes = new Int2ObjectOpenHashMap<>();
    Int2ObjectMap<IntArrayList> parkingIdToStreetIds = new Int2ObjectOpenHashMap<>();

    for (Street street : map.getStreets()) {
      // Get all parking possibilities in street from repository
      List<Parking> parkings = parkingRepository.getParking(street.getName());

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

        // Check if positions of starting and end nodes needs to be added to
        // spatial index
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
      ParkingIndexEntry indexEntry = new ParkingIndexEntry(parking, positions, getReferencePosition(positions));
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
  
  private static Coordinate getReferencePosition(List<Coordinate> positions) {
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