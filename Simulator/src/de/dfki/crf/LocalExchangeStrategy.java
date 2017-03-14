package de.dfki.crf;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import allow.simulator.knowledge.Experience;
import allow.simulator.knowledge.IExchangeStrategy;

public class LocalExchangeStrategy implements IExchangeStrategy<IKnowledgeModel<Experience>> {

	private static final String MY_SQL_MERGE_SIMPLE = "CREATE TABLE IF NOT EXISTS %1$s AS SELECT * FROM %2$s; "
			+ "ALTER TABLE %1$s ADD PRIMARY KEY(nodeId, prevNodeId, weather, weekday, timeOfDay, modality);";
	
	private static final String MY_SQL_MERGE_MUTUAL = 
			"CREATE TEMPORARY TABLE ex AS (SELECT nodeId, prevNodeId, weather, "
			+ "weekday, timeOfDay, modality, ttime, prevttime, filllevel, weight FROM %1$s); "
			
			+ "INSERT INTO %1$s (SELECT * FROM %2$s) "
			+ "ON DUPLICATE KEY UPDATE "
			+ "ttime=(%1$s.ttime*%1$s.weight+VALUES(ttime))/(%1$s.weight+1), "
			+ "prevttime=(%1$s.prevttime*%1$s.weight+VALUES(prevttime))/(%1$s.weight+1), "
			+ "filllevel=(%1$s.filllevel*%1$s.weight+VALUES(filllevel))/(%1$s.weight+1), "
			+ "weight=(%1$s.weight+1); "
			
			+ "INSERT INTO %2$s (SELECT * FROM ex) "
			+ "ON DUPLICATE KEY UPDATE "
			+ "ttime=(%2$s.ttime*%2$s.weight+VALUES(ttime))/(%2$s.weight+1), "
			+ "prevttime=(%2$s.prevttime*%2$s.weight+VALUES(prevttime))/(%2$s.weight+1), "
			+ "filllevel=(%2$s.filllevel*%2$s.weight+VALUES(filllevel))/(%2$s.weight+1), "
			+ "weight=(%2$s.weight+1); ";
	
	private final DBConnector dbConnector;
	
  public LocalExchangeStrategy(DBConnector dbConnector) {
    this.dbConnector = dbConnector;
  }
  
	@Override
	public boolean exchangeKnowledge(IKnowledgeModel<Experience> k1, IKnowledgeModel<Experience> k2) {
		final boolean tableExists1 = dbConnector.tableExists(k1.getInstanceId());	
		final boolean tableExists2 = dbConnector.tableExists(k2.getInstanceId());

		if (!tableExists1 && !tableExists2)
			return false;
		
		Statement stmt = null;
		Connection con = null;
		String stmtString = null;
		
		try {
			con = dbConnector.getConnection();
			stmt = con.createStatement();
			
			if (tableExists1 && !tableExists2) {
				stmtString = String.format(MY_SQL_MERGE_SIMPLE, k2.getInstanceId(), k1.getInstanceId());
				dbConnector.addTable(k2.getInstanceId());
				stmt.execute(stmtString);
			}

			if (!tableExists1 && tableExists2) {
				stmtString = String.format(MY_SQL_MERGE_SIMPLE, k1.getInstanceId(), k2.getInstanceId());
				dbConnector.addTable(k1.getInstanceId());
				stmt.execute(stmtString);
			}

			if (tableExists1 && tableExists2) {
				stmtString = String.format(MY_SQL_MERGE_MUTUAL, k1.getInstanceId(), k2.getInstanceId());
				stmt.execute(stmtString);
			}
			
		} catch (SQLException e) {
			//System.out.println(k1.getInstanceId() + "->" + k2.getInstanceId() + ": " + e.getMessage());
			
		} finally {
			
			try {
				if (stmt != null)
					stmt.close();
				if (con != null)
					con.close();

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}