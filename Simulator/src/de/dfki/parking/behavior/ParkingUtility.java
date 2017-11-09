package de.dfki.parking.behavior;

import allow.simulator.util.Triple;
import allow.simulator.utility.IUtility;

public class ParkingUtility implements IUtility<Triple<Double, Double, Double>, ParkingPreferences> {

  @Override
  public double computeUtility(Triple<Double, Double, Double> input, ParkingPreferences preferences) {
    double c = Math.min(input.first, preferences.getCmax());
    double wd = Math.min(input.second, preferences.getWdmax());
    double st = Math.min(input.third, preferences.getStmax());   
    double cfc = Math.max(preferences.getCweight() * (1 - c / preferences.getCmax()), 0);
    double cfwd = Math.max(preferences.getWdweight() * (1 - wd / preferences.getWdmax()), 0);
    double cfst = Math.max(preferences.getStweight() * (1 - st / preferences.getStmax()), 0);
    return Math.max(cfc + cfwd + cfst, 0);
  }

  @Override
  public boolean ascendingOrder() {
    return false;
  }
}
