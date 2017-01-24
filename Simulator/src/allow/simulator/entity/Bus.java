package allow.simulator.entity;

import allow.simulator.core.Context;
import allow.simulator.mobility.data.BusStop;
import allow.simulator.mobility.data.Trip;

/**
 * Represents a bus entity which is a subtype of a TransportationEntity.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class Bus extends TransportationEntity {
	/**
	 * Creates new instance of a public transportation mean.
	 * 
	 * @param id Id of the transportation mean.
	 * @param utility Utility function of the transportation mean.
	 * @param context Context of the transportation mean.
	 * @param capacity Capacity of the transportation mean.
	 */
	public Bus(long id, Context context, BusAgency agency, int capacity) {
		super(id, context, agency, capacity);
	}
	
	/**
	 * Returns the stop the public transportation entity is currently waiting at.
	 * 
	 * @return Stop the public transportation entity is currently waiting at or null, 
	 * if entity is inactive or moving.
	 */
	public BusStop getCurrentStop() {
		return (BusStop) currentStop;
	}
	
	/**
	 * Sets the stop the public transportation entity is currently waiting at.
	 * 
	 * @param s Stop the public transportation entity is waiting at. Can be null to
	 * indicate that the public transportation entity is inactive or driving.
	 */
	public void setCurrentStop(BusStop s) {
		currentStop = s;
	}
	
	/**
	 * Sets trip of the public transportation entity is currently operating.
	 * 
	 * @param trip Trip the public transportation entity operates. Can be null to
	 * indicate that the public transportation entity does not operate a trip
	 */
	public void setCurrentTrip(Trip trip) {
		currentTrip = trip;
	}
	
	/**
	 * Returns trip the public transportation entity is operating.
	 * 
	 * @return Trip the public transportation entity is operating
	 */
	public Trip getCurrentTrip() {
		return currentTrip;
	}
	
	/**
	 * Returns the agency the public transportation entity is used by.
	 * 
	 * @return Agency the public transportation entity is used by.
	 */
	public BusAgency getTransportationAgency() {
		return (BusAgency) agency;
	}

	@Override
	public String toString() {
		return "[PublicTransportation" + id + "]";
	}

	@Override
	public String getType() {
		return EntityTypes.BUS;
	}
}
