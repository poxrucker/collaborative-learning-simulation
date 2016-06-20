package allow.simulator.core;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import allow.simulator.entity.Entity;
import allow.simulator.entity.EntityType;
import allow.simulator.entity.FlexiBusAgency;
import allow.simulator.entity.Person;
import allow.simulator.entity.PlanGenerator;
import allow.simulator.entity.PublicTransportation;
import allow.simulator.entity.PublicTransportationAgency;
import allow.simulator.entity.Taxi;
import allow.simulator.entity.TaxiAgency;
import allow.simulator.entity.knowledge.EvoKnowledge;
import allow.simulator.entity.utility.Preferences;
import allow.simulator.entity.utility.Utility;
import allow.simulator.mobility.data.IDataService;
import allow.simulator.mobility.data.MobilityRepository;
import allow.simulator.mobility.data.OfflineDataService;
import allow.simulator.mobility.data.OnlineDataService;
import allow.simulator.mobility.data.TransportationRepository;
import allow.simulator.mobility.planner.BikeRentalPlanner;
import allow.simulator.mobility.planner.FlexiBusPlanner;
import allow.simulator.mobility.planner.JourneyPlanner;
import allow.simulator.mobility.planner.OTPPlannerService;
import allow.simulator.mobility.planner.TaxiPlanner;
import allow.simulator.statistics.Statistics;
import allow.simulator.util.Coordinate;
import allow.simulator.world.StreetMap;
import allow.simulator.world.Weather;
import allow.simulator.world.overlay.DistrictOverlay;
import allow.simulator.world.overlay.IOverlay;
import allow.simulator.world.overlay.RasterOverlay;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Main class of simulator for collaborative learning in the scenario of
 * personalized, multi-modal urban mobility.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class Simulator {
	// Instance of the simulator which can be accessed using Instance() method
	private static Simulator instance;
		
	// Simulation context
	private Context context;
	
	// Id counter for entities
	private long ids;
	
	// Threadpool for executing multiple tasks in parallel
	private ExecutorService threadpool;
	
	public static final String OVERLAY_DISTRICTS = "partitioning";
	public static final String OVERLAY_RASTER = "raster";

	/**
	 * Creates a new instance of the simulator.
	 * @throws IOException 
	 */
	public void setup(Configuration config, SimulationParameter params) throws IOException {
		// Reset Id counter.
		ids = 0;
		threadpool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		// Setup world.
		System.out.println("Loading world...");
		StreetMap world = new StreetMap(config.getMapPath());
		
		System.out.println("  Adding layer \"" + OVERLAY_DISTRICTS + "\"...");
		Path l = config.getLayerPath(OVERLAY_DISTRICTS);
		if (l == null)
			throw new IllegalStateException("Error: Missing layer with key \"" + OVERLAY_DISTRICTS + "\".");
		IOverlay rasterOverlay = new RasterOverlay();
		IOverlay districtOverlay = DistrictOverlay.parse(l, world);
		world.addOverlay(rasterOverlay, OVERLAY_RASTER);
		world.addOverlay(districtOverlay, OVERLAY_DISTRICTS);
		
		// Create data services.
		System.out.println("Creating data services...");
		List<IDataService> dataServices = new ArrayList<IDataService>();
		List<Service> dataConfigs = config.getDataServiceConfiguration();
		
		for (Service dataConfig : dataConfigs) {

			if (dataConfig.isOnline()) {
				// For online queries create online data services. 
				dataServices.add(new OnlineDataService(dataConfig.getURL(), dataConfig.getPort()));
		
			} else {
				// For offline queries create mobility repository and offline service.
				MobilityRepository repos = new MobilityRepository(Paths.get(dataConfig.getURL()), world);
				dataServices.add(new OfflineDataService(repos));
			}
		}
		
		// Create time and weather.
		Time time = new Time(config.getStartingDate(), 5);
				
		// Create planner services.
		System.out.println("Creating planner services...");
		List<OTPPlannerService> plannerServices = new ArrayList<OTPPlannerService>();
		List<Service> plannerConfigs = config.getPlannerServiceConfiguration();
		
		for (int i = 0; i < plannerConfigs.size(); i++) {
			Service plannerConfig = plannerConfigs.get(i);
			plannerServices.add(new OTPPlannerService(plannerConfig.getURL(), plannerConfig.getPort(), world, dataServices.get(0), time));
		}		
		
		// Create taxi planner service
		Coordinate taxiRank = new Coordinate(11.1198448, 46.0719489);
		TaxiPlanner taxiPlannerService = new TaxiPlanner(plannerServices, taxiRank);
		
		// Create bike rental service
		Coordinate bikeRentalStation = new Coordinate(11.1248895,46.0711398);
		BikeRentalPlanner bikeRentalPlanner = new BikeRentalPlanner(plannerServices, bikeRentalStation);
		System.out.println("Loading weather model...");
		Weather weather = new Weather(config.getWeatherPath(), time);
		
		JourneyPlanner planner = new JourneyPlanner(plannerServices, taxiPlannerService,
				bikeRentalPlanner, new FlexiBusPlanner());
		
		// Create global context from world, time, planner and data services, and weather.
		context = new Context(world, new EntityManager(), time, planner, dataServices.get(0), weather, new Statistics(400), params);
		
		// Setup entities.
		System.out.println("Loading entities from file...");
		loadEntitiesFromFile(config.getAgentConfigurationPath(), params.KnowledgeModel);
		
		// Create public transportation.
		System.out.println("Creating public transportation system...");
		TransportationRepository repos = TransportationRepository.loadPublicTransportation(this);
		context.setTransportationRepository(repos);
		
		// Initialize EvoKnowlegde and setup logger.
		EvoKnowledge.initialize(config.getEvoKnowledgeConfiguration(), params.KnowledgeModel, "evo_" + params.BehaviourSpaceRunNumber, threadpool);
		
		// Update world
		world.update();
	}
	
	private void loadEntitiesFromFile(Path config, String knowledgeModel) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		
		List<String> lines = Files.readAllLines(config, Charset.defaultCharset());
		
		for (String line : lines) {
			Person p = mapper.readValue(line, Person.class);
			p.setContext(context);
			PlanGenerator.generateDayPlan(p);
			context.getEntityManager().addEntity(p);
			ids = Math.max(ids, p.getId() + 1);
		}
	}
	
	/**
	 * Returns the current instance of the Simulator.
	 *  
	 * @return Instance of the Simulator
	 */
	public static Simulator Instance() {
		if (instance == null)
			instance = new Simulator();
		return instance;
	}

	/**
	 * Adds a new entity of given type to the simulation.
	 * 
	 * @param e Type of entity.
	 * @return Instance of new entity of given type.
	 */
	public Entity addEntity(EntityType e) {
		// Entity to add.
		Entity newEntity = null;
		
		switch (e) {
			case BUS:
				newEntity = new PublicTransportation(ids++, new Utility(), new Preferences(), context, 25);
				break;
				
			case TAXI:
				newEntity = new Taxi(ids++, new Utility(), new Preferences(), context, 3);
				break;
				
			case PUBLICTRANSPORTAGENCY:
				newEntity = new PublicTransportationAgency(ids++, new Utility(), new Preferences(), context);
				break;
			
			case FLEXIBUSAGENCY:
				newEntity = new FlexiBusAgency(ids++, new Utility(), new Preferences(), context);
				break;
				
			case TAXIAGENCY:
				newEntity = new TaxiAgency(ids++, new Utility(), new Preferences(), context);
				break;
			
			default:
				throw new IllegalArgumentException("Error: Unknown entity type.");
		}
		context.getEntityManager().addEntity(newEntity);
		return newEntity;
	}
	
	/**
	 * Advances the simulation by one step.
	 * 
	 * @param deltaT Time interval for this step.
	 */
	public void tick(int deltaT) {
		// Save current day to trigger routine scheduling
		int days = context.getTime().getDays();
		
		// Update time
		context.getTime().tick(deltaT);
		
		// Update world
		context.getWorld().update();
		
		// Trigger routine scheduling.
		if (days != context.getTime().getDays()) {
			Collection<Entity> persons = context.getEntityManager().getEntitiesOfType(EntityType.PERSON);

			for (Entity p : persons) {
				PlanGenerator.generateDayPlan((Person) p);
				p.getRelations().resetBlackList();
			}
		}
		
		if (context.getTime().getCurrentTime().getHour() == 3
				&& context.getTime().getCurrentTime().getMinute() == 0
				&& context.getTime().getCurrentTime().getSecond() == 0) {
			context.getStatistics().reset();
		}
		//context.getWorld().getStreetMap().getNBusiestStreets(20);
		EvoKnowledge.invokeRequest();
		EvoKnowledge.cleanModel();
	}
	
	/**
	 * Returns the context associated with this Simulator instance.
	 * 
	 * @return Context of this Simulator instance
	 */
	public Context getContext() {
		return context;
	}
	
	public void finish() {
		threadpool.shutdown();

		try {
			threadpool.awaitTermination(10, TimeUnit.SECONDS);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
