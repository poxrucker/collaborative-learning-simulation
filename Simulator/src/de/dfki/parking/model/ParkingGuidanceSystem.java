package de.dfki.parking.model;

import de.dfki.parking.knowledge.ParkingKnowledge;

public final class ParkingGuidanceSystem {
  // Parking map instance shared by guidance system and users
  private final ParkingMap parkingMap;
  
  private final ParkingKnowledge knowledge;
  
  public ParkingGuidanceSystem(ParkingMap parkingMap, ParkingKnowledge knowledge) {
    this.parkingMap = parkingMap;
    this.knowledge = knowledge;
  }
}
