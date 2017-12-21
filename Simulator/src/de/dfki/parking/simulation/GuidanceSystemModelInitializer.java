package de.dfki.parking.simulation;

import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.entity.Person;
import de.dfki.parking.behavior.baseline.BaselineExplorationStrategy;
import de.dfki.parking.behavior.baseline.BaselineSelectionStrategy;
import de.dfki.parking.behavior.baseline.BaselineUpdateStrategy;
import de.dfki.parking.behavior.guidance.GuidanceSystemSelectionStrategy;
import de.dfki.parking.behavior.guidance.GuidanceSystemUpdateStrategy;
import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.knowledge.ParkingMap;
import de.dfki.parking.knowledge.ParkingMapFactory;
import de.dfki.parking.model.GuidanceSystem;
import de.dfki.parking.utility.ParkingPreferences;
import de.dfki.parking.utility.ParkingPreferencesFactory;
import de.dfki.parking.utility.ParkingUtility;

public final class GuidanceSystemModelInitializer implements IParkingModelInitializer {
  // Creates ParkingMap instances
  private final ParkingMapFactory parkingMapFactory;

  // Creates ParkingPreference instances for different profiles
  private final ParkingPreferencesFactory prefsFactory;

  // ParkingIndex index instance
  private final ParkingIndex parkingIndex;

  // GuidanceSystem instance to use for parking spot assignments
  private final GuidanceSystem guidanceSystem;

  // Model parameters
  private final long validTime;
  private final double percentUsers;
  private final double percentSensorCars;

  public GuidanceSystemModelInitializer(ParkingMapFactory knowledgeFactory, ParkingPreferencesFactory prefsFactory, ParkingIndex parkingIndex,
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

    // Initialize parking preferences
    ParkingPreferences prefs = prefsFactory.createFromProfile(person.getProfile());
    person.setParkingPreferences(prefs);
    
    // Initialize parking utility
    ParkingUtility utility = new ParkingUtility();
    person.setParkingUtility(utility);

    // Initialize local map
    ParkingMap localMap = parkingMapFactory.createWithGarages();
    
    // Initialize parking strategies for users and non-users
    if (ThreadLocalRandom.current().nextDouble() < percentUsers) {
      person.setUser();
      
      if (ThreadLocalRandom.current().nextDouble() < percentSensorCars)
        person.setHasSensorCar();
      
      person.setParkingSelectionStrategy(new GuidanceSystemSelectionStrategy(prefs, utility, guidanceSystem));
      person.setExplorationStrategy(new BaselineExplorationStrategy(localMap, prefs, utility, parkingIndex, validTime));
      person.setUpdateStrategy(new GuidanceSystemUpdateStrategy(localMap, guidanceSystem, person.hasSensorCar()));
      
    } else {
      person.setParkingSelectionStrategy(new BaselineSelectionStrategy(localMap, prefs, utility, validTime));
      person.setExplorationStrategy(new BaselineExplorationStrategy(localMap, prefs, utility, parkingIndex, validTime));
      person.setUpdateStrategy(new BaselineUpdateStrategy(localMap));
    }
  }
}