package de.dfki.parking.simulation;

import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.entity.Person;
import de.dfki.parking.behavior.ParkingBehavior;
import de.dfki.parking.behavior.baseline.BaselineExplorationStrategy;
import de.dfki.parking.behavior.baseline.BaselineInitializationStrategy;
import de.dfki.parking.behavior.baseline.BaselineSelectionStrategy;
import de.dfki.parking.behavior.baseline.BaselineUpdateStrategy;
import de.dfki.parking.behavior.guidance.GuidanceSystemInitializationStrategy;
import de.dfki.parking.behavior.guidance.GuidanceSystemSelectionStrategy;
import de.dfki.parking.behavior.guidance.GuidanceSystemUpdateStrategy;
import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.knowledge.ParkingMap;
import de.dfki.parking.knowledge.ParkingMapFactory;
import de.dfki.parking.model.GuidanceSystem;
import de.dfki.parking.model.ParkingState;
import de.dfki.parking.utility.ProfileBasedParkingPreferencesFactory;
import de.dfki.parking.utility.ParkingUtility;

public final class GuidanceSystemModelInitializer implements IParkingModelInitializer {
  // Creates ParkingMap instances
  private final ParkingMapFactory parkingMapFactory;

  // Creates ParkingPreference instances for different profiles
  private final ProfileBasedParkingPreferencesFactory prefsFactory;

  // ParkingIndex index instance
  private final ParkingIndex parkingIndex;

  // GuidanceSystem instance to use for parking spot assignments
  private final GuidanceSystem guidanceSystem;

  // Model parameters
  private final long validTime;
  private final double percentUsers;
  private final double percentSensorCars;

  public GuidanceSystemModelInitializer(ParkingMapFactory knowledgeFactory, ProfileBasedParkingPreferencesFactory prefsFactory, ParkingIndex parkingIndex,
      GuidanceSystem guidanceSystem, long validTime, double percentUsers, double percentSensorCars) {
    this.parkingMapFactory = knowledgeFactory;
    this.prefsFactory = prefsFactory;
    this.parkingIndex = parkingIndex;
    this.guidanceSystem = guidanceSystem;
    this.validTime = validTime;
    this.percentUsers = percentUsers;
    this.percentSensorCars = percentSensorCars;
  }

  @Override
  public void initializePerson(Person person) {
    if (!person.hasCar())
      return; // If person does not have a car, there is nothing to do

    // Initialize parking state
    ParkingState parkingState = new ParkingState();
    parkingState.setParkingPreferences(prefsFactory.create(person.getProfile()));
    parkingState.setParkingUtility(new ParkingUtility());

    // Initialize local map
    ParkingMap localMap = parkingMapFactory.createWithGarages();
    
    // Initialize parking strategies for users and non-users
    ParkingBehavior parkingBehavior;
    
    if (ThreadLocalRandom.current().nextDouble() < percentUsers) {
      parkingState.setUser(true);
      
      if (ThreadLocalRandom.current().nextDouble() < percentSensorCars)
        parkingState.setHasSensorCar(true);
      
      parkingBehavior = new ParkingBehavior(new GuidanceSystemInitializationStrategy(guidanceSystem, parkingState.getParkingUtility(), parkingState.getParkingPreferences()), 
          new GuidanceSystemSelectionStrategy(parkingState.getParkingPreferences(), parkingState.getParkingUtility(), guidanceSystem),
          new BaselineExplorationStrategy(localMap, parkingState.getParkingPreferences(), parkingState.getParkingUtility(), parkingIndex, validTime),
          new GuidanceSystemUpdateStrategy(localMap, guidanceSystem, parkingState.hasSensorCar()));
      person.setParkingBehavior(parkingBehavior);
     
    } else {     
      parkingBehavior = new ParkingBehavior(new BaselineInitializationStrategy(), 
          new BaselineSelectionStrategy(localMap, parkingState.getParkingPreferences(), parkingState.getParkingUtility(), validTime),
          new BaselineExplorationStrategy(localMap, parkingState.getParkingPreferences(), parkingState.getParkingUtility(), parkingIndex, validTime),
          new BaselineUpdateStrategy(localMap));
      person.setParkingBehavior(parkingBehavior);
    }
    // Set state and behavior
    person.setParkingState(parkingState);
    person.setParkingBehavior(parkingBehavior);
  }
}