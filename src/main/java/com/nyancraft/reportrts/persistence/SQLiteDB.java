package com.nyancraft.reportrts.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.nyancraft.reportrts.ReportRTS;

import lib.PatPeter.SQLibrary.SQLite;

public class SQLiteDB extends SQLDB {
    private SQLite db;
    private ArrayList<String> columns = new ArrayList<String>();

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
                ReportRTS.getPlugin().getDescription().getName(),
                ReportRTS.getPlugin().getDataFolder().getPath());

        try{
            db.open();
            if(!db.checkConnection()) return false;
        } catch(Exception e){
            ReportRTS.getPlugin().getLogger().severe("Failed to connect to the SQLite database.");
        }

        try{
            if(!checkTables()) return false;
        }catch(Exception e){
            ReportRTS.getPlugin().getLogger().severe("Could not access SQLite tables.");
            e.printStackTrace();
            return false;
        }
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
        return this.checkColumns();
    }

    private boolean checkColumns(){
        ResultSet rs = db.query(QueryGen.getColumns("reportrts_request"));
        columns.clear();
        try{
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
        db.query("DELETE FROM reportrts_request");
        db.query("DELETE FROM reportrts_user");
        return true;
    }

    @Override
    public boolean checkTable(String table){
        return db.checkTable(table);
    }
}
