package de.dfki.parking.simulation;

import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.entity.Person;
import de.dfki.parking.behavior.baseline.BaselineExplorationStrategy;
import de.dfki.parking.behavior.baseline.BaselineSelectionStrategy;
import de.dfki.parking.behavior.guidance.GuidanceSystem;
import de.dfki.parking.behavior.guidance.GuidanceSystemSelectionStrategy;
import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.knowledge.ParkingKnowledge;
import de.dfki.parking.knowledge.ParkingKnowledgeFactory;
import de.dfki.parking.utility.ParkingPreferences;
import de.dfki.parking.utility.ParkingPreferencesFactory;
import de.dfki.parking.utility.ParkingUtility;

public final class GuidanceSystemModelInitializer implements IParkingModelInitializer {
  // Creates ParkingKnowledge instances
  private final ParkingKnowledgeFactory knowledgeFactory;

  // Creates ParkingPreference instances for different profiles
  private final ParkingPreferencesFactory prefsFactory;

  // ParkingIndex index instance
  private final ParkingIndex parkingIndex;

  // Global knowledge instance
  private final GuidanceSystem guidanceSystem;

  // Model parameters
  private final long validTime;
  private final double percentUsers;
  private final double percentSensorCars;

  public GuidanceSystemModelInitializer(ParkingKnowledgeFactory knowledgeFactory, ParkingPreferencesFactory prefsFactory, ParkingIndex parkingIndex,
      GuidanceSystem guidanceSystem, long validTime, double percentUsers, double percentSensorCars) {
    this.knowledgeFactory = knowledgeFactory;
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

    // Initialize local knowledge
    ParkingKnowledge localKnowledge = knowledgeFactory.createWithGarages();
    person.setLocalParkingKnowledge(localKnowledge);
    
    // Initialize parking preferences
    ParkingPreferences prefs = prefsFactory.createFromProfile(person.getProfile());
    person.setParkingPreferences(prefs);
    
    // Initialize parking utility
    ParkingUtility utility = new ParkingUtility();
    person.setParkingUtility(utility);

    // Initialize parking strategies for users and non-users
    if (ThreadLocalRandom.current().nextDouble() < percentUsers) {
      person.setUser();
      person.setParkingSelectionStrategy(new GuidanceSystemSelectionStrategy(prefs, utility, guidanceSystem));
      person.setExplorationStrategy(new BaselineExplorationStrategy(localKnowledge, prefs, utility, parkingIndex, validTime));

      // Determine is person has a sensor car
      if (ThreadLocalRandom.current().nextDouble() < percentSensorCars)
        person.setHasSensorCar();

    } else {
      person.setParkingSelectionStrategy(new BaselineSelectionStrategy(localKnowledge, prefs, utility, validTime));
      person.setExplorationStrategy(new BaselineExplorationStrategy(localKnowledge, prefs, utility, parkingIndex, validTime));
    }
  }
}