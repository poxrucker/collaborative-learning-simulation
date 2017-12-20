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

public final class ParkingMap {
  
  private final ParkingIndex parkingIndex;
  private final Int2ObjectMap<ParkingMapEntry> parkingKnowledge;

  public ParkingMap(ParkingIndex parkingMap) {
    this.parkingIndex = parkingMap;
    this.parkingKnowledge = new Int2ObjectOpenHashMap<>();
  }

  public void update(Parking parking, int nParkingSpots, int nFreeParkingSpots, double pricePerHour, long time) {
    // Check if entry is known
    ParkingMapEntry ret = parkingKnowledge.get(parking.getId());

    if (ret == null) {
      // Get ParkingMapEntry from ParkingMap
      ParkingIndexEntry entry = parkingIndex.getEntryForParkingId(parking.getId());
      ret = new ParkingMapEntry(entry, nParkingSpots, nFreeParkingSpots, pricePerHour, -1);
      parkingKnowledge.put(parking.getId(), ret);
    }
    ret.update(nFreeParkingSpots, time);
  }

  public Collection<ParkingMapEntry> findStreetParking(StreetNode node) {
    // Get parking possibilities in street from ParkingMap
    Collection<ParkingIndexEntry> entries = parkingIndex.getParkingInStreet(node);

    if (entries == null)
      return new ObjectArrayList<>(0);

    Collection<ParkingMapEntry> res = new ObjectArrayList<>(entries.size());
    
    for (ParkingIndexEntry entry : entries) {
      ParkingMapEntry knowledgeEntry = parkingKnowledge.get(entry.getParking().getId());

      if (knowledgeEntry == null)
        continue;

      res.add(knowledgeEntry);
    } 
    return res;
  }

  public Collection<ParkingMapEntry> findGarageParking(StreetNode node) {
    // Get parking possibilities in street from ParkingMap
    Collection<ParkingIndexEntry> entries = parkingIndex.getParkingAtNode(node);

    if (entries == null)
      return new ObjectArrayList<>(0);

    Collection<ParkingMapEntry> res = new ObjectArrayList<>(entries.size());
    
    for (ParkingIndexEntry entry : entries) {
      ParkingMapEntry knowledgeEntry = parkingKnowledge.get(entry.getParking().getId());

      if (knowledgeEntry == null)
        continue;

      res.add(knowledgeEntry);
    } 
    return res;
  }

  public Collection<ParkingMapEntry> findParkingNearby(Coordinate position, double maxDistance) {
    // Get nearby parking possibilities from ParkingMap
    Collection<ParkingIndexEntry> indexEntries = parkingIndex.getParkingsWithMaxDistance(position, maxDistance);

    if (indexEntries.size() == 0)
      return new ObjectArrayList<>(0);

    return filterUnknownFromIndex(indexEntries);
  }

  private Collection<ParkingMapEntry> filterUnknownFromIndex(Collection<ParkingIndexEntry> indexEntries) {
    List<ParkingMapEntry> ret = new ObjectArrayList<>(indexEntries.size());

    for (ParkingIndexEntry entry : indexEntries) {
      ParkingMapEntry temp = parkingKnowledge.get(entry.getParking().getId());

      if (temp == null)
        continue;

      ret.add(temp);
    }
    return ret;
  }
}
