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
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.Activity.Type;
import allow.simulator.util.Coordinate;

public final class NetLogoAgent extends Turtle implements IAgentAdapter {
	// Entity wrapped by this agent adapter
	private final Entity entity;
	
	// Indicates whether this agent is visible when active
	private final boolean canBeVisible;
	
	// Shape lookup table mapping activities to shapes in NetLogo
	private static final Map<Activity.Type, String> shapes;
	
	static {
		Map<Activity.Type, String> temp = new EnumMap<Activity.Type, String>(Activity.Type.class);
		temp.put(Type.CORRECT_POSITION, "person");
		temp.put(Type.PREPARE_JOURNEY, "person");
		temp.put(Type.PLAN_JOURNEY, "person");
		temp.put(Type.CYCLE, "bike");
		temp.put(Type.DRIVE, "car side");
		temp.put(Type.FILTER_ALTERNATIVES, "person");
		temp.put(Type.PREPARE_JOURNEY, "person");
		temp.put(Type.USE_PUBLIC_TRANSPORT, "person");
		temp.put(Type.WALK, "person");
		temp.put(Type.RANK_ALTERNATIVES, "person");
		temp.put(Type.LEARN, "person");
		temp.put(Type.REPLAN, "person");
		temp.put(Type.WAIT, "person");
		temp.put(Type.PREPARE_TRIP, "bus");
		temp.put(Type.DRIVE_TO_NEXT_STOP, "bus");
		temp.put(Type.PICKUP_AND_WAIT, "bus");
		temp.put(Type.RETURN_TO_AGENCY, "bus");
		temp.put(Type.LEARN, "bus");
		temp.put(Type.PREPARE_TAXI_TRIP, "car side");
		temp.put(Type.PICK_UP_OR_DROP, "car side");
		temp.put(Type.DRIVE_TO_NEXT_DESTINATION, "car side");
		temp.put(Type.RETURN_TO_TAXI_AGENCY, "car side");
		temp.put(Type.LEARN, "car side");
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
	private NetLogoAgent(World world, AgentSet breedType, double color,
			boolean canBeVisible, Entity entity) throws AgentException {
		super(world, breedType, 0.0, 0.0);
		this.entity = entity;
		this.canBeVisible = canBeVisible;
		size(1.0);
		hidden(true);
		colorDouble(color);
		Coordinate netlogo = entity.getContext().getWorld().getTransformation().GISToNetLogo(entity.getPosition());
				
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
		Coordinate temp = entity.getContext().getWorld().getTransformation().GISToNetLogo(entity.getPosition());
		
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
	
	public static NetLogoAgent createNetLogoAgent(World world, Entity entity) throws AgentException {
		AgentSet breed = null;
		double color = 0.0;
		boolean canBeVisible = true;
		
		switch (entity.getType()) {
		case BUS:
			breed = world.getBreed("BUSSES");
			break;
			
		case FLEXIBUS:
			breed = world.getBreed("FLEXIBUSSES");
			break;
			
		case FLEXIBUSAGENCY:
			breed = world.getBreed("TRANSPORTAGENCIES");
			canBeVisible = false;
			break;
			
		case PERSON:
			breed = world.getBreed("PERSONS");
			color = (double) Math.random() * 149.0;
			break;
			
		case PUBLICTRANSPORTAGENCY:
			breed = world.getBreed("TRANSPORTAGENCIES");
			canBeVisible = false;
			break;
			
		case TAXI:
			breed = world.getBreed("TAXIS");
			color = 45.0;
			break;
			
		case TAXIAGENCY:
			breed = world.getBreed("TRANSPORTAGENCIES");
			canBeVisible = false;
			break;
			
		case TRAIN:
			breed = world.getBreed("TRAINS");
			break;
			
		case URBANMOBILITYSYSTEM:
			break;
			
		default:
			throw new IllegalArgumentException("Error: Unknown entity type " + entity.getType());
		}
		return new NetLogoAgent(world, breed, color, canBeVisible, entity);
	}
}
