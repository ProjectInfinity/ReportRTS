package com.nyancraft.reportrts.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.persistence.query.Query;

import lib.PatPeter.RE_SQLibrary.MySQL;

public class MySQLDB extends SQLDB {
    private MySQL db;
    private ArrayList<String> columns = new ArrayList<String>();

    public Query queryGen;

    public ResultSet query(String query){
        try{
            return db.query(query);
        }catch(Exception e){
            return null;
        }
    }

    public boolean connect(){
        ReportRTS.getPlugin().getLogger().info("Connecting to MySQL.");
        db = new MySQL(ReportRTS.getPlugin().getLogger(), "[MySQL]", ReportRTS.getPlugin().storageHostname,
                ReportRTS.getPlugin().storagePort, ReportRTS.getPlugin().storageDatabase,
                ReportRTS.getPlugin().storageUsername, ReportRTS.getPlugin().storagePassword);
        queryGen = DatabaseManager.getQueryGen();
        try{
            db.open();
            if(!db.checkConnection()) return false;
        }catch(Exception e){
            ReportRTS.getPlugin().getLogger().warning("Failed to connect to the MySQL database.");
            return false;
        }

        try{
            checkTables();
            checkColumns();
        }catch(Exception e){
            ReportRTS.getPlugin().getLogger().warning("Could not access MySQL tables.");
            return false;
        }
        ReportRTS.getPlugin().getLogger().info("Successfully connected and checked tables, will use MySQL.");
        return true;
    }

    private boolean checkTables() throws Exception{
        if(!this.db.isTable("reportrts_request")){
            if(!db.createTable(queryGen.createRequestTable())) return false;
            ReportRTS.getPlugin().getLogger().info("Created reportrts_request table.");
        }
        if(!this.db.isTable("reportrts_user")){
            if(!db.createTable(queryGen.createUserTable())) return false;
            ReportRTS.getPlugin().getLogger().info("Created reportrts_user table.");
        }
        return true;
    }

    private boolean checkColumns(){
        try{
            ResultSet rs = db.query(queryGen.getColumns("reportrts_request"));
            columns.clear();
            while(rs.next()){
                columns.add(rs.getString("Field"));
            }
            rs.close();
            if(!columns.contains("yaw") || !columns.contains("pitch")){
                db.query("ALTER TABLE `reportrts_request`" +
                        " ADD COLUMN `yaw` smallint(6) NOT NULL DEFAULT 0 AFTER `z`," +
                        " ADD COLUMN `pitch` smallint(6) NOT NULL DEFAULT 0 AFTER `yaw`");
                ReportRTS.getPlugin().getLogger().info("Successfully upgraded the database structure to v0.4.0");
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
            db.query("TRUNCATE TABLE reportrts_request");
            db.query("TRUNCATE TABLE reportrts_user");
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
