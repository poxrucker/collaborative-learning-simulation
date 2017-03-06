package allow.simulator.world;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import allow.simulator.entity.Person;


/**
 * Represents a street of the street map in the Allow Ensembles simulator.
 * Streets are composed of a list of street segments.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public class Street extends Observable implements Observer {
	// Unique Id of the street.
	private long id;

	// Name of the street.
	private String name;

	// Length of street in meters.
	private double length;

	// Current number of vehicles on the street.
	private int numberOfVehicles;
	private double vehicleLengthRatio;
	
	// Subsegments this street is divided into.
	private List<StreetSegment> subSegments;
	
	// Indicates that street is blocked and cannot be used
	private boolean blocked;
	
	private IntSet carsOnStreet;
	private int nWorkers;
	private int nStudents;
	private int nChildren;
	private int nHomemakers;
	
	/**
	 * Constructor.
	 * Creates a new instance of a street.
	 * 
	 * @param id Id of the street.
	 * @param name Name of the street.
	 * @param subSegments Subsegments of the street.
	 */
	public Street(long id, String name, List<StreetSegment> subSegments) {
		this.id = id;
		this.name = name;
		this.subSegments = subSegments;

		for (StreetSegment subseg : subSegments) {
			length += subseg.getLength();
			subseg.addObserver(this);
		}
		carsOnStreet = new IntOpenHashSet();
	}

	/**
	 * Returns the Id of the street.
	 * 
	 * @return Id of the segment.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Returns the name of the street.
	 * 
	 * @return Name of the segment.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the length of this street in meters.
	 * 
	 * @return Length of this segments in m.
	 */
	public double getLength() {
		return length;
	}

	/**
	 * Returns the starting node of this street.
	 * 
	 * @return Starting node of this street.
	 */
	public StreetNode getStartingNode() {
		return subSegments.get(0).getStartingNode();
	}
	
	/**
	 * Returns the end node of this street.
	 * 
	 * @return End node of this street.
	 */
	public StreetNode getEndNode() {
		return subSegments.get(subSegments.size() - 1).getEndingNode();
	}
	
	/**
	 * Returns the number of subsegments this segment is divided into. 1 means
	 * that segment is not divided at all.
	 * 
	 * @return Number of subsegments this segment is divided into.
	 */
	public int getNumberOfSubSegments() {
		return subSegments.size();
	}

	/**
	 * Returns the total number of vehicles currently moving on one of the
	 * segments of this Street instance.
	 * 
	 * @return Total number of vehicles on this Street instance.
	 */
	public int getNumberOfVehicles() {
		return numberOfVehicles;
	}
	
	/**
	 * Returns the ratio of number of vehicles and the length of this Street
	 * instance.
	 * 
	 * @return Ratio of number of vehicles and the length of this Street
	 * instance.
	 */
	public double getVehicleLengthRatio() {
		return vehicleLengthRatio;
	}
	
	/**
	 * Returns the list of subsegments this segment is divided into.
	 * 
	 * @return List of subsegments this segment is divided into.
	 */
	public List<StreetSegment> getSubSegments() {
		return subSegments;
	}

	public int[] getUsageStatistics() {
		return new int[] { carsOnStreet.size(), nWorkers, nStudents, nChildren, nHomemakers };
	}
	
	public void resetUsageStatistics() {
		carsOnStreet.clear();
		nWorkers = 0;
		nStudents = 0;
		nChildren = 0;
		nHomemakers = 0;
	}
	
	public boolean isBlocked() {
		return blocked;
	}
	
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
		
		if (!hasChanged()) {
			setChanged();
			notifyObservers();
		}
	}
	
	public String toString() {
		return "[Street" + id + ", " + name + " " + length + "]";
	}

	@Override
	public boolean equals(Object other) {

		if (other == this) {
			return true;
		}

		if (!(other instanceof Street)) {
			return false;
		}
		Street s = (Street) other;
		return id == s.id && name.equals(s.name) && length == s.length;
	}
	
	@Override
	public int hashCode() {
		return 79 + 31 * ((int) (id ^ (id >>> 32) + name.hashCode()));
	}
	
	public void update(Observable o, Object arg) {
		
		if (arg != null && arg instanceof Person) {
			Person person = (Person)arg;
			
			if (!carsOnStreet.add(person.getId()))
				return;
			
			switch (person.getProfile()) {
			case CHILD:
				nChildren++;
				break;
			case HOMEMAKER:
				nHomemakers++;
				break;
			case RANDOM:
				break;
			case STUDENT:
				nStudents++;
				break;
			case WORKER:
				nWorkers++;
				break;
			default:
				break;
			
			}
		}
		
		if (!hasChanged()) {
			setChanged();
			notifyObservers();
		}
	}
	
	public void updatePossibleSpeedOnSegments() {
		
		if (subSegments.size() == 1) {
			StreetSegment seg = subSegments.get(0);
			seg.updatePossibleSpeed(10 * (double) seg.getNumberOfVehicles() / length);
			
		} else if (subSegments.size() == 2) {
			StreetSegment first = subSegments.get(0);
			StreetSegment second = subSegments.get(1);
			double carsPerMeter = (double) (first.getNumberOfVehicles() + second.getNumberOfVehicles()) / length;
			first.updatePossibleSpeed(10 * carsPerMeter);
			second.updatePossibleSpeed(10 * carsPerMeter);
		
		} else if (subSegments.size() > 2) {
			StreetSegment first = subSegments.get(0);
			StreetSegment second = subSegments.get(1);
			double carsPerMeter = (double) (first.getNumberOfVehicles() + second.getNumberOfVehicles()) / (first.getLength() + second.getLength());
			first.updatePossibleSpeed(10 * carsPerMeter);
			
			for (int i = 1; i < subSegments.size() - 1; i++) {
				StreetSegment prev = subSegments.get(i - 1);
				StreetSegment curr = subSegments.get(i);
				StreetSegment next = subSegments.get(i + 1);
				carsPerMeter = (double) (prev.getNumberOfVehicles() + curr.getNumberOfVehicles() + next.getNumberOfVehicles()) 
						/ (prev.getLength() + curr.getLength() + next.getLength());
				curr.updatePossibleSpeed(10 * carsPerMeter);
			}
			
			StreetSegment previous = subSegments.get(subSegments.size() - 2);
			StreetSegment last = subSegments.get(subSegments.size() - 1);
			carsPerMeter = (double) (last.getNumberOfVehicles() + previous.getNumberOfVehicles()) / (last.getLength() + previous.getLength());
			last.updatePossibleSpeed(10 * carsPerMeter);
		}
		int temp = 0;
		
		for (StreetSegment seg : subSegments) {
			temp += seg.getNumberOfVehicles();
		}
		numberOfVehicles = temp;
		vehicleLengthRatio = numberOfVehicles / length;
		
		/*if (vehicleLengthRatio > 0.1) {
			Simulator.Instance().getContext().getStatistics().reportCongestedStreet();
		}*/
	}
}