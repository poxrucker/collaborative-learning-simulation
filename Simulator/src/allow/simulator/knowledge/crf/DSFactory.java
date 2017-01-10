package allow.simulator.knowledge.crf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import allow.simulator.core.DBConfiguration;

public class DSFactory {

	private static DBConfiguration config = null;
	
	public static void init(DBConfiguration config) {
		DSFactory.config = config;
	}
	
	public static Connection getConnection() throws SQLException {
		Connection conn = DriverManager.getConnection(config.getDBPath() + config.getDBName() + "?allowMultiQueries=true", config.getUser(), config.getPassword());
		return conn;
    }
	
}
