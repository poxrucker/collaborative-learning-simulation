package de.dfki.parking.model;

import de.dfki.parking.utility.ParkingPreferences;
import de.dfki.parking.utility.ParkingUtility;

public final class ParkingState {
  // State variables for parking spot search simulation
  private boolean isUser;
  private boolean hasSensorCar;
  private Parking currentParking;
  private long searchStartTime;
  private long searchEndTime;
  private ParkingUtility parkingUtility;
  private ParkingPreferences parkingPreferences;
  private int parkingReservationId;
  private ParkingPossibility parkingCandidate;
  
  public boolean isUser() {
    return isUser;
  }
  
  public void setUser(boolean isUser) {
    this.isUser = isUser;
  }
  
  public boolean hasSensorCar() {
    return hasSensorCar;
  }
  
  public void setHasSensorCar(boolean hasSensorCar) {
    this.hasSensorCar = hasSensorCar;
  }
  
  public Parking getCurrentParking() {
    return currentParking;
  }
  
  public void setCurrentParking(Parking currentParking) {
    this.currentParking = currentParking;
  }
  
  public ParkingUtility getParkingUtility() {
    return parkingUtility;
  }
  
  public void setParkingUtility(ParkingUtility utility) {
    this.parkingUtility = utility;
  }
  
  public ParkingPreferences getParkingPreferences() {
    return parkingPreferences;
  }
  
  public void setParkingPreferences(ParkingPreferences preferences) {
    this.parkingPreferences = preferences;
  }
  
  public long getSearchStartTime() {
    return searchStartTime;
  }
  
  public void setSearchStartTime(long searchStartTime) {
    this.searchStartTime = searchStartTime;
  }
  
  public long getSearchEndTime() {
    return searchEndTime;
  }
  
  public void setSearchEndTime(long searchEndTime) {
    this.searchEndTime = searchEndTime;
  }
  
  public int getParkingReservationId() {
    return parkingReservationId;
  }
  
  public void setParkingReservationId(int parkingReservationId) {
    this.parkingReservationId = parkingReservationId;
  }
  
  public ParkingPossibility getParkingCandidate() {
    return parkingCandidate;
  }
  
  public void setParkingCandidate(ParkingPossibility parkingCandidate) {
    this.parkingCandidate = parkingCandidate;
  }
}
