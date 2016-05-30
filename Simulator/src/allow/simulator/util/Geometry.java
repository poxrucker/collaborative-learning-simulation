package allow.simulator.util;

import java.util.ArrayList;
import java.util.List;

public class Geometry {
	
	public static List<Coordinate> decodePolyline(String encoded) {
		List<Coordinate> points = new ArrayList<Coordinate>();
		
		if (encoded.equals("")) {
			return points;
		}
		
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			points.add(new Coordinate((double) lng / 1E5, (double) lat / 1E5));
		}
		return points;
	}
	
	private static final double FACTOR = (Math.PI / 360.0);
	private static final double DEG2RAD = (Math.PI / 180.0);
	private static final double EARTH_FACTOR = 12742000.0; // 2 * 6371 * 1000.
	
	public static double haversineDistance(Coordinate first, Coordinate second) {
		double sinDLon = Math.sin(FACTOR * (second.x - first.x));
		double sinDLat = Math.sin(FACTOR * (second.y - first.y));
		double a = sinDLat * sinDLat + sinDLon * sinDLon * Math.cos(DEG2RAD * second.y) * Math.cos(DEG2RAD * first.y);
		return EARTH_FACTOR * Math.atan2(Math.sqrt(a), Math.sqrt((1.0 - a)));
	}
	
	/**
	 * Returns the Euclidean distance of two 2D coordinate.
	 * 
	 * @param first First coordinate
	 * @param second Second coordinate
	 * @return Euclidean distance between first and second coordinate
	 */
	public static double euclideanDistance(Coordinate first, Coordinate second) {
		double diffX = first.x - second.x;
		double diffY = first.y - second.y;
		return Math.sqrt(diffX * diffX + diffY * diffY);
	}
}
