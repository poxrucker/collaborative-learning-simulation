package allow.simulator.entity;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import allow.simulator.core.Context;
import allow.simulator.utility.IUtility;
import allow.simulator.utility.Preferences;

/**
 * Represents a transport agency entity managing a set of trips and vehicles
 * to execute them. 
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public abstract class TransportationAgency extends Entity {
	// Id of the agency
	protected String agencyId;
	
	// List of transport entities used by this agency
	protected final Queue<TransportationEntity> vehicles;
	
	/**
	 * Creates new instance of a transportation agency entity.
	 * 
	 * @param id Id of the transportation agency entity
	 * @param utility Utility function
	 * @param context Simulation context
	 */
	protected TransportationAgency(long id, String type, IUtility utility, Preferences prefs, Context context, String agencyId) {
		super(id, type, utility, prefs, context);
		this.agencyId = agencyId;
		vehicles = new ConcurrentLinkedQueue<TransportationEntity>();
	}
	
	/**
	 * Returns Id of this agency.
	 * 
	 * @return Id of this agency.
	 */
	public String getAgencyId() {
		return agencyId;
	}

	@Override
	public void exchangeKnowledge() { }
	
	public boolean isActive() {
		return false;
	}
	
	public String toString() {
		return "[TransportationAgency" + id + "]";
	}
}
