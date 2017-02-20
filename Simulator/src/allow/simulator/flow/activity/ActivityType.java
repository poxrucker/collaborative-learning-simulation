package allow.simulator.flow.activity;

public enum ActivityType {
	
	/**
	 * Bus activities.
	 */
	PREPARE_TRIP,
	
	PICKUP_AND_WAIT,
	
	DRIVE_TO_NEXT_STOP,
	
	RETURN_TO_AGENCY,

	/**
	 * Person activities.
	 */
	USE_PUBLIC_TRANSPORT,
	
	USE_TAXI,
	
	PLAN_JOURNEY,
	
	FILTER_ALTERNATIVES,
	
	RANK_ALTERNATIVES,
	
	PREPARE_JOURNEY,
	
	DRIVE,
	
	WALK,
	
	CYCLE,
	
	CORRECT_POSITION,
	
	REGISTER_TO_FLEXIBUS,
	
	USE_FLEXIBUS,
	
	REPLAN,
	
	REPLAN_CAR_JOURNEY,
	
	WAIT,
	
	/**
	 * Taxi activities.
	 */
	PREPARE_TAXI_TRIP,
	
	DRIVE_TO_NEXT_DESTINATION,
	
	PICK_UP_OR_DROP,
	
	RETURN_TO_TAXI_AGENCY,
	
	/**
	 * Transportation agency activities.
	 */
	SCHEDULE_NEXT_TRIPS,

	SCHEDULE_NEXT_FLEXIBUS_TRIPS,
	
	SCHEDULE_NEXT_TAXI_TRIPS,
	
	/**
	 * General activities.
	 */
	LEARN
}
