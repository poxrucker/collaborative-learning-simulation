package de.dfki.parking.knowledge;

import java.util.List;

import allow.simulator.util.Coordinate;
import allow.simulator.world.Street;
import de.dfki.parking.model.Parking;
import de.dfki.parking.model.ParkingIndex;
import de.dfki.parking.model.ParkingIndex.ParkingIndexEntry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class ParkingKnowledge {
  
  public static final class ParkingKnowledgeEntry {
    // Parking possibility
    private ParkingIndexEntry parkingMapEntry;
   
    // Number of parking spots as known to an entity
    private int nParkingSpots;
    
    // Number of free parking spots as known to an entity
    private int nFreeParkingSpots;
    
    // Last time this entry was updated
    private long lastUpdate;
    
    public ParkingKnowledgeEntry(ParkingIndexEntry parkingMapEntry,
        int nParkingSpots,
        int nFreeParkingSpots,
        long lastUpdate) {
      this.parkingMapEntry = parkingMapEntry;
      this.nParkingSpots = nParkingSpots;
      this.nFreeParkingSpots = nFreeParkingSpots;
      this.lastUpdate = lastUpdate;
    }
    
    public ParkingKnowledgeEntry(ParkingIndexEntry parkingMapEntry,
        int nParkingSpots,
        int nFreeParkingSpots) {
      this(parkingMapEntry, nFreeParkingSpots, nFreeParkingSpots, -1);
    }
    
    public ParkingIndexEntry getParkingMapEntry() {
      return parkingMapEntry;
    }
    
    public int getNParkingSpots() {
      return nParkingSpots;
    }
    
    public int getNFreeParkingSpots() {
      return nFreeParkingSpots;
    }
    
    public long getLastUpdate() {
      return lastUpdate;
    }
    
    public void updateEntry(int nFreeParkingSpots, long ts) {
      this.nFreeParkingSpots = nFreeParkingSpots;
      this.lastUpdate = ts;
    }
  }

  private final ParkingIndex parkingMap;
  private final Int2ObjectMap<ParkingKnowledgeEntry> parkingKnowledge;
  
  public ParkingKnowledge(ParkingIndex parkingMap) {
    this.parkingMap = parkingMap;
    this.parkingKnowledge = new Int2ObjectOpenHashMap<>();
  }
  
  public void update(Parking parking, int nParkingSpots, int nFreeParkingSpots, long time) {
    // Check if entry is known
    ParkingKnowledgeEntry ret = parkingKnowledge.get(parking.getId());
    
    if (ret == null) {
      // Get ParkingMapEntry from ParkingMap
      ParkingIndexEntry entry = parkingMap.getForParking(parking);
      ret = new ParkingKnowledgeEntry(entry, nParkingSpots, nFreeParkingSpots);
      parkingKnowledge.put(parking.getId(), ret);
    }
    ret.updateEntry(nFreeParkingSpots, time);
  }
  
  public List<ParkingKnowledgeEntry> findParkingInStreet(Street street) {
    List<ParkingKnowledgeEntry> ret = new ObjectArrayList<>();

    // Get parking possibilities in street from ParkingMap
    List<ParkingIndexEntry> entries = parkingMap.getParkingsInStreet(street);
    
    if (entries == null)
      return ret;
    
    // Filter entries which are unknown using parking knowledge mapping
    for (ParkingIndexEntry entry : entries) {
      ParkingKnowledgeEntry temp = parkingKnowledge.get(entry.getParking().getId());
      
      if (temp == null)
        continue;
      ret.add(temp);
    }
    return ret;
  }
  
  public List<ParkingKnowledgeEntry> findParkingNearby(Coordinate position, double maxDistance) {
    // Get nearby parking possibilities from ParkingMap
    List<ParkingIndexEntry> entries = parkingMap.getParkingsWithMaxDistance(position, maxDistance);
    
    // Filter entries which are unknown using parking knowledge mapping
    List<ParkingKnowledgeEntry> ret = new ObjectArrayList<>();
    
    for (ParkingIndexEntry entry : entries) {
      ParkingKnowledgeEntry temp = parkingKnowledge.get(entry.getParking().getId());
      
      if (temp == null)
        continue;
      ret.add(temp);
    }
    return ret;
  }  
}
