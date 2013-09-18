package com.nyancraft.reportrts.persistence.database;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class MySQL extends Database {

    private String hostname = "localhost";
    private String username = "minecraft";
    private String password = "";
    private String database = "minecraft";
    private String prefix = "";
    private int port = 3306;

    public MySQL(Logger log, String database, String username, String password, String hostname, int port, String prefix){
        super(log, "MySQL");
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.database = database;
        this.prefix = prefix;
        this.port = port;
    }

    @Override
    protected boolean initialize(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            return true;
        }catch(ClassNotFoundException e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean open(){
        if(!initialize()) return false;
        try{
            this.connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?autoReconnect=true", username, password);
            this.connected = true;
            return true;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isTable(String table){
        Statement statement;
        try{
            statement = this.connection.createStatement();
            statement.executeQuery("SELECT * FROM " + table);
            statement.close(); // Is this needed?
            return true;
        }catch(SQLException e){
            return false;
        }
    }

    @Override
    public ResultSet query(String query) throws SQLException{
        try{
            Statement statement = this.connection.createStatement();
            if(statement.execute(query)){
                statement.executeQuery(query);
                return statement.getResultSet();
            }
            return null;
        }catch(SQLException e){
            return null;
        }
    }

    @Override
    public boolean createTable(String sql) {
        try {
            query(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean truncate(String table){
        Statement statement = null;
        try{
            if(!this.isTable(table)){
                log.warning("The table \"" + table + "\" does not exist!");
                return false;
            }
            statement = this.connection.createStatement();
            statement.executeQuery("TRUNCATE TABLE " + table);
            statement.close();
            return true;
        }catch(SQLException e){
            log.warning("Unable to truncate table \"" + table + "\"");
            return false;
        }
    }
}
