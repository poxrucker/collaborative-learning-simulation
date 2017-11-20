package de.dfki.parking.behavior;

import allow.simulator.util.Triple;
import allow.simulator.utility.IUtility;

public class ParkingUtility implements IUtility<Triple<Double, Double, Double>, ParkingPreferences> {

  @Override
  public double computeUtility(Triple<Double, Double, Double> input, ParkingPreferences preferences) {
    double c = Math.min(input.first, preferences.getCmax());
    double wd = Math.min(input.second, preferences.getWdmax());
    double st =  Math.min(input.third, preferences.getStmax());   
    double cfc = preferences.getCweight() * (1 - c / preferences.getCmax());
    double cfwd = preferences.getWdweight() * (1 - wd / preferences.getWdmax());
    double cfst = preferences.getStweight() * (1 - st / preferences.getStmax());
    return Math.max(cfc + cfwd + cfst, 0);
  }

  @Override
  public boolean ascendingOrder() {
    return false;
  }
}
