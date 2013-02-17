package com.nyancraft.reportrts.persistence;

import java.sql.Connection;

import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.persistence.query.*;

public class DatabaseManager {
    private static Database database;
    private static Query queryGen;

    public static boolean load(){
        String type = ReportRTS.getPlugin().storageType;
        if(type.equalsIgnoreCase("mysql")){
            if(!loadMySQL()) loadSQLite();
        }
        if(type.equalsIgnoreCase("sqlite")) loadSQLite();
        /*if(type.equalsIgnoreCase("h2")){
            if(!loadH2()){
                File h2File = new File("lib/h2-1.3.164.jar");
                if(!h2File.exists()){
                    ReportRTS.getPlugin().getLogger().warning("-------------------------------");
                    ReportRTS.getPlugin().getLogger().warning("H2 library not found!");
                    ReportRTS.getPlugin().getLogger().warning("Either change the storage type");
                    ReportRTS.getPlugin().getLogger().warning("or install the required H2 library to your lib folder.");
                    ReportRTS.getPlugin().getLogger().warning("See http://dev.bukkit.org/server-mods/reportrts/pages/h2/ for more info.");
                    ReportRTS.getPlugin().getLogger().warning("-------------------------------");
                    ReportRTS.getPlugin().getServer().getPluginManager().disablePlugin(ReportRTS.getPlugin());
                    return false;
                }
            }
        } */
        database.setLoaded();
        return true;
    }

    private static boolean loadMySQL(){
        database = new MySQLDB();
        queryGen = new MySQLQuery();
        return database.connect();
    }

    private static boolean loadSQLite(){
        database = new SQLiteDB();
        queryGen = new SQLiteQuery();
        return database.connect();
    }

    public static Database getDatabase(){
        return database;
    }

    public static Connection getConnection(){
        return database.connection();
    }

    public static Query getQueryGen(){
        return queryGen;
    }
}
