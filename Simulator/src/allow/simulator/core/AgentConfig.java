package allow.simulator.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class AgentConfig {
	// Path to configuration file
	private final String url;
	
	@JsonCreator
	public AgentConfig(@JsonProperty("url") String url) {
		this.url = url;
	}
	
	public String getAgentConfigurationFile() {
		return url;
	}
}
