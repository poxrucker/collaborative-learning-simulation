package allow.simulator.netlogo.agent;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Observable;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.agent.World;
import org.nlogo.api.AgentException;

import allow.simulator.entity.Entity;
import allow.simulator.entity.EntityTypes;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.util.Coordinate;
import allow.simulator.world.Transformation;

public final class NetLogoAgent extends Turtle implements IAgentAdapter {
	// Entity wrapped by this agent adapter
	private final Entity entity;
	
	// Indicates whether this agent is visible when active
	private final boolean canBeVisible;
	
	// Transformation from simulated to Netlogo world
	private final Transformation transformation;
	
	// Shape lookup table mapping activities to shapes in NetLogo
	private static final Map<ActivityType, String> shapes;
	
	static {
		Map<ActivityType, String> temp = new EnumMap<ActivityType, String>(ActivityType.class);
		temp.put(ActivityType.CORRECT_POSITION, "person");
		temp.put(ActivityType.PREPARE_JOURNEY, "person");
		temp.put(ActivityType.PLAN_JOURNEY, "person");
		temp.put(ActivityType.CYCLE, "bike");
		temp.put(ActivityType.DRIVE, "car side");
		temp.put(ActivityType.FILTER_ALTERNATIVES, "person");
		temp.put(ActivityType.PREPARE_JOURNEY, "person");
		temp.put(ActivityType.USE_PUBLIC_TRANSPORT, "person");
		temp.put(ActivityType.USE_TAXI, "person");
		temp.put(ActivityType.WALK, "person");
		temp.put(ActivityType.RANK_ALTERNATIVES, "person");
		temp.put(ActivityType.LEARN, "person");
		temp.put(ActivityType.REPLAN, "person");
		temp.put(ActivityType.WAIT, "person");
		temp.put(ActivityType.PREPARE_TRIP, "bus");
		temp.put(ActivityType.DRIVE_TO_NEXT_STOP, "bus");
		temp.put(ActivityType.PICKUP_AND_WAIT, "bus");
		temp.put(ActivityType.RETURN_TO_AGENCY, "bus");
		temp.put(ActivityType.LEARN, "bus");
		temp.put(ActivityType.PREPARE_TAXI_TRIP, "car side");
		temp.put(ActivityType.PICK_UP_OR_DROP, "car side");
		temp.put(ActivityType.DRIVE_TO_NEXT_DESTINATION, "car side");
		temp.put(ActivityType.RETURN_TO_TAXI_AGENCY, "car side");
		temp.put(ActivityType.LEARN, "car side");
		shapes = Collections.unmodifiableMap(temp);
	}
	
	/**
	 * Creates a new instance of an adapter wrapping a simulation entity into
	 * a Netlogo agent. The agent will be added to the NetLogo world as part of
	 * the specified breed and will have the given color.
	 * 
	 * @param world
	 * @param breedType
	 * @param color
	 * @param entity
	 * @throws AgentException
	 */
	private NetLogoAgent(World world, AgentSet breedType, double color, Transformation transformation,
			boolean canBeVisible, Entity entity) throws AgentException {
		super(world, breedType, 0.0, 0.0);
		this.entity = entity;
		this.canBeVisible = canBeVisible;
		this.transformation = transformation;
		size(1.0);
		hidden(true);
		colorDouble(color);
		Coordinate netlogo = transformation.transform(entity.getPosition());
				
		if ((netlogo.x > world().minPxcor()) && (netlogo.x < world().maxPxcor()) && (netlogo.y > world().minPycor() && (netlogo.y < world().maxPycor()))) {
			xandycor(netlogo.x, netlogo.y);
		}
		entity.addObserver(this);
	}
	
	@Override
	public Entity getEntity() {
		return entity;
	}

	@Override
	public boolean execute() {
		Activity executedActivity = entity.execute();
		
		if (executedActivity == null) {
			if (!hidden())
				hidden(true);
			return false;
		}
		
		String s = shapes.get(executedActivity.getType());

		// Update shape and visibility if necessary
		if (!canBeVisible)
			return true;
		
		if (!s.equals(shape()))
			shape(s);	
		
		if (hidden())
			hidden(false);
		return true;
	}

	@Override
	public void exchangeKnowledge() {
		entity.exchangeKnowledge();
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (o != entity)
			throw new IllegalStateException("Error: Update must be called by wrapped entity.");
		
		// Update x and y coordinates.
		Coordinate temp = transformation.transform(entity.getPosition());
		
		if ((temp.x > world().minPxcor()) && (temp.x < world().maxPxcor()) && (temp.y > world().minPycor() && (temp.y < world().maxPycor()))) {
			
			try {
				xandycor(temp.x, temp.y);
				
			} catch (AgentException e) {
				e.printStackTrace();
			}
			hidden(false);
			
		} else {
			hidden(true);
		}	
	}
	
	public static NetLogoAgent createNetLogoAgent(NetLogoWrapper wrapper, Entity entity) throws AgentException {
		AgentSet breed = null;
		double color = 0.0;
		boolean canBeVisible = true;
		World world = wrapper.getWorld();
		
		switch (entity.getType()) {
		case EntityTypes.BUS:
			breed = world.getBreed("BUSSES");
			break;
			
		case EntityTypes.FLEXIBUS:
			breed = world.getBreed("FLEXIBUSSES");
			break;
			
		case EntityTypes.FLEXIBUS_AGENCY:
			breed = world.getBreed("TRANSPORTAGENCIES");
			canBeVisible = false;
			break;
			
		case EntityTypes.PERSON:
			breed = world.getBreed("PERSONS");
			color = (double) Math.random() * 149.0;
			break;
			
		case EntityTypes.PUBLIC_TRANSPORT_AGENCY:
			breed = world.getBreed("TRANSPORTAGENCIES");
			canBeVisible = false;
			break;
			
		case EntityTypes.TAXI:
			breed = world.getBreed("TAXIS");
			color = 45.0;
			break;
			
		case EntityTypes.TAXI_AGENCY:
			breed = world.getBreed("TRANSPORTAGENCIES");
			canBeVisible = false;
			break;
		
		default:
			throw new IllegalArgumentException("Error: Unknown entity type " + entity.getType());
		}
		return new NetLogoAgent(world, breed, color, wrapper.getTransformation(), canBeVisible, entity);
	}
}
