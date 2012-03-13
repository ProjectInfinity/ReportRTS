package com.nyancraft.reportrts.persistence;

import java.sql.Connection;
import java.sql.ResultSet;

import org.bukkit.Location;

public interface Database {

	public boolean connect();
	
	public void disconnect();
	
    public boolean isLoaded();

    public void setLoaded();
	
	public void populateRequestMap();
	
	public void deleteRequestsByTime(String table, int lessThanThis);
	
	public int getNumberHeldRequests();
	
	public int getUserId(String player);
	
	public boolean fileRequest(String player, String world, Location location, String message, int userId);
	
	public int getLatestTicketIdByUser(int userId);
	
	public ResultSet getHeldRequests(int from);
	
	public ResultSet getTicketById(int id);
	
	public ResultSet getHeldTicketById(int id);
	
	public ResultSet getLocationById(int id);
	
	public boolean setRequestStatus(int id, String player, int status);
	
	public boolean setUserStatus(String player, int status);
	
	public boolean resetDB();
	
	public Connection connection();
}
