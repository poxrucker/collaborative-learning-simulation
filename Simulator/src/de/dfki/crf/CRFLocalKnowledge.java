package de.dfki.crf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import allow.simulator.knowledge.Experience;

public class CRFLocalKnowledge implements IKnowledgeModel<Experience> {
	
	private static final String MY_SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS %1$s "
			+ "(nodeId INT, prevNodeId INT, weather TINYINT UNSIGNED, weekday TINYINT UNSIGNED, "
			+ "timeOfDay TINYINT UNSIGNED, modality TINYINT UNSIGNED, ttime FLOAT, prevttime FLOAT, "
			+ "fillLevel FLOAT, weight DOUBLE, "
			+ "PRIMARY KEY(nodeId, prevNodeId, weather, weekday, timeOfDay, modality));%2$s";
	
	private static final String SQL_INSERT_VALUES = "INSERT INTO %1$s "
			+ " (nodeId, prevNodeId, weather, weekday, timeOfDay, modality, ttime, prevttime, fillLevel, weight)"
			+ " VALUES ";
	
	private static final String MY_SQL_UPDATE_ON_INSERT = "ON DUPLICATE KEY UPDATE "
			+ "ttime=(%1$s.ttime * %1$s.weight + VALUES(ttime)) / (%1$s.weight + 1), "
			+ "prevttime=(%1$s.prevttime * %1$s.weight + VALUES(prevttime)) / (%1$s.weight + 1), "
			+ "filllevel=(%1$s.filllevel * %1$s.weight + VALUES(filllevel)) / (%1$s.weight + 1), "
			+ "weight=LN(EXP(%1$s.weight) + EXP(1))";
	
	private static final String MY_SQL_MERGE_SIMPLE = "CREATE TABLE IF NOT EXISTS %1$s AS SELECT * FROM %2$s; "
			+ "ALTER TABLE %1$s ADD PRIMARY KEY(nodeId, prevNodeId, weather, weekday, timeOfDay, modality);";
	
	private static final String MY_SQL_MERGE_MUTUAL = 
			"CREATE TEMPORARY TABLE ex AS (SELECT nodeId, prevNodeId, weather, "
			+ "weekday, timeOfDay, modality, ttime, prevttime, filllevel, weight FROM %1$s); "
			
			+ "INSERT INTO %1$s (SELECT * FROM %2$s) "
			+ "ON DUPLICATE KEY UPDATE "
			+ "ttime=(%1$s.ttime*%1$s.weight+VALUES(ttime)*VALUES(weight))/(%1$s.weight+VALUES(weight)), "
			+ "prevttime=(%1$s.prevttime*%1$s.weight+VALUES(prevttime)*VALUES(weight))/(%1$s.weight+VALUES(weight)), "
			+ "filllevel=(%1$s.filllevel*%1$s.weight+VALUES(filllevel)*VALUES(weight))/(%1$s.weight+VALUES(weight)), "
			+ "weight=LN(EXP(%1$s.weight)+EXP(VALUES(weight))); "
			
			+ "INSERT INTO %2$s (SELECT * FROM ex) "
			+ "ON DUPLICATE KEY UPDATE "
			+ "ttime=(%2$s.ttime*%2$s.weight+VALUES(ttime)*VALUES(weight))/(%2$s.weight+VALUES(weight)), "
			+ "prevttime=(%2$s.prevttime*%2$s.weight+VALUES(prevttime)*VALUES(weight))/(%2$s.weight+VALUES(weight)), "
			+ "filllevel=(%2$s.filllevel*%2$s.weight+VALUES(filllevel)*VALUES(weight))/(%2$s.weight+VALUES(weight)), "
			+ "weight=LN(EXP(%2$s.weight)+EXP(VALUES(weight))); ";
		
	private final String sqlCreateTables;
	private final String sqlInsertValues;
	private final String sqlUpdateOnInsert;
	private final String sqlMergeSimple;
	private final String sqlMergeMutual;
	private final DBConnector dbConnector;
	private final String instanceId;
	
	public CRFLocalKnowledge(String instanceId, DBConnector dbConnector) {
	  sqlCreateTables = MY_SQL_CREATE_TABLE;
		sqlInsertValues = SQL_INSERT_VALUES;
		sqlUpdateOnInsert = MY_SQL_UPDATE_ON_INSERT;
		sqlMergeSimple = MY_SQL_MERGE_SIMPLE;
		sqlMergeMutual = MY_SQL_MERGE_MUTUAL;
		this.dbConnector = dbConnector;
		this.instanceId = instanceId;
	}
	
