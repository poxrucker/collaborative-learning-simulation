package allow.simulator.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DBConfiguration {

	private String dbPath;
	private String dbName;
	private String user;
	private String password;
	
	@JsonCreator
	public DBConfiguration(@JsonProperty("dbPath") String dbPath,
			@JsonProperty("dbName") String dbName,
			@JsonProperty("user") String user,
			@JsonProperty("password") String password) {
		this.dbPath = dbPath;
		this.dbName = dbName;
		this.user = user;
		this.password = password;
	}
	
	public String getDBPath() {
		return dbPath;
	}

	public String getDBName() {
		return dbName;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getPassword() {
		return password;
	}
}
