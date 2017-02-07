package allow.simulator.mobility.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import allow.simulator.entity.Person;
import allow.simulator.entity.TransportationEntity;
import allow.simulator.util.Coordinate;

public class Stop {
	// Id of stop
	protected final String stopId;

	// Position of the stop
	protected final Coordinate position;

	// Transportation entities waiting at the stop
	protected final List<TransportationEntity> transportationEntities;

	// Passengers waiting at the stop
	protected final List<Person> passengers;

	/**
	 * Creates new instance of a stop.
	 * 
	 * @param stopId Id of the stop.
	 * @param position Location of the stop.
	 */
	public Stop(String stopId, Coordinate position) {
		this.stopId = stopId;
		this.position = position;
		transportationEntities = new ArrayList<TransportationEntity>();
		passengers = new ArrayList<Person>();
	}

	/**
	 * Returns the Id of the stop.
	 * 
	 * @return Id of the stop.
	 */
	public String getStopId() {
		return stopId;
	}

	/**
	 * Returns the position of this stop.
	 * 
	 * @return Position of this stop.
	 */
	public Coordinate getPosition() {
		return position;
	}
	
	/**
	 * Checks if there are any passengers waiting at this stop.
	 * 
	 * @return True if there are passengers waiting, false otherwise
	 */
	public boolean hasWaitingPersons() {
		return !passengers.isEmpty();
	}

	/**
	 * Adds a waiting passengers to this stop.
	 * 
	 * @param passenger Passenger to add to this stop
	 */
	public void addWaitingPerson(Person passenger) {
		passengers.add(passenger);
	}

	/**
	 * Removes a waiting passenger from this stop.
	 * 
	 * @param p Passenger to remove from this stop
	 */
	public void removeWaitingPerson(Person passenger) {
		passengers.remove(passenger);
	}

	/**
	 * Checks if there is a transportation entity currently waiting at the stop.
	 *
	 * @return True if there is a waiting transportation entity, false otherwise.
	 */
	public boolean hasWaitingTransportationEntity() {
		return !transportationEntities.isEmpty();
	}
	
	/**
	 * Returns a list of transportation entities waiting at the stop.
	 * 
	 * @return Waiting transportation entities
	 */
	public List<TransportationEntity> getTransportationEntities() {
		return Collections.unmodifiableList(transportationEntities);
	}
	
	/**
	 * Adds a transportation entity to wait at this stop.
	 * 
	 * @param b Transportation entity to be added to this stop.
	 */
	public void addTransportationEntity(TransportationEntity entity) {
		transportationEntities.add(entity);
	}
	
	/**
	 * Removes a waiting transportation entity from this stop.
	 * 
	 * @param b Transportation entity to remove from this stop.
	 */
	public void removeTransportationEntity(TransportationEntity entity) {
		transportationEntities.remove(entity);
	}
	
	public String toString() {
		return "[Stop" + stopId + "]";
	}
}
