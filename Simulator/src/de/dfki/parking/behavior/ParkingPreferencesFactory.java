package de.dfki.parking.behavior;

import allow.simulator.entity.Profile;

public final class ParkingPreferencesFactory {

  public ParkingPreferences createFromProfile(Profile profile) {
    
    switch (profile) {
    
    case HOMEMAKER:
      return createHomemakerPreferences();

    case STUDENT:
      return createStudentPreferences();
      
    case WORKER:
      return createWorkerPreferences();
      
    default:
      throw new IllegalArgumentException("Illegal profile");
    }
  }
  
  private ParkingPreferences createHomemakerPreferences() {
    double cweight = 35.0;
    double wdweight = 50.0;
    double stweight = 15.0;
    double cmax = 2.8;
    double wdmax = 300;
    double stmax = 8 * 60; 
    return new ParkingPreferences(cweight, wdweight, stweight, cmax, wdmax, stmax);
  }
  
  private ParkingPreferences createStudentPreferences() {
    double cweight = 50.0;
    double wdweight = 15.0;
    double stweight = 35.0;
    double cmax = 2.8;
    double wdmax = 300;
    double stmax = 8 * 60; 
    return new ParkingPreferences(cweight, wdweight, stweight, cmax, wdmax, stmax);
  }
  
  private ParkingPreferences createWorkerPreferences() {
    double cweight = 15.0;
    double wdweight = 35.0;
    double stweight = 50.0;
    double cmax = 2.8;
    double wdmax = 300;
    double stmax = 8 * 60; 
    return new ParkingPreferences(cweight, wdweight, stweight, cmax, wdmax, stmax);
  }
}
