package de.dfki.parking.simulation;

import allow.simulator.entity.Person;
import de.dfki.parking.behavior.ParkingBehavior;
import de.dfki.parking.behavior.baseline.BaselineExplorationStrategy;
import de.dfki.parking.behavior.baseline.BaselineInitializationStrategy;
import de.dfki.parking.behavior.baseline.BaselineSelectionStrategy;
import de.dfki.parking.behavior.baseline.BaselineUpdateStrategy;
import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.knowledge.ParkingMap;
import de.dfki.parking.knowledge.ParkingMapFactory;
import de.dfki.parking.model.ParkingState;
import de.dfki.parking.utility.ParkingPreferencesFactory;
import de.dfki.parking.utility.ParkingUtility;

public final class BaselineParkingModelInitializer implements IParkingModelInitializer {
  // Creates ParkingMap instances
  private final ParkingMapFactory parkingMapFactory;
  
  // Creates ParkingPreference instances for different profiles 
  private final ParkingPreferencesFactory prefsFactory;
  
  // ParkingIndex index instance 
  private final ParkingIndex parkingIndex;
   
  // Simulation parameters
  private final long validTime;
  
  public BaselineParkingModelInitializer(ParkingMapFactory knowledgeFactory, 
      ParkingPreferencesFactory prefsFactory, ParkingIndex parkingIndex, long validTime) {
    this.parkingMapFactory = knowledgeFactory;
    this.prefsFactory = prefsFactory;
    this.parkingIndex = parkingIndex;
    this.validTime = validTime;
  }
  
  @Override
  public void initializePerson(Person person) {
    if (!person.hasCar())
      return; // If person does not have a car, there is nothing to do

    // Initialize parking state
    ParkingState parkingState = new ParkingState();
    parkingState.setParkingPreferences(prefsFactory.createFromProfile(person.getProfile()));
    parkingState.setParkingUtility(new ParkingUtility());
    
    // Initialize local map
    ParkingMap localMap = parkingMapFactory.createWithGarages();
    
    // Initialize parking behavior
    ParkingBehavior parkingBehavior = new ParkingBehavior(new BaselineInitializationStrategy(), 
        new BaselineSelectionStrategy(localMap, parkingState.getParkingPreferences(), parkingState.getParkingUtility(), validTime),
        new BaselineExplorationStrategy(localMap, parkingState.getParkingPreferences(), parkingState.getParkingUtility(), parkingIndex, validTime),
        new BaselineUpdateStrategy(localMap));
    
    // Set state and behavior
    person.setParkingBehavior(parkingBehavior);
    person.setParkingState(parkingState);
  }
}