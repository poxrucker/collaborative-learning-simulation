package allow.simulator.knowledge.crf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import allow.simulator.core.EvoKnowledgeConfig;
import allow.simulator.entity.Entity;
import allow.simulator.knowledge.Experience;

public class DBConnector {
	
	public enum DBType {
		
		MYSQL,
		
		POSTGRE;
	}
	
	// Dictionary holding tables which have been 
	private static String prefix = null;
	private static EvoKnowledgeConfig config;
	private static CRFKnowledgeModel model;
	private static DBType dbType;
	
	// Saves which tables already exist
	public static ConcurrentHashMap<String, Boolean> aIdTableExists = new ConcurrentHashMap<String, Boolean>();

	private static final String KNOWLEDGE_MODEL_NO_KNOWLEDGE = "without";
	private static final String KNOWLEDGE_MODEL_LOCAL = "local";
	private static final String KNOWLEDGE_MODEL_LOCAL_EXCHANGE = "local (with exchange)";
	private static final String KNOWLEDGE_MODEL_GLOBAL_TEMPORAL = "global";
	private static final String KNOWLEGDE_MODEL_REGIONAL = "regional";
	
	private static void initMySQL() {
		Connection con = null, con2 = null;
		Statement stmt = null, stmt2 = null;
		ResultSet queries = null;
		
		try {
			// Init driver
			Class.forName("com.mysql.jdbc.Driver");
			
			// Creating database
			con = DriverManager.getConnection(config.getModelPath(), config.getUser(), config.getPassword());
			stmt = con.createStatement();
			stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + config.getModelName());
			
			queries = stmt.executeQuery("SELECT CONCAT(\"DROP TABLE \", table_name, \";\") "
					+ "FROM information_schema.tables WHERE table_schema = \"" + config.getModelName() + "\" "
					+ "AND table_name LIKE \"" + prefix + "%\";");

			// Deleting previous tables
			con2 = DriverManager.getConnection(config.getModelPath() + config.getModelName(), config.getUser(), config.getPassword());
			stmt2 = con2.createStatement();
			
			while (queries.next()) {
				stmt2.executeUpdate(queries.getString(1));
			}
			stmt2.executeUpdate("DROP TABLE IF EXISTS " + prefix + "_tbl_global;");

		} catch (SQLException e) {
			e.printStackTrace();
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			
		} finally {
			
			try {
				if (stmt != null) stmt.close();
				if (con != null) con.close();
				if (stmt2 != null) stmt2.close();
				if (con2 != null) con2.close();
				if (queries != null) queries.close();
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*private static void initPostgre() {
		Connection con = null;
		Statement stmt = null;
		
		try {
			Class.forName("org.postgresql.Driver");
			
			// Reset tables if they exist.
			con = DriverManager.getConnection(config.getModelPath() + config.getModelName(), config.getUser(), config.getPassword());
			stmt = con.createStatement();
			Collection<Entity> persons = Simulator.Instance().getContext().getEntityManager().getEntitiesOfType(EntityTypes.PERSON);
			System.out.println(persons.size());
			
			for (Entity e : persons) {
				stmt.executeUpdate("DROP TABLE IF EXISTS " + prefix + "_tbl_" + e.getId() + ";");
			}
			stmt.executeUpdate("DROP TABLE IF EXISTS " + prefix + "_tbl_global;");

		} catch (SQLException e) {
			e.printStackTrace();
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			
		} finally {
			
			try {
				if (stmt != null) stmt.close();
				if (con != null) con.close();
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}*/
	
	private static void initaIdTableExists() {
		aIdTableExists.clear();
		Statement stmt = null;
		Connection con = null;
		ResultSet rs = null;
		
		String stmtString = "";
		String tableName = prefix + "_tbl_%";
		
		try {
			// get connection
			con = DSFactory.getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery("SHOW TABLES FROM " + config.getModelName() + " LIKE '" + tableName + "'");
			
			while (rs.next()) {
				String table = rs.getString(1);
				String[] tokens = table.split("_");
				aIdTableExists.put(tokens[tokens.length - 1], true);
			}

		} catch (SQLException e) {
			System.out.println(stmtString);
			// e.printStackTrace();

		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (con != null)
					con.close();
				if (rs != null)
					rs.close();
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void initDatabase() {
		
		if (config.getModelPath().contains("mysql")) {
			dbType = DBType.MYSQL;
			initMySQL();
			
		} else if (config.getModelPath().contains("postgres")) {
			dbType = DBType.POSTGRE;
			throw new UnsupportedOperationException("Error: Postgresql currently not supported.");
			//initPostgre();
			
		} else {
			throw new IllegalArgumentException("Error: Unknown database driver.");
		}
		System.out.println("EvoKnowledge database connector initialized.");
	}
	
	public static void init(EvoKnowledgeConfig config, String knowledgeModel, String prefix) {
		DBConnector.prefix = prefix;
		DBConnector.config = config;
		DSFactory.init(config);
		
		switch (knowledgeModel) {
			case KNOWLEDGE_MODEL_NO_KNOWLEDGE:
				model = CRFNoKnowledge.getInstance();
				break;
				
			case KNOWLEDGE_MODEL_LOCAL:
			case KNOWLEDGE_MODEL_LOCAL_EXCHANGE:
				initDatabase();
				initaIdTableExists();
				model = new CRFLocalKnowledge(dbType, prefix, config.getModelName());
				break;
				
			case KNOWLEDGE_MODEL_GLOBAL_TEMPORAL:
				initDatabase();
				initaIdTableExists();
				model = new CRFGlobalKnowledge(dbType);
				break;
			
			case KNOWLEGDE_MODEL_REGIONAL:
				initDatabase();
				initaIdTableExists();
				model = new CRFRegionalKnowledge(dbType, prefix, config.getModelName());
				break;
				
			default:
				throw new IllegalArgumentException("Error: Knowledge model \"" + knowledgeModel  + "\" unknown.");
		}		
	}
	
	public static boolean addEntry(Entity agent, List<Experience> entries) {
		return model.addEntry(agent, entries, prefix);
	}
	
	public static List<Experience> getPredictedItinerary(Entity agent, List<Experience> it) {
		return model.getPredictedItinerary(agent, it, prefix);
	}
	
	public static void cleanModel(Entity agent) {
		model.clean(agent, prefix);
	}
	
	public static boolean exchangeKnowledge(Entity agent1, Entity agent2) {
		return model.exchangeKnowledge(agent1, agent2, prefix);
	}
}