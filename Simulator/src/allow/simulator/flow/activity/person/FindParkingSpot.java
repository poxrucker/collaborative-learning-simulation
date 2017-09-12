package allow.simulator.flow.activity.person;

import java.util.List;

import allow.simulator.entity.Person;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.world.Street;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class FindParkingSpot extends Activity<Person> {
  // Visited streets
  private final List<Street> visitedStreets;
  
  public FindParkingSpot(Person entity, Street street) {
    super(ActivityType.FIND_PARKING_SPOT, entity);
    visitedStreets = new ObjectArrayList<>(new Street[] { street });
  }

  public FindParkingSpot(Person entity, List<Street> visitedStreets) {
    super(ActivityType.FIND_PARKING_SPOT, entity);
    this.visitedStreets = visitedStreets;
  }
  
  @Override
  public double execute(double deltaT) {
    // Current street
    Street current = visitedStreets.get(visitedStreets.size() - 1);
    
    if (current.getEndNode().getPosition().equals(entity.getHome())) {
      // If destination is home, set finished as person parks at their home
      setFinished();
      return deltaT;
    }
    
    // Check if there is a free parking spot in the current street
    
    
    
    return 0;
  }

}
