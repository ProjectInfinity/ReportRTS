package com.nyancraft.reportrts;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.Location;

import com.nyancraft.reportrts.data.HelpRequest;

import lib.PatPeter.SQLibrary.*;

public class RTSDatabaseManager {
	public static File dbFolder = new File("plugins/ReportRTS");
    public static SQLite db = new SQLite(ReportRTS.getPlugin().getLogger(), "[ReportRTS]", "ReportRTS", dbFolder.getPath());
 
    /**
     * Initializes, opens and confirms the tables and database.
     */
    public static void enableDB(){
    	db.initialize();
    	db.open();
    	confirmTables();
    }
    
    /**
     * Closes the database.
     */
    public static void disableDB(){ if(db.checkConnection()) db.close(); }
    
    /**
     * Gets number of open requests from the database and puts them into the requestMap.
     * @return Integer amount of requests
     */    
    public static int getOpenRequests(){
    	
    	if(!db.checkConnection()) return 0;
    	int openRequests = 0;
    	
    	try {
        	ResultSet result = db.query("SELECT * FROM reportrts_request as request INNER JOIN reportrts_user as user ON request.user_id = user.id WHERE `status` < 2");
			while(result.next()){
				ReportRTS.getPlugin().requestMap.put(result.getInt(1), new HelpRequest(result.getString("name"), result.getInt(1), result.getLong("tstamp"), result.getString("text"), result.getInt("status"), result.getInt("x"), result.getInt("y"), result.getInt("z"), result.getString("world")));
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return openRequests;
    }
    
    /**
     * Gets number of held requests from the database.
     * @return Integer amount of requests
     */
    public static int getHeldRequests(){
    	
    	if(!db.checkConnection()) return 0;
    	int heldRequests = 0;
    	
    	try {
    		ResultSet result = db.query("SELECT `id` FROM `reportrts_request` WHERE `status` = 2");
    		while(result.next()){
    			heldRequests++;
    		}
    		result.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return heldRequests;
    }
    
    private static void confirmTables(){
    	if(!db.checkTable("reportrts_request")){
    		String queryString = "CREATE TABLE reportrts_request (id integer primary key,"
    				+ " user_id integer no null,"
    				+ " mod_id integer,"
    				+ " mod_timestamp bigint,"
    				+ " mod_comment varchar(255),"
    				+ " tstamp bigint not null,"
    				+ " world varchar(255) not null," 
    				+ " x integer not null," 
    				+ " y integer not null,"
    				+ " z integer not null,"
    				+ " text varchar(255) not null,"
    				+ " status integer,"
    				+ " notified_of_completion integer)";
    		try {
    			db.query(queryString);
    			ReportRTS.getPlugin().getLogger().log(Level.INFO, "Successfully created the requests table.");
			} catch (Exception e) {
				ReportRTS.getPlugin().getLogger().log(Level.SEVERE, "Unable to create the requests table.");
				e.printStackTrace();
			}
    	}
    	if(!db.checkTable("reportrts_user")){
    		String queryString = "CREATE TABLE reportrts_user (id integer primary key,"
    				+ " name varchar(255) not null,"
    				+ " banned integer)";
    		try {
    			db.query(queryString);
    			ReportRTS.getPlugin().getLogger().log(Level.INFO, "Successfully created the users table.");
			} catch (Exception e) {
				ReportRTS.getPlugin().getLogger().log(Level.SEVERE, "Unable to create the users table.");
				e.printStackTrace();
			}
    	}
    }
    
    public static int getUserIdCreateIfNotExists(String player){
    	if(!db.checkConnection()) return 0;
    	int userId = 0;
    	try {
    		PreparedStatement ps = db.getConnection().prepareStatement("SELECT `id` FROM `reportrts_user` WHERE `name` = ?");
    		ps.setString(1, player);
    		ResultSet rs = ps.executeQuery();
    		userId = !rs.isBeforeFirst() ? createUser(player) : rs.getInt("id");
    		ps.close();
    		rs.close();	
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return userId;
    }
     
    private static int createUser(String player){
    	if(!db.checkConnection()) return 0;
    	int userId = 0;
    	try {
			PreparedStatement ps = db.getConnection().prepareStatement("INSERT INTO `reportrts_user` (`name`, `banned`) VALUES (?, '0')");
			ps.setString(1, player);
			if(ps.executeUpdate() < 1) return 0;
			ps.close();
			ps = db.getConnection().prepareStatement("SELECT `id` FROM `reportrts_user` WHERE `name` = ?");
			ps.setString(1, player);
			ResultSet rs = ps.executeQuery();
		
			if(!rs.isBeforeFirst()) return 0;
			userId = rs.getInt(1);
			ps.close();
			rs.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return userId;
    }
    /**
     * Files a request, inserting it into the database.
     * @param player String Name of player
     * @param world String Name of world
     * @param location getLocation() object
     * @param message String help request message
     * @return True if successful.
     */
    public static boolean fileRequest(String player, String world, Location location, String message, int userId){
    	if(!db.checkConnection() || userId == 0) return false;
    	long tstamp = System.currentTimeMillis()/1000;
    	try {
    		ResultSet rs = db.query("SELECT `banned` FROM reportrts_user WHERE `id` = '" + userId + "'");
    		if(rs.getInt("banned") == 1){
    			rs.close();
    			return false;
    		}
    		rs.close();
			PreparedStatement ps = db.getConnection().prepareStatement("INSERT INTO `reportrts_request` (`user_id`, `tstamp`, `world`, `x`, `y`, `z`," +
					" `text`, `status`, `notified_of_completion`) VALUES" +
					" (?, ?, ?, ?, ?, ?, ?, '0', '0')");
			ps.setInt(1, userId);
			ps.setLong(2, tstamp);
			ps.setString(3, world);
			ps.setInt(4, location.getBlockX());
			ps.setInt(5, location.getBlockY());
			ps.setInt(6, location.getBlockZ());
			ps.setString(7, message);
			if(ps.executeUpdate() < 1) return false;
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
    	return true;
    }
    
    public static int getlatestTicketIdByUser(String player, int userId){
    	if(!db.checkConnection()) return 0;
    	int ticketId = 0;
    	ResultSet result = db.query("SELECT `id` FROM `reportrts_request` WHERE `user_id` = '" + userId + "' ORDER BY `tstamp` DESC LIMIT 1");
		try {
			ticketId = result.getInt("id");
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return ticketId;
    }
    
    /**
     * Sets a ticket to the status specified
     * @param id
     * @param name
     * @return true if successful
     */
    public static boolean setRequestStatus(int id, String name, int status){
    	if(!db.checkConnection()) return false;
    	ResultSet rs = db.query("SELECT `status` FROM reportrts_request WHERE `id` = " + id);
    	try {
			if(rs.getInt("status") == status) {
				rs.close();
				return false;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
    	int modId = getUserIdCreateIfNotExists(name);
    	try {
    		PreparedStatement ps = db.getConnection().prepareStatement("UPDATE reportrts_request SET `status` = ?, mod_id = ?, mod_timestamp = ? WHERE `id` = ?");
    		ps.setInt(1, status);
    		ps.setInt(2, modId);
    		ps.setLong(3, System.currentTimeMillis() / 1000);
    		ps.setInt(4, id);
    		if(ps.executeUpdate() < 1) {
    			ps.close();
    			return false;
    		}
    		ps.close();
        	//db.query("UPDATE reportrts_request SET `status` = '" + status + "', mod_id = '" + modId + "', mod_timestamp = '" + System.currentTimeMillis() / 1000 + "' WHERE `id` = " + id).close();

        	ResultSet result = db.query("SELECT `status` FROM `reportrts_request` WHERE `id` = " + id);
			if(result.getInt("status") != status){
				result.close();
				return false;
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
    	return true;
    }
    
    public static boolean setUserStatus(String player, int status){
    	int userId = getUserIdCreateIfNotExists(player);

    	ResultSet result = db.query("SELECT `banned` FROM reportrts_user WHERE `id` = '" + userId + "'");
    	
    	try {
			int banned = result.getInt("banned");
			result.close();
			if(banned == status) return false;
			PreparedStatement ps = db.getConnection().prepareStatement("UPDATE reportrts_user SET `banned` = '" + status + "' WHERE `id` = ?");
			ps.setInt(1, userId);
			if(ps.executeUpdate() < 1) return false;
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
    	return true;
    }
    
    public static boolean resetDB(){
    	db.query("DELETE FROM reportrts_request");
		db.query("DELETE FROM reportrts_user");
    	return true;
    }
}