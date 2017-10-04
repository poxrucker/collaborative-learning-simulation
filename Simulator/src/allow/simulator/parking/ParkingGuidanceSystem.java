package allow.simulator.parking;

public final class ParkingGuidanceSystem {
  // Parking map instance shared by guidance system and users
  private final ParkingMap parkingMap;
  
  public ParkingGuidanceSystem(ParkingMap parkingMap) {
    this.parkingMap = parkingMap;
  }
}
