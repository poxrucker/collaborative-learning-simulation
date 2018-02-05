package allow.simulator.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import allow.simulator.mobility.data.IDataService;
import allow.simulator.mobility.data.TransportationRepository;
import allow.simulator.mobility.planner.JourneyPlanner;
import allow.simulator.statistics.Statistics;
import allow.simulator.world.Street;
import allow.simulator.world.Weather;
import allow.simulator.world.World;
import de.dfki.parking.index.ParkingIndex;

/**
 * Represents context (i.e. world, time, a.s.o. shared by all entities) of 
 * the simulation.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class Context {
	// World including the StreetMap
	private final World world;
	
	// Mapping of streets to parking possibilities
	private final ParkingIndex parkingMap;
	
	// EntityManager instance holding all entities
	private final EntityManager entityManager;
	
	// Simulated time
	private final Time time;
	
	// Weather
	private final Weather weather;
	
	// Data service to obtain information about transportation network
	private final IDataService dataService;
	
	// Journey planner instance for multi-modal journey planning
	private final JourneyPlanner journeyPlanner;
	
	// Transportation repository containing transportation entities
	private TransportationRepository transportationRepository;
	
	// Statistics to collect from the simulation
	private final Statistics stats;
	
	// (Externally) provided simulation parameters
	private final SimulationParameter params;
	
	// Streets of interest
	private final Set<Street> roiStreets;

	/**
	 * Creates new instance of the Context class holding context information
	 * globally available within the simulation.
	 * 
	 * @param world World instance
	 * @param parkingMap ParkingMap instance
	 * @param entityManager EntityManager instance
	 * @param time Time instance
	 * @param dataService Data service to be used
	 * @param plannerService Planner services to be used.
	 * @param weather Current weather.
	 */
	public Context(World world,
	    ParkingIndex parkingMap,
			EntityManager entityManager,
			Time time,
			JourneyPlanner journeyPlanner,
			IDataService dataService,
			Weather weather,
			Statistics stats,
			SimulationParameter params,
			Collection<Street> roiStreets) {
		this.world = world;
		this.parkingMap = parkingMap;
		this.entityManager = entityManager;
		this.time = time;
		this.journeyPlanner = journeyPlanner;
		this.dataService = dataService;
		this.weather = weather;
		this.stats = stats;
		this.params = params;
		this.roiStreets = new HashSet<>(roiStreets);
	}

	public Collection<Street> getRoiStreets() {
	  return roiStreets;
	}
	
	/**
	 * Returns the world of the simulation.
	 * 
	 * @return World of the simulation.
	 */
	public World getWorld() {
		return world;
	}
	
	/**
	 * Returns the ParkingMap instance.
	 * 
	 * @return ParkingMap instance
	 */
	public ParkingIndex getParkingMap() {
	  return parkingMap;
	}
	
	/**
	 * Returns the EntityManager instance of the simulation.
	 * 
	 * @return EntityManager instance
	 */
	public EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * Returns the time of the simulation.
	 * 
	 * @return Time of the simulation.
	 */
	public Time getTime() {
		return time;
	}
	
	/**
	 * Returns the data service instance to obtain information about the
	 * transportation network.
	 * 
	 * @return Data service instance to be used in the simulation
	 */
	public IDataService getDataService() {
		return dataService;
	}
	
	/**
	 * Returns the journey planner instance
	 * 
	 * @return Journey planner instance
	 */
	public JourneyPlanner getJourneyPlanner() {
		return journeyPlanner;
	}
	
	/**
	 * Returns the weather of the simulation.
	 * 
	 * @return Weather of the simulation.
	 */
	public Weather getWeather() {
		return weather;
	}
	
	/**
	 * Returns the statistics collection of the simulation.
	 *  
	 * @return Statistics collection of the simulation.
	 */
	public Statistics getStatistics() {
		return stats;
	}
	
	public void setTransportationRepository(TransportationRepository repository) {
		transportationRepository = repository;
	}
	
	public TransportationRepository getTransportationRepository() {
		return transportationRepository;
	}
	
	/**
	 * Returns collection of simulation parameters coming from the external
	 * simulation environment.
	 * 
	 * @return Simulation parameters.
	 */
	public SimulationParameter getSimulationParameters() {
		return params;
	}
}
