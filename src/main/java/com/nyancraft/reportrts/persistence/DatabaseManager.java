package com.nyancraft.reportrts.persistence;

import java.sql.Connection;

import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.persistence.query.MySQLQuery;
import com.nyancraft.reportrts.persistence.query.Query;

public class DatabaseManager {
    private static Database database;
    private static Query queryGen;

    public static boolean load(){
        String type = ReportRTS.getPlugin().storageType;
        if(type.equalsIgnoreCase("mysql")){
            if(loadMySQL()){
                database.setLoaded();
                return true;
            }
        }
        return false;
    }

    private static boolean loadMySQL(){
        database = new MySQLDB();
        queryGen = new MySQLQuery();
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
