package allow.simulator.world.overlay;

import java.util.ArrayList;
import java.util.List;

import allow.simulator.util.Coordinate;

/**
 * Represented a shape which is given by only a single point.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class SinglePointShape extends Shape {
	// Point of the shape.
	private final Coordinate point; 
	
	/**
	 * Creates a new instance of a single point area given the point of interest.
	 * 
	 * @param point Point that represents the single point shape
	 */
	public SinglePointShape(Coordinate point) {
		this.point = point;
	}
	
	@Override
	public boolean contains(Coordinate other) {
		return point.equals(other);
	}

	@Override
	public List<Coordinate> getBoundary() {
		ArrayList<Coordinate> ret = new ArrayList<Coordinate>();
		ret.add(point);
		return ret;
	}

	@Override
	public Coordinate getCenter() {
		return point;
	}

}
