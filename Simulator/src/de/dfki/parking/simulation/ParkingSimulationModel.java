package de.dfki.parking.simulation;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import allow.simulator.core.Configuration;
import allow.simulator.core.Context;
import allow.simulator.core.EntityManager;
import allow.simulator.core.ServiceConfig;
import allow.simulator.core.SimulationParameter;
import allow.simulator.core.Time;
import allow.simulator.entity.Entity;
import allow.simulator.entity.EntityTypes;
import allow.simulator.entity.Person;
import allow.simulator.entity.PlanGenerator;
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
import allow.simulator.world.StreetMap;
import allow.simulator.world.Weather;
import allow.simulator.world.overlay.DistrictOverlay;
import allow.simulator.world.overlay.IOverlay;
import allow.simulator.world.overlay.RasterOverlay;
import de.dfki.parking.behavior.baseline.BaselineExplorationStrategy;
import de.dfki.parking.behavior.baseline.BaselineSelectionStrategy;
import de.dfki.parking.behavior.guidance.GuidanceSystemSelectionStrategy;
import de.dfki.parking.behavior.mappingdisplay.MappingDisplayExplorationStrategy;
import de.dfki.parking.behavior.mappingdisplay.MappingDisplaySelectionStrategy;
import de.dfki.parking.data.ParkingDataRepository;
import de.dfki.parking.index.ParkingIndex;
import de.dfki.parking.knowledge.ParkingKnowledge;
import de.dfki.parking.knowledge.ParkingKnowledgeFactory;
import de.dfki.parking.model.ParkingFactory;
import de.dfki.parking.model.ParkingRepository;
import de.dfki.parking.utility.ParkingPreferences;
import de.dfki.parking.utility.ParkingPreferencesFactory;
import de.dfki.parking.utility.ParkingUtility;
import de.dfki.simulation.AbstractSimulationModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Main class of simulator for collaborative learning in the scenario of
 * personalized, multi-modal urban mobility.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class ParkingSimulationModel extends AbstractSimulationModel {
  // Simulation context
  private Context context;

  // Threadpool for executing multiple tasks in parallel
  private ExecutorService threadpool;

  public static final String OVERLAY_DISTRICTS = "partitioning";
  public static final String OVERLAY_RASTER = "raster";

  /**
   * Initializes the simulator
   * 
   * @throws IOException
   */
  public void setup(Map<String, Object> parameters) throws Exception {
    threadpool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    Configuration config = (Configuration)parameters.get("config");
    SimulationParameter params = (SimulationParameter)parameters.get("params");
    
    EntityManager entityManager = new EntityManager();
    
    // Setup world
    StreetMap world = initializeStreetMap(config, params, entityManager);

    // Set scenario to normal
    params.Scenario = "";

    // Create data services
    List<IDataService> dataServices = initializeDataServices(config, world);

    // Create time
    Time time = new Time(config.getStartingDate(), 10);

    // Create weather model
    Weather weather = new Weather(config.getWeatherPath(), time);

    // Create planner services.
    List<OTPPlanner> plannerServices = new ArrayList<OTPPlanner>();
    List<ServiceConfig> plannerConfigs = config.getPlannerServiceConfiguration();

    for (int i = 0; i < plannerConfigs.size(); i++) {
      ServiceConfig plannerConfig = plannerConfigs.get(i);
      plannerServices.add(new OTPPlanner(plannerConfig.getURL(), plannerConfig.getPort(), world, dataServices.get(0), time));
    }

    // Create taxi planner service
    Coordinate taxiRank = new Coordinate(11.1198448, 46.0719489);
    TaxiPlanner taxiPlannerService = new TaxiPlanner(plannerServices, taxiRank);

    // Create bike rental service
    Coordinate bikeRentalStation = new Coordinate(11.1248895, 46.0711398);
    BikeRentalPlanner bikeRentalPlanner = new BikeRentalPlanner(plannerServices, bikeRentalStation);

    // Initialize journey planner instance
    JourneyPlanner planner = new JourneyPlanner(plannerServices, taxiPlannerService, bikeRentalPlanner, new FlexiBusPlanner(), threadpool);

    // Load ParkingDataRepository
    ParkingDataRepository parkingDataRepository = ParkingDataRepository.load(Paths.get(params.StreetParkingPath), Paths.get(params.GarageParkingPath));
    
    // Initialize ParkingRepository
    ParkingFactory parkingFactory = new ParkingFactory(params.DataScalingFactor, 0.0);
    ParkingRepository parkingRepository = ParkingRepository.initialize(parkingDataRepository, world, parkingFactory, true);
    
    // Initialize ParkingIndex
    ParkingIndex parkingIndex = ParkingIndex.build(parkingRepository);
    
    // Create global context from world, time, planner and data services, and weather
    context = new Context(world, parkingIndex, entityManager, time, planner, dataServices.get(0), weather, new Statistics(500), params, new ObjectArrayList<>());

    // Setup entities
    initializeEntities(config.getAgentConfigurationPath(), params);
    configureParkingSpotModel(context, params);

    // Create public transportation repository
    TransportationRepository repos = new TransportationRepository(context);
    context.setTransportationRepository(repos);

    // Initialize EvoKnowlegde and setup logger.
    EvoKnowledge.initialize(config.getEvoKnowledgeConfiguration(), "without", "evo_" + params.BehaviourSpaceRunNumber, threadpool);

    // Update world
    world.update();
    System.out.println("Setup simulation run " + params.BehaviourSpaceRunNumber);
  }
 
  @Override
  public void tick() {
    // Save current day to trigger routine scheduling
    int days = context.getTime().getDays();

    // Update time
    context.getTime().tick();

    // Update world
    context.getWorld().update();

    // Trigger routine scheduling.
    if (days != context.getTime().getDays()) {
      Collection<Entity> persons = context.getEntityManager().getEntitiesOfType(EntityTypes.PERSON);

      for (Entity p : persons) {
        PlanGenerator.generateDayPlan((Person) p);
        p.getRelations().resetBlackList();
      }
    }
    EvoKnowledge.invokeRequest();
    EvoKnowledge.cleanModel();
  }

  @Override
  public Context getContext() {
    return context;
  }

  @Override
  public void finish() {
    threadpool.shutdown();

    try {
      threadpool.awaitTermination(2, TimeUnit.SECONDS);

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  private StreetMap initializeStreetMap(Configuration config, SimulationParameter params, EntityManager entityManager) throws IOException {
    StreetMap world = new StreetMap(config.getMapPath());

    Path l = config.getLayerPath(OVERLAY_DISTRICTS);

    if (l == null)
      throw new IllegalStateException("Error: Missing layer with key \"" + OVERLAY_DISTRICTS + "\".");

    IOverlay districtOverlay = DistrictOverlay.parse(l, world);
    IOverlay rasterOverlay = new RasterOverlay(world.getDimensions(), params.GridResX, params.GridResY, entityManager);
    world.addOverlay(rasterOverlay, OVERLAY_RASTER);
    world.addOverlay(districtOverlay, OVERLAY_DISTRICTS);
    return world;
  }

  private List<IDataService> initializeDataServices(Configuration config, StreetMap map) throws IOException {
    List<IDataService> dataServices = new ObjectArrayList<IDataService>();
    List<ServiceConfig> dataConfigs = config.getDataServiceConfiguration();

    for (ServiceConfig dataConfig : dataConfigs) {

      if (dataConfig.isOnline()) {
        // For online queries create online data services.
        dataServices.add(new OnlineDataService(dataConfig.getURL(), dataConfig.getPort()));

      } else {
        // For offline queries create mobility repository and offline service.
        GTFSData gtfs = GTFSData.parse(Paths.get(dataConfig.getURL()));
        RoutingData routing = RoutingData.parse(Paths.get(dataConfig.getURL()), map, gtfs);
        dataServices.add(new OfflineDataService(gtfs, routing));
      }
    }
    return dataServices;
  }

  private void initializeEntities(Path config, SimulationParameter param) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    List<String> lines = Files.readAllLines(config, Charset.defaultCharset());
    List<Coordinate> homeLocations = new ArrayList<Coordinate>();

    for (String line : lines) {
      Person person = mapper.readValue(line, Person.class);

      if (person.hasCar())
        homeLocations.add(person.getHome());

      context.getEntityManager().addEntity(person);
      person.setContext(context);
      PlanGenerator.generateDayPlan(person);
    }
  }

  private void configureParkingSpotModel(Context context, SimulationParameter param) {
    // Get all persons
    Collection<Entity> persons = context.getEntityManager().getEntitiesOfType(EntityTypes.PERSON);
    
    // Get ParkingMap
    ParkingIndex parkingMap = context.getParkingMap();
   
    switch (param.Model) {

    case "Baseline":
      initializeBaselineModel(persons, parkingMap, param.ValidTime * 60);
      break;

    case "Mapping Display":
      initializeMappingDisplayModel(persons, parkingMap, param.PercentUsers, param.PercentSensorCars, param.ValidTime * 60);
      break;

    case "Central Guidance":
      initializeGuidanceSystemModel(persons, parkingMap, param.PercentUsers, param.PercentSensorCars, param.ValidTime * 60);
      break;

    default:
      throw new IllegalArgumentException();

    }
  }

  private void initializeBaselineModel(Collection<Entity> persons, ParkingIndex parkingMap, long validTime) {
    ParkingPreferencesFactory prefsFactory = new ParkingPreferencesFactory();
    ParkingKnowledgeFactory knowledgeFactory = new ParkingKnowledgeFactory(parkingMap);
    
    for (Entity entity : persons) {
      // Get person
      Person person = (Person) entity;

      // If person does not have a car, there is nothing to do
      if (!person.hasCar())
        continue;

      // Otherwise, create a new ParkingMap instance and preferences and assign them to person
      ParkingKnowledge knowledge = knowledgeFactory.createWithGarages();
      person.setLocalParkingKnowledge(knowledge);
      ParkingUtility utility = new ParkingUtility();
      person.setParkingUtility(utility);
      ParkingPreferences prefs = prefsFactory.createFromProfile(person.getProfile());
      person.setParkingPreferences(prefs);
      person.setParkingSelectionStrategy(new BaselineSelectionStrategy(knowledge, prefs, utility, validTime));
      person.setExplorationStrategy(new BaselineExplorationStrategy(knowledge, prefs, utility, parkingMap, validTime));
    }
  }

  private void initializeMappingDisplayModel(Collection<Entity> persons, ParkingIndex parkingMap,
      int percentUsers, int percentSensorCars, long validTime) {
    ParkingPreferencesFactory prefsFactory = new ParkingPreferencesFactory();
    ParkingKnowledgeFactory knowledgeFactory = new ParkingKnowledgeFactory(parkingMap);

    // Create a ParkingMap instance which is shared by Users
    ParkingKnowledge globalKnowledge = knowledgeFactory.createWithGarages();

    for (Entity entity : persons) {
      // Get person
      Person person = (Person) entity;

      // If person does not have a car, there is nothing to do
      if (!person.hasCar())
        continue;

      // Create and assign local parking map instance
      ParkingKnowledge localKnowledge = knowledgeFactory.createWithGarages();
      person.setLocalParkingKnowledge(localKnowledge);
      ParkingPreferences prefs = prefsFactory.createFromProfile(person.getProfile());
      person.setParkingPreferences(prefs);
      ParkingUtility utility = new ParkingUtility();
      person.setParkingUtility(utility);
      
      if (ThreadLocalRandom.current().nextInt(100) < percentUsers) {
        // Person is a user; set property and assign shared parking map
        person.setUser();
        person.setGlobalParkingKnowledge(globalKnowledge);
        person.setParkingSelectionStrategy(new MappingDisplaySelectionStrategy(localKnowledge, globalKnowledge, prefs, utility, validTime));
        person.setExplorationStrategy(new MappingDisplayExplorationStrategy(localKnowledge, globalKnowledge, prefs, utility, parkingMap, validTime));

        // Determine is person has a sensor car
        if (ThreadLocalRandom.current().nextInt(100) < percentSensorCars)
          person.setHasSensorCar(); 
        
      } else {
        person.setParkingSelectionStrategy(new BaselineSelectionStrategy(localKnowledge, prefs, utility, validTime));
        person.setExplorationStrategy(new BaselineExplorationStrategy(localKnowledge, prefs, utility, parkingMap, validTime));
      }
    }
  }

  private void initializeGuidanceSystemModel(Collection<Entity> persons, ParkingIndex parkingMap, 
      int percentUsers, int percentSensorCars, long validTime) {
    ParkingPreferencesFactory prefsFactory = new ParkingPreferencesFactory();

    // Create a ParkingMap instance which is shared by Users
    ParkingKnowledge globalKnowledge = new ParkingKnowledge(parkingMap);
    
    for (Entity entity : persons) {
      // Get person
      Person person = (Person) entity;

      // If person does not have a car, there is nothing to do
      if (!person.hasCar())
        continue;

      // Create and assign local parking map instance
      ParkingKnowledge localKnowledge = new ParkingKnowledge(parkingMap);   
      person.setLocalParkingKnowledge(localKnowledge);
      ParkingPreferences prefs = prefsFactory.createFromProfile(person.getProfile());
      person.setParkingPreferences(prefs);
      ParkingUtility utility = new ParkingUtility();
      person.setParkingUtility(utility);
      
      if (ThreadLocalRandom.current().nextInt(100) < percentUsers) {
        // Person is a user; set property and assign shared parking map
        person.setUser();
        person.setGlobalParkingKnowledge(globalKnowledge);
        person.setParkingSelectionStrategy(new GuidanceSystemSelectionStrategy());
        
        // Determine is person has a sensor car
        if (ThreadLocalRandom.current().nextInt(100) < percentSensorCars)
          person.setHasSensorCar();
        
      } else {
        person.setParkingSelectionStrategy(new BaselineSelectionStrategy(localKnowledge, prefs, utility, validTime));
      }
    }
  }
}
