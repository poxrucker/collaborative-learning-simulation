package allow.simulator.entity.knowledge.crf;

public class CRFQuerys {

	public static final String MY_SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS %1$s "
			+ "(nodeId INT, prevNodeId INT, weather TINYINT UNSIGNED, weekday TINYINT UNSIGNED, "
			+ "timeOfDay TINYINT UNSIGNED, modality TINYINT UNSIGNED, ttime FLOAT,"
			+ "prevttime FLOAT, fillLevel FLOAT, weight DOUBLE, "
			+ "PRIMARY KEY(nodeId, prevNodeId, weather, weekday, timeOfDay, modality));%2$s";
	
	public static final String SQL_INSERT_VALUES = "INSERT INTO %1$s "
			+ " (nodeId, prevNodeId, weather, weekday, timeOfDay, modality, ttime, prevttime, fillLevel, weight)"
			+ " VALUES ";
	
	public static final String MY_SQL_UPDATE_ON_INSERT = "ON DUPLICATE KEY UPDATE "
			+ "ttime=(%1$s.ttime * %1$s.weight + VALUES(ttime)) / (%1$s.weight + 1), "
			+ "prevttime=(%1$s.prevttime * %1$s.weight + VALUES(prevttime)) / (%1$s.weight + 1), "
			+ "filllevel=(%1$s.filllevel * %1$s.weight + VALUES(filllevel)) / (%1$s.weight + 1), "
			+ "weight=LN(EXP(%1$s.weight) + EXP(1))";
	
	public static final String MY_SQL_MERGE_SIMPLE = "CREATE TABLE IF NOT EXISTS %1$s AS SELECT * FROM %2$s; "
			+ "ALTER TABLE %1$s ADD PRIMARY KEY(nodeId, prevNodeId, weather, weekday, timeOfDay, modality); ";
	
	public static final String MY_SQL_MERGE_MUTUAL = 
			"CREATE TEMPORARY TABLE ex AS (SELECT nodeId, prevNodeId, weather, "
			+ "weekday, timeOfDay, modality, ttime, prevttime, filllevel, weight FROM %1$s); "
			
			+ "INSERT INTO %1$s (SELECT * FROM %2$s) "
			+ "ON DUPLICATE KEY UPDATE "
			+ "ttime=(%1$s.ttime*%1$s.weight+VALUES(ttime)*VALUES(weight))/(%1$s.weight+VALUES(weight)), "
			+ "prevttime=(%1$s.prevttime*%1$s.weight+VALUES(prevttime)*VALUES(weight))/(%1$s.weight+VALUES(weight)), "
			+ "filllevel=(%1$s.filllevel*%1$s.weight+VALUES(filllevel))*VALUES(weight)/(%1$s.weight+VALUES(weight)), "
			+ "weight=LN(EXP(%1$s.weight)+EXP(VALUES(weight))); "
			
			+ "INSERT INTO %2$s (SELECT * FROM ex) "
			+ "ON DUPLICATE KEY UPDATE "
			+ "ttime=(%2$s.ttime*%2$s.weight+VALUES(ttime)*VALUES(weight))/(%2$s.weight+VALUES(weight)), "
			+ "prevttime=(%2$s.prevttime*%2$s.weight+VALUES(prevttime)*VALUES(weight))/(%2$s.weight+VALUES(weight)), "
			+ "filllevel=(%2$s.filllevel*%2$s.weight+VALUES(filllevel)*VALUES(weight))/(%2$s.weight+VALUES(weight)), "
			+ "weight=LN(EXP(%2$s.weight)+EXP(VALUES(weight))); ";
	
}
