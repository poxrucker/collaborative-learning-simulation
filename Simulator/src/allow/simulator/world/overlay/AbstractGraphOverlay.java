package allow.simulator.world.overlay;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import allow.simulator.util.Coordinate;
import allow.simulator.world.StreetNode;

/**
 * Represents an abstract layer on top of a StreetMap partitioning it
 * into a set of areas having special properties.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public abstract class AbstractGraphOverlay {
	
	/**
	 * Represents the type of layer.
	 * 
	 * @author Andreas Poxrucker (DFKI)
	 *
	 */
	public enum Type {
		
		/**
		 * District layer defines parts of the given StreetMap as districts, i.e.
		 * residential areas, industrial areas etc.
		 */
		DISTRICTS,
		
		/**
		 * Safety layer assigns a safety level to certain areas of the given
		 * StreetMap.  
		 */
		SAFETY
		
	}
	
	// Name of the layer e.g. "partitioning" or "security"
	protected Type type;
	
	// Mapping of areas of the layer to nodes within that area.
	protected Map<Area, List<StreetNode>> areaToNodesMapping;
	
	// Mapping of nodes to areas it lies in.
	protected Map<StreetNode, List<Area>> nodesToAreaMapping;
	
	/**
	 * Constructor.
	 * Creates a new instance of layer given a name and the StreetMap instance
	 * the layer is overlaid to.
	 * 
	 * @param type Name/Identifier of the layer.
	 * @param base StreetMap the layer is added to.
	 */
	protected AbstractGraphOverlay(Type type) {
		this.type = type;
		areaToNodesMapping = new Object2ObjectOpenHashMap<Area, List<StreetNode>>();
		nodesToAreaMapping = new Object2ObjectOpenHashMap<StreetNode, List<Area>>();
	}
	
	/**
	 * Adds a new area to the layer and identifies points of the underlying
	 * StreetMap if they lie within that area.
	 * 
	 * @param area Area to add.
	 */
	public abstract void addArea(Area area);
	
	/**
	 * Returns a list of areas which contain the given point (areas may be overlapping).
	 * 
	 * @param point Point to find areas which contain it.
	 * @return List of areas which contain the given point.
	 */
	public List<Area> getAreasContainingPoint(StreetNode point) {
		return nodesToAreaMapping.get(point);
	}
	
	/**
	 * Returns a list of areas which contain the given point (areas may be overlapping).
	 * 
	 * @param point Point to find areas which contain it.
	 * @return List of areas which contain the given point.
	 */
	public List<Area> getAreasContainingPoint(Coordinate point) {
		Collection<Area> areas = areaToNodesMapping.keySet();
		
		if (areas.size() == 0)
			return Collections.emptyList();
		
		List<Area> ret = new ObjectArrayList<Area>();
		
		for (Area a : areas) {
			
			if (a.pointInArea(point))
				ret.add(a);
		}
		return ret;
	}
	
	public List<StreetNode> getPointsInArea(Area area) {
		return Collections.unmodifiableList(areaToNodesMapping.get(area));
	}
	
	/**
	 * Returns the type of the layer.
	 * 
	 * @return Type of the layer.
	 */
	public Type getType() {
		return type;
	}
}
