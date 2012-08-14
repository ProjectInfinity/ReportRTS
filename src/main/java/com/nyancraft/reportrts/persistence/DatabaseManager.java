package com.nyancraft.reportrts.persistence;

import java.sql.Connection;

import com.nyancraft.reportrts.ReportRTS;

public class DatabaseManager {
    private static Database database;

    public static boolean load(){
        if(ReportRTS.getPlugin().useMySQL){
            if(!loadMySQL()){
                ReportRTS.getPlugin().useMySQL = false;
                loadSQLite();
            }
        }else{
            loadSQLite();
        }
        database.setLoaded();
        return true;
    }

    private static boolean loadMySQL(){
        database = new MySQLDB();
        return database.connect();
    }

    private static boolean loadSQLite(){
        database = new SQLiteDB();
        return database.connect();
    }

    public static Database getDatabase(){
        return database;
    }

    public static Connection getConnection(){
        return database.connection();
    }
}
