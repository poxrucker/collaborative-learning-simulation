package de.dfki.parking.exploration;

import allow.simulator.util.Coordinate;
import de.dfki.parking.knowledge.ParkingKnowledge;
import de.dfki.parking.model.ParkingMap;

public class MapDisplayParkingSearchStrategy implements IExplorationStrategy {

  private ParkingKnowledge localKnowledge;
  private ParkingKnowledge globalKnowledge;
  private ParkingMap parkingMap;
  
  @Override
  public Coordinate findNextPossibleParking(Coordinate position, Coordinate destination, long currentTime) {
    // TODO Auto-generated method stub
    return null;
  }

}
