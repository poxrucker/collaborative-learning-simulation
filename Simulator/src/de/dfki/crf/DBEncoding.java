package de.dfki.crf;

import java.time.LocalDateTime;

import allow.simulator.mobility.planner.TType;
import allow.simulator.world.Weather;

public class DBEncoding {

	private static final byte NIGHT = 0;
	private static final byte EARLY_MORNING = 1;
	private static final byte MORNING = 2;
	private static final byte EARLY_AFTERNOON = 3;
	private static final byte AFTERNOON = 4;
	private static final byte EVENING = 5;
	
	private static final byte TIME_OF_DAY[] = new byte[] {
		NIGHT,
		NIGHT,
		NIGHT,
		NIGHT,
		NIGHT,
		EARLY_MORNING,
		EARLY_MORNING,
		EARLY_MORNING,
		EARLY_MORNING,
		MORNING,
		MORNING,
		MORNING,
		EARLY_AFTERNOON,
		EARLY_AFTERNOON,
		EARLY_AFTERNOON,
		AFTERNOON,
		AFTERNOON,
		AFTERNOON,
		AFTERNOON,
		EVENING,
		EVENING,
		EVENING,
		NIGHT,
		NIGHT
	};
	
	public static byte encodeTimeOfDay(LocalDateTime dateTime) {
		return TIME_OF_DAY[dateTime.getHour()];
	}
	
	public static byte encodeTType(TType type) {
		
		switch (type) {
		case WALK:
			return 0;
		
		case CAR:
			return 1;
		
		case BUS:
			return 2;
		
		case RAIL:
			return 3;
		
		case CABLE_CAR:
			return 4;
		
		case BICYCLE:
			return 5;
		
		case TRANSIT:
			return 6;
		
		case FLEXIBUS:
			return 7;
		
		case TAXI:
			return 8;
			
		case SHARED_TAXI:
			return 9;
			
		case SHARED_BICYCLE:
			return 10;
			
		default:
			throw new IllegalArgumentException("Error: Unknown transporation type " + type);
		}
	}
	

	/**
	 * Returns an encoded from the weather state.
	 * For fog the first bit is set.
	 * For rain the second bit is set.
	 * For snow the third bit is set.
	 * For thunderstorm the fourth bit is set.
	 * Combinations are possible, too.
	 * 
	 * @return Encoded weather state
	 */
	public static byte encodeWeather(Weather.State weather) {
		byte encoding = 0;
		
		if (weather.getFog())
			encoding |= 1;
		
		if (weather.getRain())
			encoding |= 2;
		
		if (weather.getSnow())
			encoding |= 4;
		
		if (weather.getThunderstorm())
			encoding |= 8;
		return encoding;
	}
	
	public static byte encodeDayOfWeek(int dayOfWeek) {
		return (byte) dayOfWeek;
	}
}
