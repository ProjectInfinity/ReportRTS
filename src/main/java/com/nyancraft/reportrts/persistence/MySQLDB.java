package com.nyancraft.reportrts.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.nyancraft.reportrts.ReportRTS;

import lib.PatPeter.SQLibrary.MySQL;

public class MySQLDB extends SQLDB {
    private MySQL db;
    private ArrayList<String> columns = new ArrayList<String>();

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
            checkColumns();
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

    private boolean checkColumns(){
        ResultSet rs = db.query(QueryGen.getColumns("reportrts_request"));
        columns.clear();
        try{
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
        db.query("TRUNCATE TABLE reportrts_request");
        db.query("TRUNCATE TABLE reportrts_user");
        return true;
    }

    @Override
    public boolean checkTable(String table){
        return db.checkTable(table);
    }
}
