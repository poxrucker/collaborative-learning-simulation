package de.dfki.crf;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import allow.simulator.knowledge.EvoKnowledge;
import allow.simulator.knowledge.IExchangeStrategy;

public class CRFLocalExchangeStrategy implements IExchangeStrategy<EvoKnowledge> {

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
	
	@Override
	public boolean exchangeKnowledge(EvoKnowledge k1, EvoKnowledge k2) {
		final String tableName1 = ""; // k1.getInstanceId();
		final String tableName2 = ""; // k2.getInstanceId();
		final boolean tableExists1 = DBConnector.aIdTableExists.get(tableName1) == null ? false : true;		
		final boolean tableExists2 = DBConnector.aIdTableExists.get(tableName2) == null ? false : true;

		if (!tableExists1 && !tableExists2)
			return false;
		
		Statement stmt = null;
		Connection con = null;
		String stmtString = null;
		
		try {
			con = DSFactory.getConnection();
			stmt = con.createStatement();
			
			if (tableExists1 && !tableExists2) {
				stmtString = String.format(MY_SQL_MERGE_SIMPLE, tableName2, tableName1);
				DBConnector.aIdTableExists.put(tableName2, true);
				stmt.execute(stmtString);
			}

			if (!tableExists1 && tableExists2) {
				stmtString = String.format(MY_SQL_MERGE_SIMPLE, tableName1, tableName2);
				DBConnector.aIdTableExists.put(tableName1, true);
				stmt.execute(stmtString);
			}

			if (tableExists1 && tableExists2) {
				stmtString = String.format(MY_SQL_MERGE_MUTUAL, tableName1, tableName2);
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