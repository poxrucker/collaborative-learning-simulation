package de.dfki.parking.knowledge;

import java.util.Collection;
import java.util.List;

import allow.simulator.util.Coordinate;
import allow.simulator.world.StreetNode;
import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.index.ParkingIndexEntry;
import de.dfki.parking.model.Parking;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class ParkingKnowledge {
  
  private final ParkingIndex parkingIndex;
  private final Int2ObjectMap<ParkingKnowledgeEntry> parkingKnowledge;

  public ParkingKnowledge(ParkingIndex parkingMap) {
    this.parkingIndex = parkingMap;
    this.parkingKnowledge = new Int2ObjectOpenHashMap<>();
  }

  public void update(Parking parking, int nParkingSpots, int nFreeParkingSpots, double pricePerHour, long time) {
    // Check if entry is known
    ParkingKnowledgeEntry ret = parkingKnowledge.get(parking.getId());

    if (ret == null) {
      // Get ParkingMapEntry from ParkingMap
      ParkingIndexEntry entry = parkingIndex.getEntryForParking(parking);
      ret = new ParkingKnowledgeEntry(entry, nParkingSpots, nFreeParkingSpots, pricePerHour, -1);
      parkingKnowledge.put(parking.getId(), ret);
    }
    ret.update(nFreeParkingSpots, time);
  }

  public Collection<ParkingKnowledgeEntry> findStreetParking(StreetNode node) {
    // Get parking possibilities in street from ParkingMap
    Collection<ParkingIndexEntry> entries = parkingIndex.getParkingInStreet(node);

    if (entries == null)
      return new ObjectArrayList<>(0);

    Collection<ParkingKnowledgeEntry> res = new ObjectArrayList<>(entries.size());
    
    for (ParkingIndexEntry entry : entries) {
      ParkingKnowledgeEntry knowledgeEntry = parkingKnowledge.get(entry.getParking().getId());

      if (knowledgeEntry == null)
        continue;

      res.add(knowledgeEntry);
    } 
    return res;
  }

  public Collection<ParkingKnowledgeEntry> findGarageParking(StreetNode node) {
    // Get parking possibilities in street from ParkingMap
    ParkingIndexEntry entry = parkingIndex.getParkingAtNode(node);

    if (entry == null)
      return new ObjectArrayList<>(0);

    ParkingKnowledgeEntry knowledgeEntry = parkingKnowledge.get(entry.getParking().getId());

    if (knowledgeEntry == null)
      return new ObjectArrayList<>(0);

    Collection<ParkingKnowledgeEntry> res = new ObjectArrayList<>(1);
    res.add(knowledgeEntry);
    return res;
  }

  public Collection<ParkingKnowledgeEntry> findParkingNearby(Coordinate position, double maxDistance) {
    // Get nearby parking possibilities from ParkingMap
    Collection<ParkingIndexEntry> indexEntries = parkingIndex.getParkingsWithMaxDistance(position, maxDistance);

    if (indexEntries.size() == 0)
      return new ObjectArrayList<>(0);

    return filterUnknownFromIndex(indexEntries);
  }

  private Collection<ParkingKnowledgeEntry> filterUnknownFromIndex(Collection<ParkingIndexEntry> indexEntries) {
    List<ParkingKnowledgeEntry> ret = new ObjectArrayList<>(indexEntries.size());

    for (ParkingIndexEntry entry : indexEntries) {
      ParkingKnowledgeEntry temp = parkingKnowledge.get(entry.getParking().getId());

      if (temp == null)
        continue;

      ret.add(temp);
    }
    return ret;
  }
}
