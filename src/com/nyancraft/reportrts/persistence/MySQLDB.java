package com.nyancraft.reportrts.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.nyancraft.reportrts.ReportRTS;

import lib.PatPeter.SQLibrary.MySQL;

public class MySQLDB extends SQLDB {
	
	private MySQL db;
	
	public ResultSet query(String query){
		try{
			return db.query(query);
		}catch(Exception e){
			return null;
		}
	}
	
	public boolean connect(){
		ReportRTS.getPlugin().getLogger().info("Connecting to MySQL.");
		db = new MySQL(
				ReportRTS.getPlugin().getLogger(),
				"[MySQL]",
				ReportRTS.getPlugin().mysqlHostname,
				ReportRTS.getPlugin().mysqlPort,
				ReportRTS.getPlugin().mysqlDatabase,
				ReportRTS.getPlugin().mysqlUsername,
				ReportRTS.getPlugin().mysqlPassword);
		try{
			db.open();
			if(!db.checkConnection()) return false;
		}catch(Exception e){
			ReportRTS.getPlugin().getLogger().severe("Failed to connect to the MySQL database.");
			return false;
		}
		
		try{
			checkTables();
		}catch(Exception e){
			ReportRTS.getPlugin().getLogger().severe("Could not access MySQL tables.");
			return false;
		}
		ReportRTS.getPlugin().getLogger().info("Successfully connected and checked tables, will use MySQL.");
		return true;
	}
	
	private boolean checkTables() throws Exception{
		if(!this.db.checkTable("reportrts_request")){
			if(!db.createTable(QueryGen.createRequestTable())) return false;
			ReportRTS.getPlugin().getLogger().info("Created reportrts_request table.");
		}
		if(!this.db.checkTable("reportrts_user")){
			if(!db.createTable(QueryGen.createUserTable())) return false;
			ReportRTS.getPlugin().getLogger().info("Created reportrts_user table.");
		}
		return true;
	}
	
	public void disconnect(){
		db.close();
	}

	@Override
	public Connection connection() {
		return db.getConnection();
	}

	@Override
	public boolean resetDB() {
		db.query("TRUNCATE TABLE reportrts_request");
		db.query("TRUNCATE TABLE reportrts_user");
		return true;
	}
	
	@Override
	public boolean checkTable(String table){
		if(!db.checkTable(table)) return false;
		return true;
	}
}