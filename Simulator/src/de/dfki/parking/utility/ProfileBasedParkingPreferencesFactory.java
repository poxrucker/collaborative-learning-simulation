package de.dfki.parking.utility;

import allow.simulator.entity.Profile;

public final class ProfileBasedParkingPreferencesFactory {
  // Models to sample profile dependent preferences
  private final ParkingPreferencesModel workerModel;  
  private final ParkingPreferencesModel studentModel;
  private final ParkingPreferencesModel homemakerModel;
  
  public ProfileBasedParkingPreferencesFactory(ParkingPreferencesModel workerModel,
      ParkingPreferencesModel studentModel, 
      ParkingPreferencesModel homemakerModel) {
    this.workerModel = workerModel;
    this.studentModel = studentModel;
    this.homemakerModel = homemakerModel;
  } 
  
  public ParkingPreferences create(Profile profile) {
      
    switch (profile) {
    
    case HOMEMAKER:
      return sampleFromModel(homemakerModel);

    case STUDENT:
      return sampleFromModel(studentModel);
      
    case WORKER:
      return sampleFromModel(workerModel);
      
    default:
      throw new IllegalArgumentException("Illegal profile");
    }
  }
  
  private static ParkingPreferences sampleFromModel(ParkingPreferencesModel model) {
    double cweight = model.sampleCWeight();
    double wdweight = model.sampleWdWeight();
    double stweight = model.sampleStWeight();
    double cmax = model.sampleCMax();
    double wdmax = model.sampleWdMax();
    double stmax = model.sampleStMax(); 
    return new ParkingPreferences(cweight, wdweight, stweight, cmax, wdmax, stmax);
  }
}