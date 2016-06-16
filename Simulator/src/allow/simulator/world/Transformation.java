package allow.simulator.world;

import allow.simulator.util.Coordinate;

/**
 * Represents an affine-linear mapping between two rectangular, bounded 
 * coordinate spaces. 
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class Transformation {
	// The envelope (i.e. bounding rectangle) of the first space.
	private double env1[];
	
	// The envelope (i.e. bounding rectangle) of the second space.
	private double env2[];
	
	// Precomputed scaling factors.
	private double s_x;
	private double s_y;
	private double s_x_inv;
	private double s_y_inv;
	
	/**
	 * Constructor.
	 * Creates a new mapping between the two given rectangular spaces.
	 * 
	 * @param env1 Envelope of first space.
	 * @param env2 Envelope of second space.
	 */
	public Transformation(double env1[], double env2[]) {
		this.env1 = env1;
		this.env2 = env2;
		setTransformation(env1, env2);
	}
	
	/**
	 * Constructor.
	 * Creates a new unit mapping between the two identical rectangular spaces.
	 */
	public Transformation() {
		env1 = new double[4];
		env2 = new double[4];
		s_x = 1.0;
		s_x_inv = 1.0;
		s_y = 1.0;
		s_y_inv = 1.0;
	}
	
	public void setTransformation(double env1[], double env2[]) {
		this.env1 = env1;
		this.env2 = env2;
		
		// Scaling factors (assume equal scaling in x and y direction).
		s_x = (env2[1] - env2[0]) / (env1[1] - env1[0]); 
		s_y = (env2[3] - env2[2]) / (env1[3] - env1[2]);
		s_x_inv = 1.0 / s_x;
		s_y_inv = 1.0 / s_y;
	}
	
	/**
	 * Converts coordinates from first space to second space.
	 * 
	 * @param first Coordinates in first space
	 * @return Coordinate in second space
	 */
	public Coordinate transform(Coordinate first) {
		return transform(first, new Coordinate());
	}
	
	/**
	 * Converts coordinates from second space to first space.
	 * 
	 * @param second Coordinates in second space
	 * @return Coordinate in first space
	 */
	public Coordinate backTransform(Coordinate second) {
		return backTransform(second, new Coordinate());
	}
	
	/**
	 * Converts coordinates from first space to second space.
	 * 
	 * @param first Coordinates in first space
	 * @param second Coordinate in second space
	 */
	public Coordinate transform(Coordinate first, Coordinate second) {
		second.x = (first.x - env1[0]) * s_x + env2[0];
		second.y = (first.y - env1[2]) * s_y + env2[2];
		return second;
	}
	
	/**
	 * Converts coordinates from second space to first space.
	 * 
	 * @param second Coordinates in second space
	 * @param first Coordinate in first space
	 */
	public Coordinate backTransform(Coordinate second, Coordinate first) {
		first.x = (second.x - env2[0]) * s_x_inv + env1[0];
		first.y = (second.y - env2[2]) * s_y_inv + env1[2];
		return first;
	}
}
