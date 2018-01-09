package de.dfki.parking.behavior;


public final class ParkingBehavior {

  private final IInitializationStrategy initializationStrategy;
  private final IParkingSelectionStrategy selectionStrategy;
  private final IExplorationStrategy explorationStrategy;
  private final IUpdateStrategy updateStrategy;
  
  public ParkingBehavior(IInitializationStrategy initializationStrategy,
      IParkingSelectionStrategy selectionStrategy,
      IExplorationStrategy explorationStrategy,
      IUpdateStrategy updateStrategy) {
    this.initializationStrategy = initializationStrategy;
    this.selectionStrategy = selectionStrategy;
    this.explorationStrategy = explorationStrategy;
    this.updateStrategy = updateStrategy;
  }
  
  public IInitializationStrategy getInitializationStrategy() {
    return initializationStrategy;
  }
  
  public IParkingSelectionStrategy getSelectionStrategy() {
    return selectionStrategy;
  }
  
  public IExplorationStrategy getExplorationStrategy() {
    return explorationStrategy;
  }
  
  public IUpdateStrategy getUpdateStrategy() {
    return updateStrategy;
  }
}
