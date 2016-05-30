package allow.simulator.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a 2D coordinate.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class Coordinate {
	
	/**
	 * x coordinate
	 */
	public double x;
	
	/**
	 * y coordinate
	 */
	public double y;
	
	/**
	 * Creates a new 2D coordinate with given x and y
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 */
	@JsonCreator
	public Coordinate(@JsonProperty("lon") double x, @JsonProperty("lat") double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Creates a new 2D coordinate with x = 0 and y = 0
	 */
	public Coordinate() { }

	/**
	 * Creates a copy of a given Coordinate instance c with x and y initialized
	 * according to c.
	 * 
	 * @param c Coordinate to be used for initialization
	 */
	public Coordinate(Coordinate c) {
		this(c.x, c.y);
	}
	
	@Override
	public String toString() {
		return "[" + x + ", " + y + "]";
	}
	
	@Override
	public boolean equals(Object other) {
		
		if (other == this)
			return true;
		
		if (!(other instanceof Coordinate))
			return false;
			
		Coordinate otherC = (Coordinate) other;
		return (otherC.x == x) && (otherC.y == y);
	}
	
	@Override
	public int hashCode() {
		return (int) (37 + 31 * (((x == 0.0) ? 0L : Double.doubleToLongBits(x)) + ((y == 0.0) ? 0L : Double.doubleToLongBits(y))));
	}
}
