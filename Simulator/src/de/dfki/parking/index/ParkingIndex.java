package de.dfki.parking.index;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import allow.simulator.util.Coordinate;
import allow.simulator.world.Street;
import allow.simulator.world.StreetNode;
import de.dfki.parking.model.GarageParking;
import de.dfki.parking.model.Parking;
import de.dfki.parking.model.Parking.Type;
import de.dfki.parking.model.ParkingRepository;
import de.dfki.parking.model.StreetParking;
import de.dfki.parking.spatial.UnmodifiableSpatialIndex;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public final class ParkingIndex {
  // Parking id to ParkingIndexEntry
  private final Int2ObjectMap<ParkingIndexEntry> parkingIndex;

  // Street id to ParkingIndexEntry mapping
  private final Int2ObjectMap<ParkingIndexEntry> streetParkingIndex;
    
  // StreetNode id to ParkingIndexEntry mapping
  private final Int2ObjectMap<ParkingIndexEntry> garageParkingIndex;
  
  // Spatial index mapping positions to ParkingMapEntry instances for bounding box lookups
  private final UnmodifiableSpatialIndex<ParkingIndexEntry> spatialIndex;

  private ParkingIndex(Int2ObjectMap<ParkingIndexEntry> parkingIndex, Int2ObjectMap<ParkingIndexEntry> streetParkingIndex,
      Int2ObjectMap<ParkingIndexEntry> garageParkingIndex, UnmodifiableSpatialIndex<ParkingIndexEntry> spatialIndex) {
    this.parkingIndex = parkingIndex;
    this.streetParkingIndex = streetParkingIndex;
    this.garageParkingIndex = garageParkingIndex;
    this.spatialIndex = spatialIndex;
  }

  /**
   * Returns a read-only collection of all entries contained in this instance.
   * 
   * @return Read-only collection of all entries contained in this instance
   */
  public Collection<ParkingIndexEntry> getAllEntries() {
    return Collections.unmodifiableCollection(parkingIndex.values());
  }

  /**
   * Returns a read-only collection of all street parking entries contained in this instance.
   * 
   * @return Read-only collection of all street parking entries contained in this instance
   */
  public Collection<ParkingIndexEntry> getAllStreetParkingEntries() {
    return Collections.unmodifiableCollection(findEntriesOfType(parkingIndex.values(), Type.STREET));
  }
  
  /**
   * Returns a read-only collection of all garage parking entries contained in this instance.
   * 
   * @return Read-only collection of all garage parking entries contained in this instance
   */
  public Collection<ParkingIndexEntry> getAllGarageParkingEntries() {
    return Collections.unmodifiableCollection(findEntriesOfType(parkingIndex.values(), Type.GARAGE));
  }

  /**
   * Returns the ParkingIndexEntry associated with the given Parking.
   * 
   * @param parking Parking to get ParkingIndexEntry for
   * @return ParkingIndexEntry for given Parking
   */
  public ParkingIndexEntry getEntryForParking(Parking parking) {
    return parkingIndex.get(parking.getId());
  }

  /**
   * Returns a collection of ParkingIndexEntry instances with given maximum distance from
   * given reference position.
   * 
   * @param position Reference position
   * @param maxDistance Maximum distance
   * @return Collection of ParkingIndexEntry instances with given maximum distance
   */
  public Collection<ParkingIndexEntry> getParkingsWithMaxDistance(Coordinate position, double maxDistance) {
    return spatialIndex.queryInRange(position, maxDistance);
  }

  /**
   * Determines if given position lies within the boundaries of this instance.
   * 
   * @param pos Position to check
   * @return True if position lies within the boundaries, false otherwise
   */
  public boolean containedInSpatialIndex(Coordinate pos) {
    return spatialIndex.contains(pos);
  }

  /**
   * Returns a ParkingIndexEntry associated with the given Street.
   * 
   * @param street Street to get ParkingIndexEntry for
   * @return ParkingIndexEntry associated with the given Street
   */
  public ParkingIndexEntry getParkingInStreet(Street street) {
    return streetParkingIndex.get(street.getId());
  }

  /**
   * Returns a ParkingIndexEntry associated with the given StreetNode.
   * 
   * @param node StreetNode to get ParkingIndexEntry for
   * @return ParkingIndexEntry associated with the given StreetNode
   */
  public ParkingIndexEntry getParkingAtNode(StreetNode node) {
    return garageParkingIndex.get(node.getId());
  }
  
  /**
   * Returns the total number of garage parking spots.
   * 
   * @return Total number of garage parking spots
   */
  public int getTotalNumberOfGarageParkingSpots() {
    return countTotalNumberOfParkingSpots(findEntriesOfType(parkingIndex.values(), Type.GARAGE));
  }

  /**
   * Returns the total number of street parking spots.
   * 
   * @return Total number of street parking spots
   */
  public int getTotalNumberOfStreetParkingSpots() {
    return countTotalNumberOfParkingSpots(findEntriesOfType(parkingIndex.values(), Type.STREET));
  }
  
  /**
   * Returns the number of free garage parking spots.
   * 
   * @return Number of free garage parking spots
   */
  public int getTotalNumberOfFreeStreetParkingSpots() {
    return countTotalNumberOfFreeParkingSpots(findEntriesOfType(parkingIndex.values(), Type.STREET));
  }

  /**
   * Returns the number of free street parking spots.
   * 
   * @return Number of free street parking spots
   */
  public int getTotalNumberOfFreeGarageParkingSpots() {
    return countTotalNumberOfFreeParkingSpots(findEntriesOfType(parkingIndex.values(), Type.GARAGE));
  }

  private static Collection<ParkingIndexEntry> findEntriesOfType(Collection<ParkingIndexEntry> entries, Type type) {
    ObjectArrayList<ParkingIndexEntry> ret = new ObjectArrayList<>();
    
    for (ParkingIndexEntry entry : entries) {
      
      if (entry.getParking().getType() != type)
        continue;
      ret.add(entry);
    }
    return ret;
  }
  
  private static int countTotalNumberOfParkingSpots(Collection<ParkingIndexEntry> entries) {
    int numberOfSpots = 0;

    for (ParkingIndexEntry entry : entries) {
      numberOfSpots += entry.getParking().getNumberOfParkingSpots();
    }
    return numberOfSpots;
  }

  private static int countTotalNumberOfFreeParkingSpots(Collection<ParkingIndexEntry> entries) {
    int numberOfFreeSpots = 0;

    for (ParkingIndexEntry entry : entries) {
      numberOfFreeSpots += entry.getParking().getNumberOfFreeParkingSpots();
    }
    return numberOfFreeSpots;
  }

  public static ParkingIndex build(ParkingRepository parkingRepository) {
    // Build parking index (mapping id of parking to respective ParkingIndexEntry)
    Int2ObjectMap<ParkingIndexEntry> parkingIndex = buildParkingIndex(parkingRepository);
    
    // Build street parking index (mapping id of streets to respective ParkingIndexEntry)
    Int2ObjectMap<ParkingIndexEntry> streetParkingIndex = buildStreetParkingIndex(parkingRepository.getStreetParking(), parkingIndex);
    
    // Build garage parking index (mapping id of nodes to respective ParkingIndexEntry)
    Int2ObjectMap<ParkingIndexEntry> garageParkingIndex = buildGarageParkingIndex(parkingRepository.getGarageParking(), parkingIndex);

    // Build spatial index (for spatial queries)
    UnmodifiableSpatialIndex<ParkingIndexEntry> spatialIndex = new UnmodifiableSpatialIndex<>(parkingIndex.values());
    return new ParkingIndex(parkingIndex, streetParkingIndex, garageParkingIndex, spatialIndex);
  }
  
  private static Int2ObjectMap<ParkingIndexEntry> buildParkingIndex(ParkingRepository parkingRepository) {
    Int2ObjectMap<ParkingIndexEntry> parkingIndex = new Int2ObjectOpenHashMap<>();
    
    for (Parking parking : parkingRepository.getGarageParking()) {
      GarageParking temp = (GarageParking)parking;
      
      // Get list of access node positions to compute reference position
      List<Coordinate> positions = new ObjectArrayList<>(temp.getAccessNodes().size());
      
      for (StreetNode node : temp.getAccessNodes()) {
        positions.add(node.getPosition());
      }
      // Add a new index entry for current Parking instance
      parkingIndex.put(parking.getId(), new ParkingIndexEntry(parking, positions, getReferencePosition(positions)));
    }
    
    for (Parking parking : parkingRepository.getStreetParking()) {
      StreetParking temp = (StreetParking)parking;
      
      // Collect all unique street node positions to compute reference position
      Collection<Coordinate> positions = new ObjectOpenHashSet<>();
      
      for (Street street : temp.getStreets()) {
        positions.add(street.getStartingNode().getPosition());
        positions.add(street.getEndNode().getPosition());
      }  
      // Add a new index entry for current Parking instance
      parkingIndex.put(parking.getId(), new ParkingIndexEntry(parking, new ObjectArrayList<>(positions), getReferencePosition(positions)));
    }
    return parkingIndex;
  }
  
  private static Int2ObjectMap<ParkingIndexEntry> buildStreetParkingIndex(Collection<Parking> streetParking, 
      Int2ObjectMap<ParkingIndexEntry> parkingIndex) {
    Int2ObjectMap<ParkingIndexEntry> streetParkingIndex = new Int2ObjectOpenHashMap<>();
    
    for (Parking parking : streetParking) {
      // Get ParkingIndexEntry of StreetParking instance and link it to street ids
      ParkingIndexEntry entry = parkingIndex.get(parking.getId());
      StreetParking temp = (StreetParking)parking;
      
      for (Street street : temp.getStreets()) {
        streetParkingIndex.put(street.getId(), entry);
      }
    } 
    return streetParkingIndex;
  }
  
  private static Int2ObjectMap<ParkingIndexEntry> buildGarageParkingIndex(Collection<Parking> garageParking, 
      Int2ObjectMap<ParkingIndexEntry> parkingIndex) {
    Int2ObjectMap<ParkingIndexEntry> garageParkingIndex = new Int2ObjectOpenHashMap<>();
    
    for (Parking parking : garageParking) {
      // Get ParkingIndexEntry of GargageParking instance and link it to node ids
      ParkingIndexEntry entry = parkingIndex.get(parking.getId());
      GarageParking temp = (GarageParking)parking;
      
      for (StreetNode node : temp.getAccessNodes()) {
        garageParkingIndex.put(node.getId(), entry);
      }
    } 
    return garageParkingIndex;
  }
  
  private static Coordinate getReferencePosition(Collection<Coordinate> positions) {
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