package allow.simulator.statistics;

import java.util.ArrayList;
import java.util.List;

import allow.simulator.world.StreetSegment;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public final class CoverageStatistics {
  // Maximum time after which a segment will be removed from the set of
  // visited segments
  private final int maximumTime;
  
  // Total length of the complete street network
  private final double totalNetworkLength;
  
  // Total length of the visited street network
  private double visitedNetworkLength;
  
  // Set of visited street network edges
  private Object2LongOpenHashMap<StreetSegment> visitedLinks;
  
  public CoverageStatistics(int maximumTime, double totalNetworkLength) {
    this.maximumTime = maximumTime;
    this.totalNetworkLength = totalNetworkLength;
    this.visitedNetworkLength = 0.0;
    visitedLinks = new Object2LongOpenHashMap<>();
  }
  
  public void updateSegment(long time, StreetSegment seg) {
    visitedLinks.put(seg, time);
  }
  
  public void updateStatistics(long time) {
    // Remove 
    removeOutdated(time);
    
    // Calculate length
    double length = 0.0;
    
    for (StreetSegment seg : visitedLinks.keySet()) {
      length += seg.getLength();
    }
    visitedNetworkLength = length;
  }
  
  public double getVisitedNetworkLength() {
    return visitedNetworkLength;
  }
  
  public double getTotalNetworkLength() {
    return totalNetworkLength;
  }
  
  private void removeOutdated(long time) {
    List<StreetSegment> segsToRemove = new ArrayList<>();
    
    for (StreetSegment seg : visitedLinks.keySet()) {
      long lastVisited = visitedLinks.getLong(seg);
      long diff = (time - lastVisited) / 1000;
          
      if (diff > maximumTime) {
        segsToRemove.add(seg);
      }
    }
    
    for (StreetSegment seg : segsToRemove) {
      visitedLinks.removeLong(seg);
    }
  }
}
