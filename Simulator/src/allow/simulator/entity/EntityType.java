package allow.simulator.entity;

public enum EntityType {
	
	/**
	 * Person entities are the most general form. They cannot be instantiated
	 * directly. Use one of the following subclasses (child, homemaker,
	 * retired, student, worker) instead.
	 */
	PERSON,
	
	/**
	 * Bus entity executing a given fixed schedule.
	 */
	BUS,
	
	/**
	 * Taxi entity executing single and shared taxi rides.
	 */
	TAXI,
	
	/**
	 * FlexiBus entity executing dynamic trips.
	 */
	FLEXIBUS,
	
	/**
	 * Train entity executing a given fixed schedule.
	 */
	TRAIN,
	
	/**
	 * Transport agency managing a static set of routes.
	 */
	PUBLICTRANSPORTAGENCY,
	
	/**
	 * Taxi agency managing a taxis which can be dynamically allocated as required.
	 */
	TAXIAGENCY,
	
	/**
	 * FlexiBus agency providing dynamic on-request bus scheduling.
	 */
	FLEXIBUSAGENCY,
	
	/**
	 * The urban mobility system for smart journey planning.
	 */
	URBANMOBILITYSYSTEM
}
