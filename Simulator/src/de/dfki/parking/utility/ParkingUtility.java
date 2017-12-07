package de.dfki.parking.utility;

import allow.simulator.utility.IUtility;

public class ParkingUtility implements IUtility<ParkingParameters, ParkingPreferences> {

  @Override
  public double computeUtility(ParkingParameters input, ParkingPreferences preferences) {
    double c = Math.min(input.getC(), preferences.getCMax());
    double wd = Math.min(input.getWd(), preferences.getWdMax());
    double st =  Math.min(input.getSt(), preferences.getStMax());   
    double cfc = preferences.getCWeight() * (1 - c / preferences.getCMax());
    double cfwd = preferences.getWdWeight() * (1 - wd / preferences.getWdMax());
    double cfst = preferences.getStWeight() * (1 - st / preferences.getStMax());
    return Math.max(cfc + cfwd + cfst, 0);
  }

  @Override
  public boolean ascendingOrder() {
    return false;
  }
}