	@Override
	public String getInstanceId() {
	  return instanceId;
	}
	
	@Override
	public boolean learn(List<Experience> dataPoints) {
		
	  if (dataPoints.size() == 0)
			return false;

		// check if table for agent already exists (hopefully saves database overhead)
		boolean tableExists = dbConnector.tableExists(instanceId);

		// track error state to avoid having to nest too many try catch statements
		boolean error = false;

		String stmtString = "";
		
		try (Connection con = dbConnector.getConnection(); PreparedStatement stmt = con.prepareStatement("")) {

			// create a new table for an agent representing his EvoKnowledge if it doesnt exist already
			if (!tableExists) {
				try {
					stmt.execute(String.format(sqlCreateTables, instanceId, ""));
				
				} catch (SQLException e) {
					e.printStackTrace();
					stmt.close();
					con.close();
					return false;
				}
				dbConnector.addTable(instanceId);
			}

			// parse the itinerary and add a line for each entry
			stmtString = String.format(sqlInsertValues, instanceId);

			boolean firstSeg = true;
			long prevNodeId = -1;
			double prevDuration = -1;

			for (Experience ex : dataPoints) {
				long nodeId = ex.getSegmentId();
				double duration = ex.getTravelTime();

				stmtString = stmtString.concat(firstSeg ? "" : ",");
				stmtString = stmtString.concat("('" + nodeId + "',");
				stmtString = stmtString.concat("'" + prevNodeId + "',");
				stmtString = stmtString.concat(DBEncoding.encodeWeather(ex.getWeather()) + ",");
				stmtString = stmtString.concat(DBEncoding.encodeDayOfWeek(ex.getWeekday()) + ",");
				stmtString = stmtString.concat(DBEncoding.encodeTimeOfDay(ex.getTStart()) + ",");
				stmtString = stmtString.concat(DBEncoding.encodeTType(ex.getTransportationMean()) + ",");
				stmtString = stmtString.concat(duration + ",");
				stmtString = stmtString.concat(prevDuration + ",");
				stmtString = stmtString.concat(ex.getPublicTransportationFillingLevel() + ",");
				stmtString = stmtString.concat("1) ");
				firstSeg = false;
				prevNodeId = nodeId;
				prevDuration = duration;
			}
			stmtString = stmtString.concat(String.format(sqlUpdateOnInsert, instanceId));
			stmtString = stmtString.concat(";");

			// System.out.println(stmtString);
			stmt.execute(stmtString);

		} catch (SQLException e) {
			System.out.println(stmtString);
			// e.printStackTrace();
			error = true;
		}
		return !error;
	}

