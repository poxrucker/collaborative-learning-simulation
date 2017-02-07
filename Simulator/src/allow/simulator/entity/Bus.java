package allow.simulator.entity;

import allow.simulator.core.Context;

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
	public Bus(int id, Context context, BusAgency agency, int capacity) {
		super(id, context, agency, capacity);
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
