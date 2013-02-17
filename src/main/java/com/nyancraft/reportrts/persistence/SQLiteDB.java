package com.nyancraft.reportrts.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.persistence.query.Query;
import lib.PatPeter.RE_SQLibrary.SQLite;

public class SQLiteDB extends SQLDB {
    private SQLite db;
    private ArrayList<String> columns = new ArrayList<String>();

    public Query queryGen;

    @Override
    public ResultSet query(String query){
        try{
            return db.query(query);
        }catch(Exception e){
            return null;
        }
    }

    public boolean connect(){
        ReportRTS.getPlugin().getLogger().info("Connecting to SQLite.");
        db = new SQLite(
                ReportRTS.getPlugin().getLogger(),
                "[SQLite]",
                ReportRTS.getPlugin().getDataFolder().getPath(),
                ReportRTS.getPlugin().getDescription().getName());
        queryGen = DatabaseManager.getQueryGen();
        try{
            db.open();
            if(!db.checkConnection()) return false;
        } catch(Exception e){
            ReportRTS.getPlugin().getLogger().warning("Failed to connect to the SQLite database.");
        }

        try{
            if(!checkTables()) return false;
        }catch(Exception e){
            ReportRTS.getPlugin().getLogger().warning("Could not access SQLite tables.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean checkTables() throws Exception{
        if(!db.isTable("reportrts_request")){
            if(!db.createTable(queryGen.createRequestTable())) return false;
            ReportRTS.getPlugin().getLogger().info("Created reportrts_request table.");
        }
        if(!db.isTable("reportrts_user")){
            if(!db.createTable(queryGen.createUserTable())) return false;
            ReportRTS.getPlugin().getLogger().info("Created reportrts_user table.");
        }
        return this.checkColumns();
    }

    private boolean checkColumns(){
        try{
            ResultSet rs = db.query(queryGen.getColumns("reportrts_request"));
            columns.clear();
            while(rs.next()){
                columns.add(rs.getString("name"));
            }
            rs.close();
            if(!columns.contains("yaw") || !columns.contains("pitch")){
                ReportRTS.getPlugin().getLogger().severe("Due to a bug, the database structure cannot be automatically upgraded on SQLite. Please run /reportrts upgrade or delete the old ReportRTS.db file!");
            }
            return true;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
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
        try {
            db.query("DELETE FROM reportrts_request");
            db.query("DELETE FROM reportrts_user");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean checkTable(String table){
        return db.isTable(table);
    }
}
