package de.dfki.parking.simulation;

import allow.simulator.entity.Person;
import de.dfki.parking.behavior.baseline.BaselineExplorationStrategy;
import de.dfki.parking.behavior.baseline.BaselineSelectionStrategy;
import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.knowledge.ParkingKnowledge;
import de.dfki.parking.knowledge.ParkingKnowledgeFactory;
import de.dfki.parking.utility.ParkingPreferences;
import de.dfki.parking.utility.ParkingPreferencesFactory;
import de.dfki.parking.utility.ParkingUtility;

public final class BaselineParkingModelInitializer implements IParkingModelInitializer {
  // Creates ParkingKnowledge instances
  private final ParkingKnowledgeFactory knowledgeFactory;
  
  // Creates ParkingPreference instances for different profiles 
  private final ParkingPreferencesFactory prefsFactory;
  
  // ParkingIndex index instance 
  private final ParkingIndex parkingIndex;
   
  // Simulation parameters
  private final long validTime;
  
  public BaselineParkingModelInitializer(ParkingKnowledgeFactory knowledgeFactory, 
      ParkingPreferencesFactory prefsFactory, ParkingIndex parkingIndex, long validTime) {
    this.knowledgeFactory = knowledgeFactory;
    this.prefsFactory = prefsFactory;
    this.parkingIndex = parkingIndex;
    this.validTime = validTime;
  }
  
  @Override
  public void initializePerson(Person person) {
    if (!person.hasCar())
      return; // If person does not have a car, there is nothing to do

    // Initialize local knowledge
    ParkingKnowledge knowledge = knowledgeFactory.createWithGarages();
    person.setLocalParkingKnowledge(knowledge);     
    
    // Initialize parking preferences
    ParkingPreferences prefs = prefsFactory.createFromProfile(person.getProfile());
    person.setParkingPreferences(prefs);
    
    // Initialize parking utility
    ParkingUtility utility = new ParkingUtility();
    person.setParkingUtility(utility);
   
    // Initialize parking strategies
    person.setParkingSelectionStrategy(new BaselineSelectionStrategy(knowledge, prefs, utility, validTime));
    person.setExplorationStrategy(new BaselineExplorationStrategy(knowledge, prefs, utility, parkingIndex, validTime));   
  }
}