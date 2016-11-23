package allow.simulator.entity;

public final class EntityTypes {
	/**
	 * Person entities form the set of travelers.
	 */
	public static final String PERSON = "Person";
	
	/**
	 * Bus entity execute a given fixed schedule.
	 */
	public static final String BUS = "Bus";
	
	/**
	 * Taxi entity execute single and shared taxi rides.
	 */
	public static final String TAXI = "Taxi";
	
	/**
	 * FlexiBus entity execute dynamic trips.
	 */
	public static final String FLEXIBUS = "FlexiBus";
	
	/**
	 * Transport agency manage a static set of routes.
	 */
	public static final String PUBLIC_TRANSPORT_AGENCY = "PublicTransportAgency";
	
	/**
	 * Taxi agency manage taxis which can be dynamically allocated as required.
	 */
	public static final String TAXI_AGENCY = "TaxiAgency";
	
	/**
	 * FlexiBus agency providing dynamic on-request bus scheduling.
	 */
	public static final String FLEXIBUS_AGENCY = "FlexiBusAgency";
	
}
