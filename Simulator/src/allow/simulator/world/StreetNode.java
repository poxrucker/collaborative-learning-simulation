package allow.simulator.world;

import allow.simulator.util.Coordinate;

public final class StreetNode {
	// Id of the node
	private final int id;
	
	// Label of this node
	private final String label;
	
	// Position of the node
	private final Coordinate position;
	
	/**
	 * Creates a new instance of a node of a street network.
	 * 
	 * @param id Id of the node.
	 * @param label Label of this node (for routing).
	 * @param position Position of the node.
	 */
	public StreetNode(int id, String label, Coordinate position) {
		this.id = id;
		this.position = position;
		this.label = label;
	}
	
	/**
	 * Returns Id of the node.
	 * 
	 * @return Id of the node.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Returns label (i.e. name) of the node.
	 * 
	 * @return Label of the node.
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Returns the position of the node.
	 * 
	 * @return Position of the node.
	 */
	public Coordinate getPosition() {
		return position;
	}
	
	@Override
	public String toString() {
		return "[StreetNode : " + id + "]";
	}
	
	@Override
	public boolean equals(Object other) {
		
		if (this == other) 
			return true;

		if (getClass() != other.getClass())
			return false;

		StreetNode n = (StreetNode) other;
		return (id == n.id) && label.equals(n.label) && position.equals(n.position);
	}
	
	@Override
	public int hashCode() {
		return 79 + 31 * ((int) (id ^ (id >>> 32) + position.hashCode()));
	}
}
