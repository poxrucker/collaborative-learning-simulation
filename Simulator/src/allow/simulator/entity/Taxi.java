package allow.simulator.entity;

import allow.simulator.core.Context;

public final class Taxi extends TransportationEntity {
	
	public Taxi(int id, Context context, TaxiAgency agency, int capacity) {
		super(id, context, agency, capacity);
	}

	@Override
	public String toString() {
		return "[Taxi" + id + "]";
	}

	@Override
	public String getType() {
		return EntityTypes.TAXI;
	}

	@Override
	public void exchangeKnowledge() {
		
	}	
}
