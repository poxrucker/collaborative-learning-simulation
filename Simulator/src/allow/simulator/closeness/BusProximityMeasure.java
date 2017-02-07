package allow.simulator.closeness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import allow.simulator.entity.Entity;
import allow.simulator.entity.Person;
import allow.simulator.entity.TransportationEntity;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.flow.activity.person.UsePublicTransport;

/**
 * 
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class BusProximityMeasure implements IProximityMeasure {
	/**
	 * Static instance of BusProximityMeasure
	 */
	public static final IProximityMeasure Instance = new BusProximityMeasure();
	
	/**
	 * Creates a new instance of a BusProximityMeasure.
	 */
	private BusProximityMeasure() { }
	
	@Override
	public Collection<Entity> getCloseEntities(Entity entity, double maxDistance) {
		// Get current activity of entity.
		Activity<?> a = entity.getFlow().getCurrentActivity();
		
		if (a.getType() != ActivityType.USE_PUBLIC_TRANSPORT)
			return Collections.emptyList();

		List<Entity> passengers = new ArrayList<Entity>();

		// If entity is using public transportation, add bus and passengers as
		// possible exchange candidates.
		TransportationEntity b = ((UsePublicTransport) a).getMeansOfTransportation();

		if ((b != null) && (b.getId() != entity.getId())) {
			passengers.add(b);
				
			for (Person p : b.getPassengers()) {
					
				if (p.getId() == entity.getId())
					continue;
				passengers.add(p);
			}
		}		
		return passengers;
	}
}
