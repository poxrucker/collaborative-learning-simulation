package de.dfki.parking.knowledge;

import de.dfki.parking.index.ParkingIndexEntry;

public final class ParkingKnowledgeEntry {
  // Referenced parking index entry
  private ParkingIndexEntry parkingIndexEntry;

  // Number of parking spots
  private int nParkingSpots;

  // Number of free parking spots as known at last update time
  private int nFreeParkingSpots;

  // Price per hour as known at last update time
  private double pricePerHour;

  // Last time this entry was updated
  private long lastUpdate;

  public ParkingKnowledgeEntry(ParkingIndexEntry parkingMapEntry, int nParkingSpots, int nFreeParkingSpots, double pricePerHour, long lastUpdate) {
    this.parkingIndexEntry = parkingMapEntry;
    this.nParkingSpots = nFreeParkingSpots;
    this.nFreeParkingSpots = nFreeParkingSpots;
    this.pricePerHour = pricePerHour;
    this.lastUpdate = lastUpdate;
  }

  /**
   * Returns the referenced ParkingIndexEntry.
   * 
   * @return Referenced ParkingIndexEntry
   */
  public ParkingIndexEntry getParkingIndexEntry() {
    return parkingIndexEntry;
  }

  /**
   * Returns the total number of parking spots.
   * 
   * @return Total number parking spots
   */
  public int getNParkingSpots() {
    return nParkingSpots;
  }

  /**
   * Returns the number of free parking spots as known at last update time.
   * 
   * @return Number of free parking spots
   */
  public int getNFreeParkingSpots() {
    return nFreeParkingSpots;
  }

  /**
   * Returns the price per hour as known at last update time.
   * 
   * @return Price per hour
   */
  public double getPricePerHour() {
    return pricePerHour;
  }

  /**
   * Returns the timestamp the entry was updated last.
   * 
   * @return Last update time
   */
  public long getLastUpdate() {
    return lastUpdate;
  }

  /**
   * Updates the number of free parking spots at time ts (ms).
   * 
   * @param nFreeParkingSpots Number of free parking spots
   * @param ts Timestamp (ms) 
   */
  public void update(int nFreeParkingSpots, long ts) {
    this.nFreeParkingSpots = nFreeParkingSpots;
    this.lastUpdate = ts;
  }
}
