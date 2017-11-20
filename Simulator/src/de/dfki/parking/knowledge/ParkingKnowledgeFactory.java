package de.dfki.parking.knowledge;

import java.util.Collection;

import de.dfki.parking.model.Parking;
import de.dfki.parking.model.ParkingIndex;
import de.dfki.parking.model.ParkingIndex.ParkingIndexEntry;

public final class ParkingKnowledgeFactory {

  private final ParkingIndex parkingIndex;
  
  public ParkingKnowledgeFactory(ParkingIndex parkingIndex) {
    this.parkingIndex = parkingIndex;
  }
  
  public ParkingKnowledge createEmpty() {
    return new ParkingKnowledge(parkingIndex);
  }
  
  public ParkingKnowledge createWithGarages() {
    ParkingKnowledge ret = new ParkingKnowledge(parkingIndex);
    Collection<ParkingIndexEntry> entries = parkingIndex.getAllGarageParkingEntries();
    
    for (ParkingIndexEntry entry : entries) {
      Parking p = entry.getParking();
      ret.update(p, p.getNumberOfParkingSpots(), p.getNumberOfFreeParkingSpots(), p.getCurrentPricePerHour(), -1);
    }
    return ret;
  }
  
  public ParkingKnowledge createFull() {
    ParkingKnowledge ret = new ParkingKnowledge(parkingIndex);
    Collection<ParkingIndexEntry> entries = parkingIndex.getAllParkings();
    
    for (ParkingIndexEntry entry : entries) {
      Parking p = entry.getParking();
      ret.update(p, p.getNumberOfParkingSpots(), p.getNumberOfFreeParkingSpots(), p.getCurrentPricePerHour(), -1);
    }
    return ret;
  }
}
