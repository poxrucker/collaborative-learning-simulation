package de.dfki.simulation;

import java.util.Map;

import allow.simulator.core.Context;

public abstract class AbstractSimulationModel {

  public abstract void setup(Map<String, Object> parameters) throws Exception;
  
  public abstract void tick() throws Exception;
  
  public abstract void finish() throws Exception;
  
  public abstract Context getContext();
  
}
