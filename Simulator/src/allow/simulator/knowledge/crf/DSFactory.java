package allow.simulator.knowledge.crf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import allow.simulator.core.EvoKnowledgeConfig;

public class DSFactory {

	private static EvoKnowledgeConfig config = null;
	
	public static void init(EvoKnowledgeConfig config) {
		DSFactory.config = config;
	}
	
	public static Connection getConnection() throws SQLException {
		Connection conn = DriverManager.getConnection(config.getModelPath() + config.getModelName() + "?allowMultiQueries=true", config.getUser(), config.getPassword());
		return conn;
    }
	
}
