package de.dfki.parking.exploration;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.world.Street;
import allow.simulator.world.StreetMap;
import allow.simulator.world.StreetNode;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public final class RandomParkingSearchStrategy {
  // StreetMap instance for path planning
  private final StreetMap map;

  // Set of already visited streets
  private final List<Street> visitedStreets;
  
  public RandomParkingSearchStrategy(StreetMap streetMap) {
    this(streetMap, new ObjectArrayList<>());
  }
  
  public RandomParkingSearchStrategy(StreetMap map, Collection<Street> visitedStreets) {
    this.map = map;
    this.visitedStreets = new ObjectArrayList<>(visitedStreets);
  }
  
  
  public List<Street> getPathToNextPossibleParking(Street current) {
    // Add street to visited streets
    visitedStreets.add(current);
    
    // Get all outgoing streets starting at current intersection
    Set<StreetNode> previouslyVisitedNodes = getVisitedNodes(visitedStreets);
    List<List<Street>> paths = getInitialPaths(current, previouslyVisitedNodes);
    
    if (paths == null)
      return null;
    
    List<List<Street>> candidates = null;

    while ((candidates = getCandidatePaths(paths, previouslyVisitedNodes)).size() == 0) {
      paths = updatePaths(paths, previouslyVisitedNodes);
      
      if (paths.size() == 0)
        return null;
    }
    return chooseRandomPath(candidates);
  }
  
  private List<List<Street>> getInitialPaths(Street current, Set<StreetNode> previouslyVisitedNodes) {
    // Get all streets starting at the end of the current street from StreetMap instance
    List<Street> outgoingStreets = getValidStreets(current);

    if (outgoingStreets.size() == 0)
      return null;
    
    // Create a separate path for each candidate street
    List<List<Street>> initialCandidates = new ObjectArrayList<>(outgoingStreets.size());

    // Filter streets which lead to nodes that have already been visited
    for (Street street : outgoingStreets) {

      if (previouslyVisitedNodes.contains(street.getEndNode()))
        continue;
      
      initialCandidates.add(new ObjectArrayList<>(new Street[] { street }));
    }
    return initialCandidates;
  }

  private List<List<Street>> getCandidatePaths(List<List<Street>> paths, Set<StreetNode> visitedNodes) {
    // Get all destination nodes from initial candidates
    List<List<Street>> candidates = new ObjectArrayList<>(paths.size());

    for (List<Street> path : paths) {
      // Check if last street end on node which has already been visited
      Street last = path.get(path.size() - 1);

      if (visitedNodes.contains(last.getEndNode()))
        continue;

      candidates.add(path);
    }
    return candidates;
  }

  private List<List<Street>> updatePaths(List<List<Street>> paths, Set<StreetNode> visitedNodes) {
    // Get all streets starting at the end of each path from StreetMap instance
    List<List<Street>> ret = new ObjectArrayList<>();

    for (List<Street> path : paths) {
      Street last = path.get(path.size() - 1);
      List<Street> outgoingStreets = new ObjectArrayList<>(map.getOutgoingEdges(last.getEndNode()));

      for (Street street : outgoingStreets) {
        
        if (!isValidStreet(street))
          continue;
         
        if (visitedNodes.contains(street.getEndNode()) && outgoingStreets.size() > 1)
          continue;
        
        List<Street> newPath = new ObjectArrayList<>(path.size() + 1);
        newPath.addAll(path);
        newPath.add(street);
        ret.add(newPath);
      }
    }
    return ret;
  }

  private List<Street> getValidStreets(Street current) {
    // Get all streets starting at the end of the current street from StreetMap instance
    Collection<Street> possibleStreets = map.getOutgoingEdges(current.getEndNode());
    List<Street> ret = new ObjectArrayList<>(possibleStreets.size());
    
    for (Street street : possibleStreets) {
      
      if (!isValidStreet(street))
        continue;
      ret.add(street);
    }
    return ret;
  }
  
  private boolean isValidStreet(Street street) {
    return !(street.getName().equals("Fußweg")
        || street.getName().equals("Bürgersteig")
        || street.getName().equals("Stufen")
        || street.getName().equals("Fußgängertunnel")
        || street.getName().equals("Fahrradweg")
        || street.getName().equals("Fußgängerbrücke"));
  }
  
  private Set<StreetNode> getVisitedNodes(List<Street> path) {
    Set<StreetNode> nodes = new ObjectOpenHashSet<>();
    nodes.add(path.get(0).getStartingNode());

    for (Street street : path) {
      nodes.add(street.getEndNode());
    }
    return nodes;
  }

  private List<Street> chooseRandomPath(List<List<Street>> streets) {
    return streets.get(ThreadLocalRandom.current().nextInt(streets.size()));
  }
}
