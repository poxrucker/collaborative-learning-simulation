package allow.simulator.netlogo.agent;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.agent.World;
import org.nlogo.api.AgentException;

import allow.simulator.core.Context;
import allow.simulator.core.EntityManager;
import allow.simulator.core.IContextWrapper;
import allow.simulator.core.Simulator;
import allow.simulator.entity.Entity;
import allow.simulator.entity.EntityType;
import allow.simulator.util.Coordinate;
import allow.simulator.util.Pair;
import allow.simulator.world.StreetMap;
import allow.simulator.world.StreetNode;
import allow.simulator.world.StreetSegment;
import allow.simulator.world.Transformation;

public final class NetLogoWrapper implements IContextWrapper {
	// Static instance
	private static NetLogoWrapper instance;
	
	// NetLogo world instance
	private final World netLogoWorld;

	// Mapping of simulator Ids to NetLogo Ids and vice versa
	private final Map<Long, Long> simToNetLogo;
	private final Map<Long, Long> netLogoToSim;

	// Transformation to convert coordinates
	private Transformation transformation;

	public NetLogoWrapper(World netLogoWorld) {
		this.netLogoWorld = netLogoWorld;
		this.simToNetLogo = new Long2LongOpenHashMap();
		this.netLogoToSim = new Long2LongOpenHashMap();
	}

	public Transformation getTransformation() {
		return transformation;
	}
	
	public World getWorld() {
		return netLogoWorld;
	}
	
	@Override
	public void wrap(Context context) {
		// Wrap world
		wrapWorld((StreetMap) context.getWorld());

		// Wrap entities
		try {
			wrapEntities(context.getEntityManager());
			
		} catch (AgentException e) {
			e.printStackTrace();
		}
	}

	private void wrapWorld(StreetMap world) {
		// Wrap world
		double worldEnvelope[] = new double[] { netLogoWorld.minPxcor(),
				netLogoWorld.maxPxcor(), netLogoWorld.minPycor(),
				netLogoWorld.maxPxcor() };

		// Get envelope of loaded world.
		double gisEnvelope[] = world.getDimensions();

		// Set transformation between NetLogo and loaded world.
		transformation = new Transformation(gisEnvelope, worldEnvelope);

		// Create NetLogo bindings for street nodes.
		Coordinate temp = new Coordinate();
		Map<Long, Turtle> util = new HashMap<Long, Turtle>();

		for (StreetNode node : world.getStreetNodes()) {
			transformation.transform(node.getPosition(), temp);
			Turtle newNode = new Turtle(netLogoWorld, netLogoWorld.getBreed("NODES"), temp.x, temp.y);
			util.put(node.getId(), newNode);
			netLogoWorld.turtles().add(newNode);
			newNode.hidden(true);
		}

		// Create NetLogo bindings for street segments.
		Collection<StreetSegment> segments = world.getStreetSegments();

		for (StreetSegment segment : segments) {
			Pair<StreetNode, StreetNode> in = world.getIncidentNodes(segment);
			Link newLink = netLogoWorld.linkManager.createLink(util.get(in.first.getId()), util.get(in.second.getId()),
					netLogoWorld.links());
			netLogoWorld.links().add(newLink);
			newLink.colorDouble(5.0);
			newLink.lineThickness(0.05);
			newLink.hidden(false);
		}
	}

	private void wrapEntities(EntityManager entityManager) throws AgentException {
		// Prepare mappings
		Long2LongOpenHashMap simToNetLogoTemp = (Long2LongOpenHashMap) simToNetLogo;
		Long2LongOpenHashMap netLogoToSimTemp = (Long2LongOpenHashMap) netLogoToSim;
		simToNetLogoTemp.clear();
		netLogoToSimTemp.clear();

		for (EntityType type : EntityType.values()) {
			// Get all entities of certain type
			Collection<Entity> entities = entityManager.getEntitiesOfType(type);

			if ((entities == null) || (entities.size() == 0))
				continue;

			for (Entity entity : entities) {
				
				switch (type) {
				case BUS:
				case FLEXIBUS:
				case PERSON:
				case TAXI:
				case PUBLICTRANSPORTAGENCY:
				case FLEXIBUSAGENCY:
				case TAXIAGENCY:
					NetLogoAgent newAgent = NetLogoAgent.createNetLogoAgent(this, entity);
					netLogoWorld.turtles().add(newAgent);

					if (netLogoToSim.get(newAgent.id) != null)
						throw new IllegalStateException("Error: NetLogo entity Id" + newAgent.id + " already in use.");
					
					netLogoToSim.put(newAgent.id, entity.getId());

					if (simToNetLogo.get(entity.getId()) != null)
						throw new IllegalStateException("Error: Simulator entity Id" + entity.getId() + " already in use.");
					
					simToNetLogo.put(entity.getId(), newAgent.id);
					break;

				default:
					break;
				}
			}
		}
	}
	
	public static NetLogoWrapper initialize(Simulator simulator, World world) {
		instance = new NetLogoWrapper(world);
		instance.wrap(simulator.getContext());
		return instance;
	}
	
	public static NetLogoWrapper Instance() {
		if (instance == null)
			throw new UnsupportedOperationException();
		return instance;
	}
}
