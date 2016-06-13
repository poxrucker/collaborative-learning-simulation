package allow.simulator.entity;

/**
 * Identifies a profile which suggest a person's behaviour/daily routine
 * in the simulation. Workers for example may go to work in the morning and
 * back in the evening. 
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public enum Profile {
	
	/**
	 * Students have a schedule from Monday to Friday arriving randomly at
	 * university at full hour from 8 am to noon and going back randomly
	 * from noon to 8 pm at full hour.
	 */
	STUDENT("Student"),
	
	/**
	 * Workers arrive at work every morning between 5 am and 9 am and go in
	 * the afternoon/evening (eight hours later).
	 */
	WORKER("Worker"),
	
	/**
	 * Homemakers perform random journeys focusing on shopping areas during
	 * the morning and afternoon and purely random journeys during the whole
	 * day.
	 */
	HOMEMAKER("Homemaker"),
	
	/**
	 * Children go to school arriving at 8 am in the morning and back at
	 * 1 pm.
	 */
	CHILD("Child"),
	
	/**
	 * Persons with the random profile perform random journeys during the 
	 * whole day.
	 */
	RANDOM("Random");
	
	// String describing the role for output.
	private String prettyPrint;
	
	private Profile(String name) {
		prettyPrint = name;
	}
	
	@Override
	public String toString() {
		return prettyPrint;
	}	
}
