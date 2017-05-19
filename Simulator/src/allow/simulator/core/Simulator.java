package allow.simulator.core;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.BufferedWriter;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import allow.simulator.entity.DailyRoutine;
import allow.simulator.entity.Entity;
import allow.simulator.entity.EntityTypes;
import allow.simulator.entity.Person;
import allow.simulator.entity.PlanGenerator;
import allow.simulator.entity.Profile;
import allow.simulator.entity.TravelEvent;
import allow.simulator.knowledge.EvoKnowledge;
import allow.simulator.mobility.data.IDataService;
import allow.simulator.mobility.data.OfflineDataService;
import allow.simulator.mobility.data.OnlineDataService;
import allow.simulator.mobility.data.RoutingData;
import allow.simulator.mobility.data.TransportationRepository;
import allow.simulator.mobility.data.gtfs.GTFSData;
import allow.simulator.mobility.planner.BikeRentalPlanner;
import allow.simulator.mobility.planner.FlexiBusPlanner;
import allow.simulator.mobility.planner.JourneyPlanner;
import allow.simulator.mobility.planner.OTPPlanner;
import allow.simulator.mobility.planner.TaxiPlanner;
import allow.simulator.statistics.Statistics;
import allow.simulator.util.Coordinate;
import allow.simulator.utility.NormalizedLinearUtility;
import allow.simulator.utility.Preferences;
import allow.simulator.world.Street;
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
	// Simulation context
	private Context context;

	private Collection<Street> streetsInROI;

	// Threadpool for executing multiple tasks in parallel
	private ExecutorService threadpool;

	public static final String OVERLAY_DISTRICTS = "partitioning";
	public static final String OVERLAY_RASTER = "raster";

	private StreetCapacityLogger logger;

	/**
	 * Creates a new instance of the simulator.
	 * 
	 * @throws IOException
	 */
	public void setup(Configuration config, SimulationParameter params)
			throws IOException {
		threadpool = Executors.newFixedThreadPool(Runtime.getRuntime()
				.availableProcessors() * 2);

		// Setup world.
		// System.out.println("Loading world...");
		StreetMap world = new StreetMap(config.getMapPath());

		// System.out.println("  Adding layer \"" + OVERLAY_DISTRICTS +
		// "\"...");
		Path l = config.getLayerPath(OVERLAY_DISTRICTS);
		if (l == null)
			throw new IllegalStateException("Error: Missing layer with key \""
					+ OVERLAY_DISTRICTS + "\".");
		RasterOverlay rasterOverlay = new RasterOverlay(world.getDimensions(),
				params.GridResX, params.GridResY);
		IOverlay districtOverlay = DistrictOverlay.parse(l, world);
		world.addOverlay(rasterOverlay, OVERLAY_RASTER);
		world.addOverlay(districtOverlay, OVERLAY_DISTRICTS);

		// Block streets
		if (params.Scenario.equals("TrentoCentro")) {
			initializeBlockedStreetsTrentoCentro(world);
		} else if (params.Scenario.equals("PiazzaVenezia")) {
			initializeBlockedStreetsPiazzaVenezia(world);
		} else if (params.Scenario.equals("ViaBerlino")) {
			initializeBlockedStreetsViaBerlino(world);
		} else if (params.Scenario.equals("Normal")) {
			params.Scenario = "";
		} else
			throw new IllegalArgumentException("Unknown scenarion "
					+ params.Scenario);

		// Initialize streets in region of interest
		//streetsInROI = world.getStreetsInROI(new double[] { 11.1178, 11.1332, 46.0646, 46.0739 });
		streetsInROI = new ArrayList<Street>(world.getStreets());
		
		// Filter walking only streets from name
		List<Street> toRemove = new ObjectArrayList<Street>();

		for (Street s : streetsInROI) {
			if (s.getName().equals("Fußweg")
					|| s.getName().equals("Bürgersteig")
					// || s.getName().equals("Parkplatz")
					|| s.getName().equals("Stufen")
					|| s.getName().equals("Weg") || s.getName().equals("Gasse")
					|| s.getName().equals("Fußgängertunnel")
					|| s.getName().equals("Fahrradweg")
					// || s.getName().equals("Anliegerstraße")
					// || s.getName().equals("Straße")
					|| s.getName().equals("Fußgängerbrücke"))
					// || s.getName().equals("Fahrweg"))
				toRemove.add(s);
		}

		for (Street s : toRemove) {
			streetsInROI.remove(s);
		}
		System.out.println(streetsInROI.size());
		
		// Write to file
		Path streetMapping = Paths.get(params.LoggingFolder + "/"
				+ params.BehaviourSpaceRunNumber + "_mapping.txt");

		try (BufferedWriter wr = Files.newBufferedWriter(streetMapping)) {

			for (Street street : streetsInROI) {
				wr.write(street.getId() + ";" + street.getName() + ";"
						+ street.getStartingNode().getLabel() + ";"
						+ street.getEndNode().getLabel() + "\n");
			}
		}

		// Create data services.
		// System.out.println("Creating data services...");
		List<IDataService> dataServices = new ArrayList<IDataService>();
		List<Service> dataConfigs = config.getDataServiceConfiguration();

		for (Service dataConfig : dataConfigs) {

			if (dataConfig.isOnline()) {
				// For online queries create online data services.
				dataServices.add(new OnlineDataService(dataConfig.getURL(),
						dataConfig.getPort()));

			} else {
				// For offline queries create mobility repository and offline
				// service.
				GTFSData gtfs = GTFSData.parse(Paths.get(dataConfig.getURL()));
				RoutingData routing = RoutingData.parse(
						Paths.get(dataConfig.getURL()), world, gtfs);
				dataServices.add(new OfflineDataService(gtfs, routing));
			}
		}

		// Create time and weather.
		Time time = new Time(config.getStartingDate(), 10);

		// Create planner services.
		// System.out.println("Creating planner services...");
		List<OTPPlanner> plannerServices = new ArrayList<OTPPlanner>();
		List<Service> plannerConfigs = config.getPlannerServiceConfiguration();

		for (int i = 0; i < plannerConfigs.size(); i++) {
			Service plannerConfig = plannerConfigs.get(i);
			plannerServices.add(new OTPPlanner(plannerConfig.getURL(),
					plannerConfig.getPort(), world, dataServices.get(0), time));
		}

		// Create taxi planner service
		Coordinate taxiRank = new Coordinate(11.1198448, 46.0719489);
		TaxiPlanner taxiPlannerService = new TaxiPlanner(plannerServices,
				taxiRank);

		// Create bike rental service
		Coordinate bikeRentalStation = new Coordinate(11.1248895, 46.0711398);
		BikeRentalPlanner bikeRentalPlanner = new BikeRentalPlanner(
				plannerServices, bikeRentalStation);
		// System.out.println("Loading weather model...");
		Weather weather = new Weather(config.getWeatherPath(), time);

		JourneyPlanner planner = new JourneyPlanner(plannerServices,
				taxiPlannerService, bikeRentalPlanner, new FlexiBusPlanner(),
				threadpool);

		// Create global context from world, time, planner and data services,
		// and weather.
		context = new Context(world, new EntityManager(), time, planner,
				dataServices.get(0), weather, new Statistics(500), params);

		// Setup entities.
		// System.out.println("Loading entities from file...");
		loadEntitiesFromFile(config.getAgentConfigurationPath(), params);

		// Create public transportation.
		// System.out.println("Creating public transportation system...");
		TransportationRepository repos = new TransportationRepository(context);
		context.setTransportationRepository(repos);

		// Initialize EvoKnowlegde and setup logger.
		EvoKnowledge.initialize(config.getEvoKnowledgeConfiguration(),
				"without", "evo_" + params.BehaviourSpaceRunNumber, threadpool);

		// Update world
		world.update(context);

		// Create logger
		logger = new StreetCapacityLogger(context, params.SamplingRateInSeconds);
		logger.start(Paths.get(params.LoggingFolder + "/streets_"
				+ params.BehaviourSpaceRunNumber + ".txt"));
		logger.log(streetsInROI);

		System.out.println("Setup simulation run "
				+ params.BehaviourSpaceRunNumber);
	}

	private void initializeBlockedStreetsTrentoCentro(StreetMap world) {
		// Close roads
		world.setStreetBlocked(
				world.getStreet("osm:node:278180296", "osm:node:339334743"),
				true);
		// world.setStreetBlocked(world.getStreet("osm:node:339334743",
		// "osm:node:278180296"), true);

		world.setStreetBlocked(
				world.getStreet("osm:node:339334743", "osm:node:339334723"),
				true);
		// world.setStreetBlocked(world.getStreet("osm:node:339334723",
		// "osm:node:339334743"), true);

		world.setStreetBlocked(
				world.getStreet("osm:node:339334723", "osm:node:1797200899"),
				true);
		// world.setStreetBlocked(world.getStreet("osm:node:1797200899",
		// "osm:node:339334723"), true);

		world.setStreetBlocked(
				world.getStreet("osm:node:1797200899", "osm:node:1797200783"),
				true);
		// world.setStreetBlocked(world.getStreet("osm:node:1797200783",
		// "osm:node:1797200899"), true);

		world.setStreetBlocked(
				world.getStreet("osm:node:1797200783", "osm:node:1797200897"),
				true);
		// world.setStreetBlocked(world.getStreet("osm:node:1797200897",
		// "osm:node:1797200783"), true);

		world.setStreetBlocked(
				world.getStreet("osm:node:256827486", "osm:node:1797200899"),
				true);
		// world.setStreetBlocked(world.getStreet("osm:node:1797200899",
		// "osm:node:256827486"), true);

	}

	private void initializeBlockedStreetsPiazzaVenezia(StreetMap world) {
		world.setStreetBlocked(world.getStreet("osm:node:2477700667", "osm:node:248798251"), true);
		world.setStreetBlocked(world.getStreet("osm:node:248798251", "osm:node:2477700667"), true);
	}
	
	private void initializeBlockedStreetsViaBerlino(StreetMap world) {
		world.setStreetBlocked(world.getStreet("osm:node:1213655165", "osm:node:1213655184"), true);
		world.setStreetBlocked(world.getStreet("osm:node:1213655184", "osm:node:1213655165"), true);
		world.setStreetBlocked(world.getStreet("osm:node:1213655184", "osm:node:1213655169"), true);
		world.setStreetBlocked(world.getStreet("osm:node:1213655169", "osm:node:1213655184"), true);
		world.setStreetBlocked(world.getStreet("osm:node:1213655169", "osm:node:9202675"), true);		
		world.setStreetBlocked(world.getStreet("osm:node:9202675", "osm:node:1213655169"), true);		

		world.setStreetBlocked(world.getStreet("osm:node:260915399", "osm:node:432436654"), true);
		world.setStreetBlocked(world.getStreet("osm:node:432436654", "osm:node:260915399"), true);
		world.setStreetBlocked(world.getStreet("osm:node:432436654", "osm:node:1109380756"), true);
		world.setStreetBlocked(world.getStreet("osm:node:1109380756", "osm:node:432436654"), true);
		world.setStreetBlocked(world.getStreet("osm:node:1109380756", "osm:node:1109380685"), true);
		world.setStreetBlocked(world.getStreet("osm:node:1109380685", "osm:node:1109380756"), true);
		world.setStreetBlocked(world.getStreet("osm:node:1109380685", "osm:node:256827548"), true);
		world.setStreetBlocked(world.getStreet("osm:node:256827548", "osm:node:1109380685"), true);


		world.setStreetBlocked(world.getStreet("osm:node:9207036", "osm:node:258510005"), true);
		world.setStreetBlocked(world.getStreet("osm:node:258510005", "osm:node:9207036"), true);

		world.setStreetBlocked(world.getStreet("osm:node:258510005", "osm:node:9197745"), true);
		world.setStreetBlocked(world.getStreet("osm:node:9197745", "osm:node:258510005"), true);

		world.setStreetBlocked(world.getStreet("osm:node:9199072", "osm:node:9193468"), true);
		world.setStreetBlocked(world.getStreet("osm:node:9193468", "osm:node:9199072"), true);

		world.setStreetBlocked(world.getStreet("osm:node:260915539", "osm:node:260915543"), true);
		world.setStreetBlocked(world.getStreet("osm:node:260915543", "osm:node:260915539"), true);

		world.setStreetBlocked(world.getStreet("osm:node:9210951", "osm:node:193574934"), true);
		world.setStreetBlocked(world.getStreet("osm:node:193574934", "osm:node:9210951"), true);

		world.setStreetBlocked(world.getStreet("osm:node:2275120574", "osm:node:330302388"), true);
		world.setStreetBlocked(world.getStreet("osm:node:330302388", "osm:node:2275120574"), true);

		world.setStreetBlocked(world.getStreet("osm:node:256827544", "osm:node:432436400"), true);
		world.setStreetBlocked(world.getStreet("osm:node:432436400", "osm:node:256827544"), true);
		
		world.setStreetBlocked(world.getStreet("osm:node:330302346", "osm:node:330302382"), true);
		world.setStreetBlocked(world.getStreet("osm:node:330302382", "osm:node:330302346"), true);

		world.setStreetBlocked(world.getStreet("osm:node:330302382", "osm:node:2275120574"), true);
		world.setStreetBlocked(world.getStreet("osm:node:2275120574", "osm:node:330302382"), true);

		//world.setStreetBlocked(world.getStreet("osm:node:432436400", "osm:node:435148101"), true);
		//world.setStreetBlocked(world.getStreet("osm:node:435148101", "osm:node:432436400"), true);

		//world.setStreetBlocked(world.getStreet("osm:node:435148101", "osm:node:256827544"), true);
		//world.setStreetBlocked(world.getStreet("osm:node:256827544", "osm:node:435148101"), true);

		world.setStreetBlocked(world.getStreet("osm:node:476648008", "osm:node:9202675"), true);
		world.setStreetBlocked(world.getStreet("osm:node:9202675", "osm:node:9202879"), true);
		world.setStreetBlocked(world.getStreet("osm:node:9202879", "osm:node:256827544"), true);
		world.setStreetBlocked(world.getStreet("osm:node:256827544", "osm:node:75697034"), true);	
		world.setStreetBlocked(world.getStreet("osm:node:75697034", "osm:node:1520990382"), true);
		world.setStreetBlocked(world.getStreet("osm:node:1520990382", "osm:node:256827546"), true);		
		world.setStreetBlocked(world.getStreet("osm:node:256827546", "osm:node:330302388"), true);
		world.setStreetBlocked(world.getStreet("osm:node:330302388", "osm:node:256827547"), true);
		world.setStreetBlocked(world.getStreet("osm:node:256827547", "osm:node:260915539"), true);
		world.setStreetBlocked(world.getStreet("osm:node:260915539", "osm:node:256827548"), true);
		world.setStreetBlocked(world.getStreet("osm:node:256827548", "osm:node:9193468"), true);
		world.setStreetBlocked(world.getStreet("osm:node:9193468", "osm:node:9207036"), true);
		world.setStreetBlocked(world.getStreet("osm:node:9207036", "osm:node:9197745"), true);
		world.setStreetBlocked(world.getStreet("osm:node:9197745", "osm:node:9210951"), true);	
		world.setStreetBlocked(world.getStreet("osm:node:9210951", "osm:node:476648008"), true);
	}

	private void loadEntitiesFromFile(Path config, SimulationParameter param)
			throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		List<String> lines = Files.readAllLines(config,
				Charset.defaultCharset());

		for (String line : lines) {
			Person person = mapper.readValue(line, Person.class);
			initializePerson(person, context, param);
			context.getEntityManager().addEntity(person);
		}

		Collection<Entity> persons = new ArrayList<Entity>(context
				.getEntityManager().getEntitiesOfType(EntityTypes.PERSON));
		ArrayList<Person> workers = new ArrayList<Person>();
		ArrayList<Person> homemaker = new ArrayList<Person>();

		for (Entity e : persons) {
			Person p = (Person) e;

			if (p.getProfile() == Profile.WORKER) {
				workers.add(p);
			} else if (p.getProfile() == Profile.HOMEMAKER) {
				homemaker.add(p);
			}
		}
		
		// Clone early shift workers
		int earlyShiftWorkersToClone = param.EarlyShiftWorkers ? (int) (workers
				.size() * (double) param.PercentEarlyShiftWorkers / 100.0) : 0;

		for (int i = 0; i < earlyShiftWorkersToClone; i++) {
			Person person = workers.get(i);
			// Clone preferences
			Preferences p1 = person.getRankingFunction().getPreferences();
			Preferences p2 = new Preferences(p1.getTTweight(), p1.getCweight(),
					p1.getWDweight(), p1.getNCweight(), p1.getTmax(),
					p1.getCmax(), p1.getWmax(), p1.getBusPreference(),
					p1.getCarPreference());

			// Clone daily routine
			DailyRoutine dr1 = person.getDailyRoutine();
			List<List<TravelEvent>> events = new ArrayList<List<TravelEvent>>(7);

			for (int j = 1; j < 8; j++) {
				List<TravelEvent> toClone = dr1.getDailyRoutine(j);
				List<TravelEvent> clone = new ArrayList<TravelEvent>(
						toClone.size());

				for (TravelEvent event : toClone) {
					TravelEvent temp = new TravelEvent(event.getTime()
							.plusHours(-2), event.getStartingPoint(),
							event.getDestination(), event.arriveBy());
					clone.add(temp);
				}
				events.add(clone);
			}
			DailyRoutine dr2 = new DailyRoutine(events);

			Person person2 = new Person(context.getEntityManager().getNextId(),
					person.getGender(), person.getProfile(),
					new NormalizedLinearUtility(), p2, new Coordinate(
							person.getHome().x, person.getHome().y),
					person.hasCar(), person.hasBike(), person.useFlexiBus(),
					dr2);
			initializePerson(person2, context, param);
			context.getEntityManager().addEntity(person2);
		}

		// Clone back shift workers
		int backShiftWorkersToClone = param.BackShiftWorkers ? (int) (workers.size() * (double) param.PercentBackShiftWorkers / 100.0) : 0;

		for (int i = 0; i < backShiftWorkersToClone; i++) {
			Person person = workers.get(i);
			// Clone preferences
			Preferences p1 = person.getRankingFunction().getPreferences();
			Preferences p2 = new Preferences(p1.getTTweight(), p1.getCweight(),
					p1.getWDweight(), p1.getNCweight(), p1.getTmax(),
					p1.getCmax(), p1.getWmax(), p1.getBusPreference(),
					p1.getCarPreference());

			// Clone daily routine
			DailyRoutine dr1 = person.getDailyRoutine();
			List<List<TravelEvent>> events = new ArrayList<List<TravelEvent>>(7);

			for (int j = 1; j < 8; j++) {
				List<TravelEvent> toClone = dr1.getDailyRoutine(j);
				List<TravelEvent> clone = new ArrayList<TravelEvent>(
						toClone.size());

				for (TravelEvent event : toClone) {
					TravelEvent temp = new TravelEvent(event.getTime()
							.plusHours(4), event.getStartingPoint(),
							event.getDestination(), event.arriveBy());
					clone.add(temp);
				}
				events.add(clone);
			}
			DailyRoutine dr2 = new DailyRoutine(events);

			Person person2 = new Person(context.getEntityManager().getNextId(),
					person.getGender(), person.getProfile(),
					new NormalizedLinearUtility(), p2, new Coordinate(
							person.getHome().x, person.getHome().y),
					person.hasCar(), person.hasBike(), person.useFlexiBus(),
					dr2);
			initializePerson(person2, context, param);
			context.getEntityManager().addEntity(person2);
		}
		
		// Clone homemaker
		int homemakersToClone = param.ExtraHomemaker ? (int) (homemaker.size() * (double) param.PercentExtraHomemaker / 100.0) : 0;

		for (int i = 0; i < homemakersToClone; i++) {
			Person person = homemaker.get(i);
			// Clone preferences 
			Preferences p1 = person.getRankingFunction().getPreferences();
			Preferences p2 = new Preferences(p1.getTTweight(), p1.getCweight(), p1.getWDweight(),
			p1.getNCweight(), p1.getTmax(), p1.getCmax(), p1.getWmax(), p1.getBusPreference(), p1.getCarPreference()); 
			Person person2 = new Person(context.getEntityManager().getNextId(), person.getGender(),
			person.getProfile(), new NormalizedLinearUtility(), p2, new
			Coordinate(person.getHome().x, person.getHome().y), person.hasCar(),
			person.hasBike(), person.useFlexiBus(), new DailyRoutine());
			initializePerson(person2, context, param);
			context.getEntityManager().addEntity(person2); 
			}

		/*
		 * for (Entity entity : persons) { Person person = (Person)entity;
		 * 
		 * if (param.EarlyShiftWorkers && (person.getProfile() ==
		 * Profile.WORKER) && (ThreadLocalRandom.current().nextInt(100) <
		 * param.PercentEarlyShiftWorkers)) {
		 * 
		 * // Clone preferences Preferences p1 =
		 * person.getRankingFunction().getPreferences(); Preferences p2 = new
		 * Preferences(p1.getTTweight(), p1.getCweight(), p1.getWDweight(),
		 * p1.getNCweight(), p1.getTmax(), p1.getCmax(), p1.getWmax(),
		 * p1.getBusPreference(), p1.getCarPreference());
		 * 
		 * // Clone daily routine DailyRoutine dr1 = person.getDailyRoutine();
		 * List<List<TravelEvent>> events = new ArrayList<List<TravelEvent>>(7);
		 * 
		 * for (int i = 1; i < 8; i++) { List<TravelEvent> toClone =
		 * dr1.getDailyRoutine(i); List<TravelEvent> clone = new
		 * ArrayList<TravelEvent>(toClone.size());
		 * 
		 * for (TravelEvent event : toClone) { TravelEvent temp = new
		 * TravelEvent(event.getTime().plusHours(-2), event.getStartingPoint(),
		 * event.getDestination(), event.arriveBy()); clone.add(temp); }
		 * events.add(clone); } DailyRoutine dr2 = new DailyRoutine(events);
		 * 
		 * Person person2 = new Person(context.getEntityManager().getNextId(),
		 * person.getGender(), person.getProfile(), new
		 * NormalizedLinearUtility(), p2, new Coordinate(person.getHome().x,
		 * person.getHome().y), person.hasCar(), person.hasBike(),
		 * person.useFlexiBus(), dr2); initializePerson(person2, context,
		 * param); context.getEntityManager().addEntity(person2); }
		 * 
		 * if (param.BackShiftWorkers && (person.getProfile() == Profile.WORKER)
		 * && (ThreadLocalRandom.current().nextInt(100) <
		 * param.PercentBackShiftWorkers)) {
		 * 
		 * // Clone preferences Preferences p1 =
		 * person.getRankingFunction().getPreferences(); Preferences p2 = new
		 * Preferences(p1.getTTweight(), p1.getCweight(), p1.getWDweight(),
		 * p1.getNCweight(), p1.getTmax(), p1.getCmax(), p1.getWmax(),
		 * p1.getBusPreference(), p1.getCarPreference());
		 * 
		 * // Clone daily routine DailyRoutine dr1 = person.getDailyRoutine();
		 * List<List<TravelEvent>> events = new ArrayList<List<TravelEvent>>(7);
		 * 
		 * for (int i = 1; i < 8; i++) { List<TravelEvent> toClone =
		 * dr1.getDailyRoutine(i); List<TravelEvent> clone = new
		 * ArrayList<TravelEvent>(toClone.size());
		 * 
		 * for (TravelEvent event : toClone) { TravelEvent temp = new
		 * TravelEvent(event.getTime().plusHours(4), event.getStartingPoint(),
		 * event.getDestination(), event.arriveBy()); clone.add(temp); }
		 * events.add(clone); } DailyRoutine dr2 = new DailyRoutine(events);
		 * 
		 * Person person2 = new Person(context.getEntityManager().getNextId(),
		 * person.getGender(), person.getProfile(), new
		 * NormalizedLinearUtility(), p2, new Coordinate(person.getHome().x,
		 * person.getHome().y), person.hasCar(), person.hasBike(),
		 * person.useFlexiBus(), dr2); initializePerson(person2, context,
		 * param); context.getEntityManager().addEntity(person2); }
		 * 
		 * if (param.ExtraHomemaker && (person.getProfile() ==
		 * Profile.HOMEMAKER) && (ThreadLocalRandom.current().nextInt(100) <
		 * param.PercentExtraHomemaker)) { // Clone preferences Preferences p1 =
		 * person.getRankingFunction().getPreferences(); Preferences p2 = new
		 * Preferences(p1.getTTweight(), p1.getCweight(), p1.getWDweight(),
		 * p1.getNCweight(), p1.getTmax(), p1.getCmax(), p1.getWmax(),
		 * p1.getBusPreference(), p1.getCarPreference()); Person person2 = new
		 * Person(context.getEntityManager().getNextId(), person.getGender(),
		 * person.getProfile(), new NormalizedLinearUtility(), p2, new
		 * Coordinate(person.getHome().x, person.getHome().y), person.hasCar(),
		 * person.hasBike(), person.useFlexiBus(), new DailyRoutine());
		 * initializePerson(person2, context, param);
		 * context.getEntityManager().addEntity(person2); } }
		 */
	}

	private void initializePerson(Person person, Context context,
			SimulationParameter param) {
		person.setContext(context);
		PlanGenerator.generateDayPlan(person);

		person.setInformed(ThreadLocalRandom.current().nextInt(100) < param.PercentInitiallyInformed);

		boolean memberOfParticipatingGroup = (param.WithWorkers && (person
				.getProfile() == Profile.WORKER))
				|| (param.WithStudents && (person.getProfile() == Profile.STUDENT))
				|| (param.WithChildren && (person.getProfile() == Profile.CHILD))
				|| (param.WithHomemaker && (person.getProfile() == Profile.HOMEMAKER));

		if (memberOfParticipatingGroup) {

			if (ThreadLocalRandom.current().nextInt(100) < param.PercentParticipating) {

				if (ThreadLocalRandom.current().nextInt(100) < param.PercentSharing)
					person.setSharing();
				else
					person.setReceiving();
			}
		}
	}

	/**
	 * Advances the simulation by one step.
	 * 
	 * @param deltaT
	 *            Time interval for this step.
	 * @throws IOException
	 */
	public void tick() throws IOException {
		// Save current day to trigger routine scheduling
		int days = context.getTime().getDays();

		// Update time
		context.getTime().tick();

		// Update world
		context.getWorld().update(context);

		// Trigger routine scheduling.
		if (days != context.getTime().getDays()) {
			Collection<Entity> persons = context.getEntityManager()
					.getEntitiesOfType(EntityTypes.PERSON);

			for (Entity p : persons) {
				PlanGenerator.generateDayPlan((Person) p);
				p.getRelations().resetBlackList();
			}
		}

		// Log streets
		boolean logged = logger.log(streetsInROI);

		if (logged) {
			StreetMap map = (StreetMap)context.getWorld();

			for (Street street : map.getStreets()) {
				street.resetUsageStatistics();
			}
		}

		/*
		 * if (context.getTime().getCurrentTime().getHour() == 3 &&
		 * context.getTime().getCurrentTime().getMinute() == 0 &&
		 * context.getTime().getCurrentTime().getSecond() == 0) {
		 * context.getStatistics().reset(); }
		 */

		// context.getWorld().getStreetMap().getNBusiestStreets(20);
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

	public Collection<Street> getStreetsInROI() {
		return streetsInROI;
	}

	public void finish() throws IOException {
		logger.close();
		threadpool.shutdown();

		try {
			threadpool.awaitTermination(2, TimeUnit.SECONDS);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
