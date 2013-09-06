package com.nyancraft.reportrts.persistence.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public abstract class Database {
    protected Logger log;

    protected final String prefix;

    protected Connection connection;

    protected boolean connected;

    public Database(Logger log, String prefix){
        if(log == null) throw new DatabaseException("Log can't be null!");
        this.log = log;
        this.prefix = prefix;
        this.connected = false;
    }

    protected abstract boolean initialize();

    public abstract boolean open();

    public abstract boolean truncate(String table);

    public abstract boolean isTable(String table);

    public abstract ResultSet query(String query) throws SQLException;

    public final boolean close() {
        this.connected = false;
        if(connection != null){
            try{
                connection.close();
                return true;
            }catch(SQLException e){
                e.printStackTrace();
                return false;
            }
        }else{
            this.log.warning("Connection is null! Cannot close it.");
            return false;
        }
    }

    public final boolean checkConnection(){
        if(connection != null)
            return true;
        return false;
    }

    public final Connection getConnection(){
        return this.connection;
    }

    public abstract boolean createTable(String query);

}
