package com.nyancraft.reportrts.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.HelpRequest;

public abstract class SQLDB implements Database{

    private boolean loaded = false;

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded() {
        loaded = true;
    }

    public abstract ResultSet query(String query);

    private boolean iterateData(ResultSet data) {
        try {
                return data.next();
            } catch (SQLException e) {
                return false;
            }
    }

    private String getString(ResultSet data, String label) {
        try {
            return data.getString(label);
            } catch (SQLException e) {
                return null;
            }
    }

    private int getInt(ResultSet data, String label) {
        try {
            return data.getInt(label);
            } catch (SQLException e) {
                return 0;
            }
    }

    private int createUser(String player){
        if(!isLoaded()) return 0;
        int userId = 0;
        try {
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement((QueryGen.createUser()));
            ps.setString(1, player);
            if(ps.executeUpdate() < 1) return 0;
            ps.close();
            ps = DatabaseManager.getConnection().prepareStatement(QueryGen.getUserId());
            ps.setString(1, player);
            ResultSet rs = ps.executeQuery();

            if(!rs.isBeforeFirst()) return 0;
            if(ReportRTS.getPlugin().useMySQL) rs.next();
            userId = rs.getInt(1);
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }

    @Override
    public int getUserId(String player) {
        if(!isLoaded()) return 0;
        int userId = 0;
        try {
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(QueryGen.getUserId());
            ps.setString(1, player);
            ResultSet rs = ps.executeQuery();
            if(!rs.isBeforeFirst()){
                userId = createUser(player);
            }else{
                if(ReportRTS.getPlugin().useMySQL) rs.next();
                userId = rs.getInt("id");
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }

    @Override
    public void populateRequestMap() {
        try {
            ResultSet rs = query(QueryGen.getAllOpenAndClaimedRequests());
            while(rs.next()){
                ReportRTS.getPlugin().requestMap.put(rs.getInt(1), new HelpRequest(rs.getString("name"), rs.getInt(1), rs.getLong("tstamp"), rs.getString("text"), rs.getInt("status"), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getInt("yaw"), rs.getInt("pitch"), rs.getString("world")));
                ReportRTS.getPlugin().requestMap.get(rs.getInt(1)).setModTimestamp(rs.getInt("mod_timestamp"));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getNumberHeldRequests() {
        if(!isLoaded()) return 0;
        int heldRequests = 0;

        try {
            ResultSet result = query("SELECT `id` FROM `reportrts_request` WHERE `status` = 2");
            while(result.next()){
                heldRequests++;
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return heldRequests;
    }

    @Override
    public boolean fileRequest(String player, String world, Location location, String message, int userId) {
        if(!isLoaded() || userId == 0) return false;
        try{
            long tstamp = System.currentTimeMillis()/1000;
            ResultSet rs = query(QueryGen.getUserStatus(userId));
            if(ReportRTS.getPlugin().useMySQL){
                if(rs.isBeforeFirst()) rs.next();
            }
            if(rs.getInt("banned") == 1){
                rs.close();
                return false;
            }
            rs.close();
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(QueryGen.createRequest());
            ps.setInt(1, userId);
            ps.setLong(2, tstamp);
            ps.setString(3, world);
            ps.setInt(4, location.getBlockX());
            ps.setInt(5, location.getBlockY());
            ps.setInt(6, location.getBlockZ());
            ps.setFloat(7, location.getYaw());
            ps.setFloat(8, location.getPitch());
            ps.setString(9, message);
            if(ps.executeUpdate() < 1) return false;
            ps.close();
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public int getLatestTicketIdByUser(int userId){
        if(!isLoaded() || userId == 0) return 0;
        int ticketId = 0;
        try {
            ResultSet rs = query(QueryGen.getLatestTicketIdByUser(userId));
            if(ReportRTS.getPlugin().useMySQL){
                if(rs.isBeforeFirst()) rs.next();
            }
            ticketId = rs.getInt("id");
            rs.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return ticketId;
    }

    @Override
    public boolean setRequestStatus(int id, String player, int status, String comment, int notified){
        if(!isLoaded()) return false;
        ResultSet rs = query(QueryGen.getTicketStatusById(id));
        try{
            if(!rs.isBeforeFirst()) return false;
            if(ReportRTS.getPlugin().useMySQL) rs.first();
            if(rs.getInt("status") == status || (status == 2 && rs.getInt("status") == 3)){
                rs.close();
                return false;
            }
            rs.close();
            int modId = getUserId(player);

            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement("UPDATE reportrts_request SET `status` = ?, mod_id = ?, mod_timestamp = ?, mod_comment = ?, notified_of_completion = ? WHERE `id` = ?");
            ps.setInt(1, status);
            ps.setInt(2, modId);
            ps.setLong(3, System.currentTimeMillis() / 1000);
            ps.setString(4, comment);
            ps.setInt(5, notified);
            ps.setInt(6, id);
            if(ps.executeUpdate() < 1) {
                ps.close();
                return false;
            }
            ps.close();
            rs = query("SELECT `status` FROM `reportrts_request` WHERE `id` = " + id);
            if(ReportRTS.getPlugin().useMySQL){
                if(rs.isBeforeFirst()) rs.next();
            }
            if(rs.getInt("status") != status){
                rs.close();
                return false;
            }
            rs.close();
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean setUserStatus(String player, int status) {
        int userId = getUserId(player);
        ResultSet rs = query(QueryGen.getUserStatus(userId));
        try {
            if(ReportRTS.getPlugin().useMySQL){
                if(rs.isBeforeFirst()) rs.next();
            }
            int banned = rs.getInt("banned");
            rs.close();
            if(banned == status) return false;
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(QueryGen.setUserStatus(status));
            ps.setInt(1, userId);
            if(ps.executeUpdate() < 1) return false;
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    @Override
    public int countRequests(int status){
        if(!isLoaded()) return 0;
        ResultSet rs = query(QueryGen.countRequests(status));
        int total = 0;
        try{
            if(!rs.isBeforeFirst()) return 0;
            if(ReportRTS.getPlugin().useMySQL) rs.first();
            total = rs.getInt(1);
            rs.close();
        }catch(SQLException e){
            e.printStackTrace();
            return 0;
        }
        return total;
    }
    @Override
    public ResultSet getHeldRequests(int from, int limit){
        return query(QueryGen.getHeldRequests(from, limit));
    }

    @Override
    public ResultSet getClosedRequests(int from, int limit){
        return query(QueryGen.getClosedRequests(from, limit));
    }

    @Override
    public ResultSet getTicketById(int id){
        return query(QueryGen.getTicketById(id));
    }

    @Override
    public ResultSet getHeldTicketById(int ticketId){
        return query(QueryGen.getHeldByTicketId(ticketId));
    }

    @Override
    public ResultSet getLocationById(int id){
        return query(QueryGen.getLocationById(id));
    }

    @Override
    public ResultSet getUnnotifiedUsers(){
        return query(QueryGen.getUnnotifiedUsers());
    }

    @Override
    public void deleteRequestsByTime(String table, int lessThanThis){
        try {
            query(QueryGen.deleteRequestOlderThan(table, lessThanThis)).close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean setNotificationStatus(int id, int status){
        query(QueryGen.setNotificationStatus(id, status));
        return true;
    }

    @Override
    public boolean insertRequest(int modId, String world, int x, int y, int z, String message, int userId, int tstamp) {
        if(!isLoaded() || userId == 0) return false;
        try{
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(QueryGen.createRequest());
            ps.setInt(1, userId);
            ps.setLong(2, tstamp);
            ps.setString(3, world);
            ps.setInt(4, x);
            ps.setInt(5, y);
            ps.setInt(6, z);
            ps.setString(7, message);
            if(ps.executeUpdate() < 1) return false;
            ps.close();
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean insertUser(int userId, String name, int banned){
        if(!isLoaded() || userId == 0) return false;
        try{
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(QueryGen.createExactUser());
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setInt(3, banned);
            if(ps.executeUpdate() < 1) return false;
            ps.close();
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public ResultSet getAllFromTable(String table){
        return query(QueryGen.getAllFromTable(table));
    }

    @Override
    public ResultSet getHandledBy(String player){
        try{
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(QueryGen.getHandledBy());
            ps.setInt(1, getUserId(player));
            return ps.executeQuery();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteEntryById(String table, int id){
        this.query(QueryGen.deleteEntryById(table, id));
    }

    @Override
    public String getUserName(int userId){
        if(!isLoaded() || userId == 0) return null;
        String username = null;
        try{
            ResultSet rs = this.query(QueryGen.getUserName(userId));
            username = rs.getString("name");
            rs.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return username;
    }
    /*
    @Override
    public boolean addRole(){

    }

    @Override
    public boolean removeRole(){

    } */
}
