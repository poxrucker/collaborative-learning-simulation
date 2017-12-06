package allow.simulator.core;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents a setup configuration for the Allow Ensembles simulator.
 * 
 * @author Andreas Poxrucker (DFKI)
 *
 */
public final class Configuration {
	// Starting date of the simulation.
	private final LocalDateTime startingDate;
	
	// Configuration of planner service.
	private final List<ServiceConfig> plannerServiceConfiguration;
	
	// Configuration of data service.
	private final List<ServiceConfig> dataServiceConfiguration;
	
	// Path to world file.
	private final WorldConfig worldConfiguration;
		
	// Path to agent configuration file.
	private final AgentConfig agentConfiguration;
	
	// EvoKnowledge configuration information.
	private final EvoKnowledgeConfig evoConfiguration;
	
	// Path to input data sources.
	private final String dataPath;
	
	/**
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
			@JsonProperty("plannerservice") List<ServiceConfig> plannerServices,
			@JsonProperty("dataservice") List<ServiceConfig> dataServices,
			@JsonProperty("world") WorldConfig worldConfig,
			@JsonProperty("agents") AgentConfig agentConfig,
			@JsonProperty("evoknowledge") EvoKnowledgeConfig evoConfig) throws ParseException {
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
	
	public EvoKnowledgeConfig getEvoKnowledgeConfiguration() {
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
	public List<ServiceConfig> getPlannerServiceConfiguration() {
		return plannerServiceConfiguration;
	}
	
	/**
	 * Returns the data service configuration.
	 * 
	 * @return Data service configuration.
	 */
	public List<ServiceConfig> getDataServiceConfiguration() {
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
