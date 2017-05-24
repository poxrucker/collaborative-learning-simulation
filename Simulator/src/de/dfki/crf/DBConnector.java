package de.dfki.crf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DBConnector {
	// Database configuration to create connections
	private final String dbUrl;
	private final String dbName;
	private final String userName;
	private final String password;
	
	// Saves which tables already exist
	private final Set<String> tableCache = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	
	public DBConnector(String dbUrl, String dbName, String userName, String password) {
	  this.dbUrl = dbUrl;
	  this.dbName = dbName;
	  this.userName = userName;
	  this.password = password;
	}
	
	public void init(String tablePrefix) throws ClassNotFoundException {
	  initDatabase(tablePrefix);
	  initTableCache(tablePrefix);
	}
	
	public Connection getConnection() throws SQLException {
    Connection conn = DriverManager.getConnection(dbUrl + dbName + "?allowMultiQueries=true", userName, password);
    return conn;
  }
	
	public boolean tableExists(String tableName) {
	  return tableCache.contains(tableName);
	}
	
	public boolean addTable(String tableName) {
	  return tableCache.add(tableName);
	}
	
	private void initDatabase(String tablePrefix) throws ClassNotFoundException {
    // Initialize driver
    Class.forName("com.mysql.jdbc.Driver");
    
    try (Connection con = getConnection(); Statement stmt = con.createStatement()) {  
      // Database creation
      stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
      
      try (ResultSet queries = stmt.executeQuery("SELECT CONCAT(\"DROP TABLE \", table_name, \";\") "
          + "FROM information_schema.tables WHERE table_schema = \"" + dbName + "\" "
          + "AND table_name LIKE \"" + tablePrefix + "%\";")) {
        
        // Deleting previous tables
        try(Statement stmt2 = con.createStatement()) {
        
          while (queries.next()) {
            stmt2.executeUpdate(queries.getString(1));
          }
          stmt2.executeUpdate("DROP TABLE IF EXISTS " + tablePrefix + "_tbl_global;");
          }
      }
      
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  private void initTableCache(String tablePrefix) {
    tableCache.clear();   
    String tableName = tablePrefix + "_tbl_%";
    
    try (Connection con = getConnection(); Statement stmt = con.createStatement()){
      
      try (ResultSet rs = stmt.executeQuery("SHOW TABLES FROM " + dbName + " LIKE '" + tableName + "'")) {

        while (rs.next()) {
          String table = rs.getString(1);
          String[] tokens = table.split("_");
          tableCache.add(tokens[tokens.length - 1]);
        }
      }
      
    } catch (SQLException e) {
      e.printStackTrace();
    } 
  }
}