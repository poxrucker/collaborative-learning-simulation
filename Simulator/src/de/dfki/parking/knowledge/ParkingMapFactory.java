package de.dfki.parking.knowledge;

import java.util.Collection;

import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.index.ParkingIndexEntry;
import de.dfki.parking.model.Parking;

public final class ParkingMapFactory {

  private final ParkingIndex parkingIndex;
  
  public ParkingMapFactory(ParkingIndex parkingIndex) {
    this.parkingIndex = parkingIndex;
  }
  
  public ParkingMap createEmpty() {
    return new ParkingMap(parkingIndex);
  }
  
  public ParkingMap createWithGarages() {
    ParkingMap ret = new ParkingMap(parkingIndex);
    Collection<ParkingIndexEntry> entries = parkingIndex.getAllGarageParkingEntries();
    return initialize(ret, entries);
  }
  
  public ParkingMap createFull() {
    ParkingMap ret = new ParkingMap(parkingIndex);
    Collection<ParkingIndexEntry> entries = parkingIndex.getAllEntries();
    return initialize(ret, entries);
  }
  
  private static ParkingMap initialize(ParkingMap knowledge, Collection<ParkingIndexEntry> entries) {
    
    for (ParkingIndexEntry entry : entries) {
      Parking p = entry.getParking();
      knowledge.update(p, p.getNumberOfParkingSpots(), p.getNumberOfFreeParkingSpots(), p.getCurrentPricePerHour(), -1);
    }
    return knowledge;
  }
}
