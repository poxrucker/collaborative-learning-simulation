package allow.simulator.core;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents a setup configuration for the Allow Ensembles simulator.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public final class Configuration {
  
  public final class AgentConfiguration {
    // Path to configuration file
    private final String url;
    
    @JsonCreator
    public AgentConfiguration(@JsonProperty("url") String url) {
      this.url = url;
    }
    
    public String getAgentConfigurationFile() {
      return url;
    }
  }
  
  public class KnowledgeConfiguration {

    private String modelPath;
    private String modelName;
    private String user;
    private String password;
    
    @JsonCreator
    public KnowledgeConfiguration(@JsonProperty("modelPath") String modelPath,
        @JsonProperty("modelName") String modelName,
        @JsonProperty("user") String user,
        @JsonProperty("password") String password) {
      this.modelPath = modelPath;
      this.modelName = modelName;
      this.user = user;
      this.password = password;
    }
    
    public String getModelPath() {
      return modelPath;
    }

    public String getModelName() {
      return modelName;
    }
    
    public String getUser() {
      return user;
    }
    
    public String getPassword() {
      return password;
    }
  }

  /**
   * Describes the configuration of a service.
   * 
   * @author Andreas Poxrucker (DFKI)
   *
   */
  public class ServiceConfiguration {
    
    /**
     * Service URL.
     */
    private String url;
    
    /**
     * Service port.
     */
    private int port;
    
    /**
     * Constructor.
     * Creates a new service description specifying URL and port.
     * 
     * @param url URL of service.
     * @param port Port to use.
     */
    @JsonCreator
    public ServiceConfiguration(@JsonProperty("url") String url, @JsonProperty("port") int port) {
      this.url = url;
      this.port = port;
    }
    
    
    /**
     * Returns if service is available over network (i.e. port is not -1).
     * 
     * @return True, if service is a network service, false otherwise.
     */
    public boolean isOnline() {
      return (port != -1);
    }
    
    /**
     * Returns URL of service in case of web service or directory containing configuration for
     * local (emulated) service.
     * 
     * @return URL of service or working directory for local service.
     */
    public String getURL() {
      return url;
    }
    
    /**
     * Returns port of service in case of web service or -1 for local (emulated) service.
     * 
     * @return Port of service or -1 for local service.
     */
    public int getPort() {
      return port;
    }
  }

  /**
   * Utility class to load/store configuration aspects of the simulated world.
   * 
   * @author Andreas Poxrucker (DFKI)
   *
   */
  public class WorldConfiguration {
    // Path to world map. 
    private String map;
    
    // List of layers to be added to the map.
    private Map<String, String> layer;
    
    // Weather model.
    private String weather;
    
    /**
     * Constructor.
     * Creates a new world configuration providing paths to the map to use,
     * the layers to add to the map, and the weather model.
     * 
     * @param mapPath Path to map to use.
     * @param layerPaths Layers to be added to the map.
     * @param weatherPath Weather model to simulate.
     */
    @JsonCreator
    public WorldConfiguration(@JsonProperty("map") String mapPath,
        @JsonProperty("layer") Map<String, String> layerPaths,
        @JsonProperty("weather") String weatherPath) {
      map = mapPath;
      layer = layerPaths;
      weather = weatherPath;
    }
    
    /**
     * Returns the path to the file containing the street map of the
     * simulation.
     * 
     * @return Path to file containing the street map of the simulation.
     */
    public String getMapFile() {
      return map;
    }
    
    /**
     * Returns path to a file describing the layer specified by parameter key to
     * be add to the map e.g. partitioning of map into residential and working
     * areas.
     * 
     * @return Path to file containing layer associated with given key, null
     * otherwise.
     */
    public String getLayerPath(String key) {
      return layer.containsKey(key) ? layer.get(key) : null;
    }
    
    /**
     * Returns the path to the weather model of the simulation.
     * 
     * @return Path to weather model of the simulation.
     */
    public String getWeatherFiles() {
      return weather;
    }
  }

	// Starting date of the simulation.
	private final LocalDateTime startingDate;
	
	// Configuration of planner service.
	private final List<ServiceConfiguration> plannerServiceConfiguration;
	
	// Configuration of data service.
	private final List<ServiceConfiguration> dataServiceConfiguration;
	
	// Path to world file.
	private final WorldConfiguration worldConfiguration;
		
	// Path to agent configuration file.
	private final AgentConfiguration agentConfiguration;
	
	// EvoKnowledge configuration information.
	private final KnowledgeConfiguration evoConfiguration;
	
	// Path to input data sources.
	private final String dataPath;
	
	/**
	 * Constructor.
	 * Creates a new configuration for the Allow Ensembles simulator.
	 * 
	 * @param dataPath Path to input data sources of simulation.
	 * @param startingDate Starting date of the simulation.
	 * @param plannerService Planner service configuration.
	 * @param dataService Data service configuration.
	 * @param loggingPath Path to file to write logging output to.
	 * @param worldPath Path containing simulated world.
	 * @throws ParseException 
	 */
	@JsonCreator
	public Configuration(@JsonProperty("datapath") String dataPath,
			@JsonProperty("startingdate") String startingDate, 
			@JsonProperty("plannerservice") List<ServiceConfiguration> plannerServices,
			@JsonProperty("dataservice") List<ServiceConfiguration> dataServices,
			@JsonProperty("world") WorldConfiguration worldConfig,
			@JsonProperty("agents") AgentConfiguration agentConfig,
			@JsonProperty("evoknowledge") KnowledgeConfiguration evoConfig) {
		this.dataPath = dataPath;
		this.startingDate = LocalDateTime.parse(startingDate, DateTimeFormatter.ofPattern("dd.MM.uuuu HH:mm:ss", Locale.ITALY));
		this.plannerServiceConfiguration = plannerServices;
		this.dataServiceConfiguration = dataServices;
		this.worldConfiguration = worldConfig;
		this.agentConfiguration = agentConfig;
		this.evoConfiguration = evoConfig;
	}
	
	/**
	 * Returns the path to map file to use.
	 * 
	 * @return Path to map file to use.
	 */
	public Path getMapPath() {
		return Paths.get(dataPath, worldConfiguration.getMapFile());
	}
	
	public Path getLayerPath(String key) {
		String path = worldConfiguration.getLayerPath(key);
		return (path != null) ? Paths.get(dataPath, path) : null;
	}
	
	public Path getWeatherPath() {
		return Paths.get(dataPath, worldConfiguration.getWeatherFiles());
	}
	
	public Path getAgentConfigurationPath() {
		return Paths.get(dataPath, agentConfiguration.getAgentConfigurationFile());
	}
	
	public KnowledgeConfiguration getEvoKnowledgeConfiguration() {
		return evoConfiguration;
	}
	
	/**
	 * Returns the starting date of the simulated world.
	 * 
	 * @return Starting date of the simulated world.
	 */
	public LocalDateTime getStartingDate() {
		return startingDate;
	}
	
	/**
	 * Returns the planner service configuration.
	 * 
	 * @return Planner service configuration.
	 */
	public List<ServiceConfiguration> getPlannerServiceConfiguration() {
		return plannerServiceConfiguration;
	}
	
	/**
	 * Returns the data service configuration.
	 * 
	 * @return Data service configuration.
	 */
	public List<ServiceConfiguration> getDataServiceConfiguration() {
		return dataServiceConfiguration;
	}
	
	/**
	 * Returns a new configuration from JSON file.
	 * 
	 * @param filePath File containing JSON description of configuration.
	 * @return Configuration read from JSON file.
	 * @throws IOException
	 */
	public static Configuration fromJSON(Path filePath) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(filePath.toFile(), Configuration.class);
	}
}
