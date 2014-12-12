package com.nyancraft.reportrts.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.nyancraft.reportrts.MigrationTask;
import com.nyancraft.reportrts.ReportRTS;

import com.nyancraft.reportrts.persistence.query.Query;
import com.nyancraft.reportrts.persistence.database.MySQL;
import org.bukkit.scheduler.BukkitTask;

public class MySQLDB extends SQLDB {
    private MySQL db;
    private ArrayList<String> columns = new ArrayList<>();

    public Query queryGen;

    public boolean connect(){
        ReportRTS.getPlugin().getLogger().info("Connecting to MySQL.");
        db = new MySQL(ReportRTS.getPlugin().getLogger(), ReportRTS.getPlugin().storageDatabase, ReportRTS.getPlugin().storageUsername,
                ReportRTS.getPlugin().storagePassword, ReportRTS.getPlugin().storageHostname, ReportRTS.getPlugin().storagePort, ReportRTS.getPlugin().storagePrefix);
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

    public ResultSet query(String query){
        try {
            return db.query(query);
        } catch (SQLException e) {
            return null;
        }
    }

    private boolean checkTables() throws Exception{
        if(!this.db.isTable(ReportRTS.getPlugin().storagePrefix + "reportrts_request")){
            if(!db.createTable(queryGen.createRequestTable())) return false;
            ReportRTS.getPlugin().getLogger().info("Created " + ReportRTS.getPlugin().storagePrefix + "reportrts_request table.");
        }
        if(!this.db.isTable(ReportRTS.getPlugin().storagePrefix + "reportrts_user")){
            if(!db.createTable(queryGen.createUserTable())) return false;
            ReportRTS.getPlugin().getLogger().info("Created " + ReportRTS.getPlugin().storagePrefix + "reportrts_user table.");
        }
        return true;
    }

    private boolean checkColumns(){
        try{
            ResultSet rs = db.query(queryGen.getColumns(ReportRTS.getPlugin().storagePrefix + "reportrts_request"));
            columns.clear();
            while(rs.next()){
                columns.add(rs.getString("Field"));
            }
            rs.close();
            if(!columns.contains("bc_server")){
                db.query("ALTER TABLE `" + ReportRTS.getPlugin().storagePrefix + "reportrts_request`" +
                        " ADD COLUMN `bc_server` VARCHAR(255) NOT NULL DEFAULT '' COLLATE 'utf8_general_ci' AFTER `world`");
                ReportRTS.getPlugin().getLogger().info("Successfully upgraded the database structure to v1.2.0");
            }
            rs = db.query(queryGen.getColumns(ReportRTS.getPlugin().storagePrefix + "reportrts_user"));
            columns.clear();
            while(rs.next()){
                columns.add(rs.getString("Field"));
            }
            rs.close();
            if(!columns.contains("uuid")){
                db.query("ALTER TABLE `" + ReportRTS.getPlugin().storagePrefix + "reportrts_user`" +
                    " ADD COLUMN `uuid` CHAR(36) NULL DEFAULT NULL AFTER `name`");
                ReportRTS.getPlugin().getLogger().info("Successfully upgraded the user database structure to accommodate for the UUID update.");

                /** UUID Async data migration. Get rid of this after a while when it is no longer needed. **/
                if(db.checkConnection()){
                    ReportRTS.getPlugin().getLogger().info("Starting UUID data migration.");
                    ReportRTS.getPlugin().getServer().getScheduler().runTaskAsynchronously(ReportRTS.getPlugin(), new MigrationTask(ReportRTS.getPlugin()));
                }
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
            db.query("TRUNCATE TABLE " + ReportRTS.getPlugin().storagePrefix + "reportrts_request");
            db.query("TRUNCATE TABLE " + ReportRTS.getPlugin().storagePrefix + "reportrts_user");
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

    @Override
    public void refresh(){
        this.query("SELECT 1");
    }
}
