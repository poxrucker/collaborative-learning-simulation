package allow.simulator.parking;

import java.util.Collection;
import java.util.Map;

import allow.simulator.util.Coordinate;
import allow.simulator.world.Street;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class ParkingMap {

  public final class ParkingMapEntry {
    private Coordinate position;
    private int nParkingSpots;
    private int nFreeParkingSpots;
    private long lastUpdate;
    
    public ParkingMapEntry(Coordinate position,
        int nParkingSpots,
        int nFreeParkingSpots,
        long lastUpdate) {
      this.position = position;
      this.nParkingSpots = nParkingSpots;
      this.nFreeParkingSpots = nFreeParkingSpots;
      this.lastUpdate = lastUpdate;
    }
    
    public ParkingMapEntry(Coordinate position,
        int nParkingSpots,
        int nFreeParkingSpots) {
      this(position, nFreeParkingSpots, nFreeParkingSpots, -1);
    }
    
    public Coordinate getPosition() {
      return position;
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
  
  private final Map<String, ParkingMapEntry> dataMap;
  private final ParkingGrid spatialMap;
  
  public ParkingMap(double[] areaBounds, int nRows, int nCols) {
    this.dataMap = new Object2ObjectOpenHashMap<>();
    this.spatialMap = new ParkingGrid(areaBounds, nRows, nCols);
  }
  
  public void update(Street street, int nParkingSpots, int nFreeParkingSpots, long ts) {
    // Try to find existing entry in parking map
    ParkingMapEntry entry = null;
    
    // First, try normal key
    String key = getKey(street);
    entry = dataMap.get(key);
    
    if (entry != null) {
      entry.updateEntry(nFreeParkingSpots, ts);
      return;
    }
    
    // If normal key was not found, try reversed key
    String reversedKey = getReverseKey(street);
    entry = dataMap.get(reversedKey);
    
    if (entry != null) {
      entry.updateEntry(nFreeParkingSpots, ts);
      return;
    }
    
    // If none of the keys is contained in the map, add new entry to data map and spatial index
    entry = new ParkingMapEntry(street.getEndNode().getPosition(), nParkingSpots, nFreeParkingSpots, ts);
    dataMap.put(key, entry);
    
    Coordinate pos = street.getEndNode().getPosition();
    spatialMap.insert(pos, entry);
  }
  
  public Collection<ParkingMapEntry> findPossibleParking(Coordinate position, double maxDistance) {
    return spatialMap.query(position, maxDistance);
  }
  
  private static String getKey(Street street) {
    return street.getStartingNode().getLabel() + "->" + street.getEndNode().getLabel();
  }
  
  private static String getReverseKey(Street street) {
    return street.getEndNode().getLabel() + "->" + street.getStartingNode().getLabel();
  }
}