	@Override
	public List<Experience> predict(List<Experience> observations) {
		// connection and statement for database query
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		boolean tableExists = dbConnector.tableExists(instanceId);
		
		if (!tableExists)
			return observations;
		
		try {
			// get connection
			con = dbConnector.getConnection();
			stmt = con.createStatement();
			String stmt1 = "SELECT %1$s FROM " + instanceId + " WHERE nodeId = %2$d";
			String stmt2 = stmt1.concat(" AND modality = %3$d");
			String stmt3 = stmt2.concat(" AND timeOfDay = %4$d");
			String stmt4 = stmt3.concat(" AND weekday = %5$d");
			String stmt5 = stmt4.concat(" AND prevNodeId = %6$d AND (prevttime BETWEEN %7$d AND %8$d)");

			String stmtString = null;
			boolean firstSeg = true;

			long prevNodeId = -1;
			double prevTTime = -1;
			long segmentTStart = 0;

			for (Experience ex : observations) {
				
				if (firstSeg)
					segmentTStart = ex.getStartingTime() / 1000;

				if (ex.isTransient())
					continue;

				double predictedTravelTime = ex.getTravelTime();
				double predictedFillLevel = 0.0;	
				boolean foundMatch = false;
				long nodeId = ex.getSegmentId();
				byte modality = DBEncoding.encodeTType(ex.getTransportationMean());
				byte timeOfDay = DBEncoding.encodeTimeOfDay(ex.getTStart());
				byte weekday = DBEncoding.encodeDayOfWeek(ex.getWeekday());
				
				// try the most detailed query first
				if (!firstSeg && prevTTime != -1) {
					stmtString = String.format(stmt5, "AVG(ttime), AVG(fillLevel)", nodeId,
							modality, timeOfDay, weekday, prevNodeId,
							Math.round((double) prevTTime * 0.7),
							Math.round((double) prevTTime * 1.3));
					rs = stmt.executeQuery(stmtString);

					if (rs.next() && (rs.getDouble(1) > 0)) {
						predictedTravelTime = rs.getDouble(1);
						predictedFillLevel = rs.getDouble(2);
						foundMatch = true;
					}
					rs.close();
				}

				// Try without previous node
				if (!foundMatch) {
					stmtString = String.format(stmt4, "AVG(ttime), AVG(fillLevel)", nodeId,
							modality, timeOfDay, weekday);
					rs = stmt.executeQuery(stmtString);

					if (rs.next() && (rs.getDouble(1) > 0)) {
						predictedTravelTime = rs.getDouble(1);
						predictedFillLevel = rs.getDouble(2);
						foundMatch = true;
					}
					rs.close();
				}

				// Try without weekday
				if (!foundMatch) {
					stmtString = String.format(stmt3, "AVG(ttime), AVG(fillLevel)", nodeId, modality, timeOfDay);
					rs = stmt.executeQuery(stmtString);

					if (rs.next() && (rs.getDouble(1) > 0)) {
						predictedTravelTime = rs.getDouble(1);
						predictedFillLevel = rs.getDouble(2);
						foundMatch = true;
					}
					rs.close();
				}

				// Try without time of day
				if (!foundMatch) {
					stmtString = String.format(stmt2, "AVG(ttime), AVG(fillLevel)", nodeId, modality);
					rs = stmt.executeQuery(stmtString);

					if (rs.next() && (rs.getDouble(1) > 0)) {
						predictedTravelTime = rs.getDouble(1);
						predictedFillLevel = rs.getDouble(2);
						foundMatch = true;
					}
					rs.close();
				}

				// try without modality
				if (!foundMatch) {
					stmtString = String.format(stmt1, "AVG(ttime), AVG(fillLevel)", nodeId);
					rs = stmt.executeQuery(stmtString);

					if (rs.next() && (rs.getDouble(1) > 0)) {
						predictedTravelTime = rs.getDouble(1);
						predictedFillLevel = rs.getDouble(2);
						foundMatch = true;
					}
					rs.close();
				}
				// Estimate actual ttime
				firstSeg = false;
				prevNodeId = nodeId;
				prevTTime = predictedTravelTime;

				ex.setStartingTime(segmentTStart * 1000);
				segmentTStart = segmentTStart
						+ ((int) predictedTravelTime * 1000);
				ex.setEndTime(segmentTStart * 1000);
				ex.setTravelTime(predictedTravelTime);
				ex.setPublicTransportationFillingLevel(predictedFillLevel);
			}

		} catch (SQLException e) {
			e.printStackTrace();

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
				return null;
			}
		}
		return observations;
	}

	@Override
	public void clean() { }

	@Override
	public boolean merge(IKnowledgeModel<Experience> other) {
		boolean tableExists1 = dbConnector.tableExists(instanceId);
		boolean tableExists2 = dbConnector.tableExists(other.getInstanceId());

		if (!tableExists1 && !tableExists2)
			return false;
		
		Statement stmt = null;
		Connection con = null;
		String stmtString = null;
		
		try {
			con = dbConnector.getConnection();
			stmt = con.createStatement();
			
			if (tableExists1 && !tableExists2) {
				stmtString = String.format(sqlMergeSimple, other.getInstanceId(), instanceId);
				dbConnector.addTable(other.getInstanceId());
				stmt.execute(stmtString);
			}

			if (!tableExists1 && tableExists2) {
				stmtString = String.format(sqlMergeSimple, instanceId, other.getInstanceId());
				dbConnector.addTable(instanceId);
				stmt.execute(stmtString);
			}

			if (tableExists1 && tableExists2) {
				stmtString = String.format(sqlMergeMutual, instanceId, other.getInstanceId());
				stmt.execute(stmtString);
			}
			
		} catch (SQLException e) {
			System.out.println(instanceId + "->" + other.getInstanceId() + ": ");
			e.printStackTrace();
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
