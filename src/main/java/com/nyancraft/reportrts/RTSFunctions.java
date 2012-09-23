package com.nyancraft.reportrts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.data.HelpRequest;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.persistence.QueryGen;

public class RTSFunctions {

    private static ArrayList<String> columns = new ArrayList<String>();

    /**
     * Combines array and returns an imploded string.
     * @param args String[] array.
     * @return String, imploded array.
     */
    public static String combineString(String[] args){
        List<String> temp_list = new LinkedList<String>();
        temp_list.addAll(Arrays.asList(args));
        while (temp_list.contains("")) {
            temp_list.remove("");
        }
        args = temp_list.toArray(new String[0]);

        return implode(args, " ");
    }
    /**
     * Join a String[] into a single string with a joiner
     */
    public static String implode( String[] array, String glue ) {

    String out = "";

    if( array.length == 0 ) {
        return out;
    }

    for( String part : array ) {
        out = out + part + glue;
    }
    out = out.substring(0, out.length() - glue.length() );

    return out;
    }

    public static String cleanUpSign(String[] lines){

        String out = "";
        for(String part : lines){
            if(part.length() > 0) out = out + part.trim() + " ";
        }
        return out;
    }
    /***
     * Messages all online moderators on the server
     * @param message - message to be displayed
     */
    public static void messageMods(String message){
        for(String name : ReportRTS.getPlugin().moderatorMap){
            Player player = ReportRTS.getPlugin().getServer().getPlayer(name);
            if(player == null) return;
            player.sendMessage(message);
        }
    }

    /**
     * Returns true if the person is online.
     * @param username - String name of player
     * @param server - server object
     * @return boolean
     */
    public static boolean isUserOnline(String username, Server server) {
        return server.getOfflinePlayer(username).isOnline();
    }

    /***
     * Attempts to parse string as integer
     * @param i - String to be parsed
     * @return true if possible to parse
     */
    public static boolean isParsableToInt(String i){
        try{
            Integer.parseInt(i);
            return true;
        } catch(NumberFormatException nfe){
            return false;
        }
    }

    /**
     * Populates the requestMap with data regarding held requests.
     */
    public static void populateHeldRequestsWithData(){
        for(Map.Entry<Integer, HelpRequest> entry : ReportRTS.getPlugin().requestMap.entrySet()){
            if(entry.getValue().getStatus() == 1){
                int ticketId = entry.getValue().getId();
                ResultSet rs = DatabaseManager.getDatabase().getHeldTicketById(ticketId);
                try {
                    if(ReportRTS.getPlugin().useMySQL){
                        if(rs.isBeforeFirst()) rs.next();
                    }
                    entry.getValue().setModName(rs.getString("name"));
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Populates the notificationMap with data.
     */
    public static void populateNotificationMapWithData(){
        try{
            ResultSet rs = DatabaseManager.getDatabase().getUnnotifiedUsers();
            if(!rs.isBeforeFirst()) return;
            if(ReportRTS.getPlugin().useMySQL){
                rs.first();
            }
            while(rs.next()){
                ReportRTS.getPlugin().notificationMap.put(rs.getInt(1), rs.getString("name"));
            }
            rs.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * Get number of open request by the specified user.
     * @param sender
     * @return
     */
    public static int getOpenRequestsByUser(CommandSender sender){
        int openRequestsByUser = 0;
        for(Map.Entry<Integer, HelpRequest> entry : ReportRTS.getPlugin().requestMap.entrySet()){
            if(entry.getValue().getName().equals(sender.getName())) openRequestsByUser++;
        }
        return openRequestsByUser;
    }

    public static long checkTimeBetweenRequests(CommandSender sender){
        for(Map.Entry<Integer, HelpRequest> entry : ReportRTS.getPlugin().requestMap.entrySet()){
            if(entry.getValue().getName().equals(sender.getName())){
                if(entry.getValue().getTimestamp() > ((System.currentTimeMillis() / 1000) - ReportRTS.getPlugin().requestDelay)) return entry.getValue().getTimestamp() - (System.currentTimeMillis() / 1000 - ReportRTS.getPlugin().requestDelay);
            }
        }
        return 0;
    }

    public static String getTimeSpent(double start){
        DecimalFormat decimal = new DecimalFormat("##.###");
        return decimal.format((System.nanoTime() - start) / 1000000);
    }

    public static String shortenMessage(String message){
        if (message.length() >= 20) {
            message = message.substring(0, 20) + "...";
        }
        return message;
    }

    public static boolean checkColumns(){
        if(ReportRTS.getPlugin().useMySQL) return true;
        columns.clear();
        try{
            ResultSet rs = DatabaseManager.getConnection().createStatement().executeQuery(QueryGen.getColumns("reportrts_request"));
            while(rs.next()){
                columns.add(rs.getString("name"));
            }
            rs.close();
            if(!columns.contains("yaw") || !columns.contains("pitch")){
                ReportRTS.getPlugin().getLogger().info("Noticed you don't have the database structure required. I'll try to fix that!");
                return false;
            }
            return true;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }
}
