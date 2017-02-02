package allow.simulator.entity;

import java.util.ArrayList;
import java.util.List;

import allow.simulator.core.Context;
import allow.simulator.mobility.data.Stop;
import allow.simulator.mobility.data.Trip;

/**
 * Abstract class modelling a transportation entity.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public abstract class TransportationEntity extends Entity {
	// Agency the bus is used by
	protected TransportationAgency agency;
	
	// Capacity of the transportation entity
	protected int capacity;
				
	// List of passengers
	protected List<Person> passengers;
			
	// Current trip the transportation entity is operating
	protected Trip currentTrip;
	
	// Current stop the transportation entity stops at or null, in case
	// the entity is moving or does not have a trip assigned
	protected Stop currentStop;
	
	// Current delay
	protected long currentDelay;
	
	/**
	 * Creates a new instance of a transportation entity
	 * 
	 * @param id Id of entity
	 * @param type Type of entity
	 * @param context Simulation context
	 * @param capacity Capacity of entity
	 */
	protected TransportationEntity(long id, Context context, TransportationAgency agency, int capacity) {
		super(id, context);
		this.capacity = capacity;
		passengers = new ArrayList<Person>(capacity);
		this.agency = agency;
	}
	
	/**
	 * Returns the current delay of the transportation entity.
	 * 
	 * @return Current delay of the transportation entity
	 */
	public long getCurrentDelay() {
		return currentDelay;
	}
	
	/**
	 * Sets the current delay of the transportation entity
	 * 
	 * @param newDelay New delay of transportation entity
	 */
	public void setCurrentDelay(long newDelay) {
		currentDelay = newDelay;
	}
	
	/**
	 * Adds a person entity to the list of passengers of the transportation entity 
	 * if there is enough capacity left. 
	 * 
	 * @param p The person to add to the list of passengers of the transportation entity
	 * @return True, if person was added to the list of passengers, false otherwise
	 */
	public boolean addPassenger(Person p) {
		boolean added = false;
		
		synchronized(passengers) {
			
			if (passengers.size() < capacity) {
				added = passengers.add(p);
			}
		}
		return added;
	}
	
	/**
	 * Removes a person entity from the list of passengers of the transportation entity.
	 * 
	 * @param p The person to remove.
	 */
	public void removePassenger(Person p) {
		
		synchronized(passengers) {
			passengers.remove(p);
		}
	}
	
	/**
	 * Returns the list of passengers of the transportation entity.
	 * 
	 * @return List of passengers of the transportation entity
	 */
	public List<Person> getPassengers() {
		return passengers;
	}
	
	/**
	 * Returns the capacity, i.e. maximum number of passengers, of the transportation entity.
	 * 
	 * @return Capacity of the transportation entity
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * Sets the trip the transportation entity is currently operating.
	 * 
	 * @param trip Trip the transportation entity operates. Can be null to
	 * indicate that the transportation entity does not operate a trip
	 */
	public void setCurrentTrip(Trip trip) {
		currentTrip = trip;
	}
	
	/**
	 * Returns the trip the transportation entity is operating.
	 * 
	 * @return Trip the transportation entity is operating
	 */
	public Trip getCurrentTrip() {
		return currentTrip;
	}
	
	/**
	 * Sets the stop of the transportation entity is currently waiting at.
	 * 
	 * @param stop Stop the transportation entity is currently waiting at. 
	 * Can be null to indicate that the transportation entity does not wait
	 * at a stop as it is moving or not executing a trip
	 */
	public void setCurrentStop(Stop stop) {
		currentStop = stop;
	}
	
	/**
	 * Returns the stop the transportation entity is currently waiting at.
	 * 
	 * @return Stop the transportation entity is currently waiting at
	 */
	public Stop getCurrentStop() {
		return currentStop;
	}
	
	/**
	 * Returns the agency the transportation entity is used by.
	 * 
	 * @return Agency the transportation entity is used by.
	 */
	public TransportationAgency getTransportationAgency() {
		return agency;
	}
	
	public String toString() {
		return "[TransportationEntity" + id + "]";
	}

	@Override
	public boolean isActive() {
		return (currentTrip != null);
	}
}
