package com.nyancraft.reportrts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.data.HelpRequest;
import com.nyancraft.reportrts.persistence.DatabaseManager;

public class RTSFunctions {

    private static ArrayList<String> columns = new ArrayList<>();

    /**
     * Combines array and returns an imploded string.
     * @param args String[] array.
     * @return String, imploded array.
     */
    public static String combineString(String[] args){
        List<String> temp_list = new LinkedList<>();
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
        if(part == null) continue;
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
     * @param playSound - boolean play sound or not.
     */
    public static void messageMods(String message, boolean playSound){
        for(UUID uuid : ReportRTS.getPlugin().moderatorMap){
            Player player = ReportRTS.getPlugin().getServer().getPlayer(uuid);
            if(player == null) return;
            player.sendMessage(message);
            if(ReportRTS.getPlugin().notificationSound && playSound) player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 0);
        }
    }

    /**
     * Synchronizes ticket data from the given ticket ID.
     * @param ticketId - ticket ID to be synchronized.
     */
    public static boolean syncTicket(int ticketId) {
        int updateResult = DatabaseManager.getDatabase().updateTicket(ticketId);
        return updateResult > 0;
    }

    /**
     * Synchronizes everything.
     */
    public static void sync(){
        ReportRTS.getPlugin().requestMap.clear();
        ReportRTS.getPlugin().notificationMap.clear();
        ReportRTS.getPlugin().moderatorMap.clear();
        DatabaseManager.getDatabase().populateRequestMap();
        RTSFunctions.populateHeldRequestsWithData();
        RTSFunctions.populateNotificationMapWithData();
        RTSFunctions.populateModeratorMapWithData();
    }

    /**
     * Returns true if the person is online.
     * @param uuid - UUID of player
     * @return boolean
     */
    public static boolean isUserOnline(UUID uuid){
        for(Player player : ReportRTS.getPlugin().getServer().getOnlinePlayers()){
            if(player.getUniqueId().equals(uuid)) return true;
        }
        return false;
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
                    if(ReportRTS.getPlugin().storageType.equalsIgnoreCase("mysql")){
                        if(rs.isBeforeFirst()) rs.next();
                    }
                    entry.getValue().setModUUID(UUID.fromString(rs.getString("uuid")));
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
            if(ReportRTS.getPlugin().storageType.equalsIgnoreCase("mysql")){
                rs.first();
            }
            while(rs.next()){
                ReportRTS.getPlugin().notificationMap.put(rs.getInt(1), UUID.fromString(rs.getString("uuid")));
            }
            rs.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * Get number of open request by the specified user.
     * @param uuid - UUID of user that sent the command.
     * @return amount of open requests by a specific user
     */
    public static int getOpenRequestsByUser(UUID uuid){
        int openRequestsByUser = 0;
        for(Map.Entry<Integer, HelpRequest> entry : ReportRTS.getPlugin().requestMap.entrySet()){
            if(entry.getValue().getUUID().equals(uuid)) openRequestsByUser++;
        }
        return openRequestsByUser;
    }

    public static long checkTimeBetweenRequests(UUID uuid){
        for(Map.Entry<Integer, HelpRequest> entry : ReportRTS.getPlugin().requestMap.entrySet()){
            if(entry.getValue().getUUID().equals(uuid)){
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

    public static void populateModeratorMapWithData(){
        for(Player player : ReportRTS.getPlugin().getServer().getOnlinePlayers()){
            if(RTSPermissions.isModerator(player)) ReportRTS.getPlugin().moderatorMap.add(player.getUniqueId());
        }
    }

    /**
     * Check if the provided String is a number or not.
     * @param number as a String
     * @return true if String is a number
     */
    public static boolean isNumber(String number){
        return (number.matches("-?\\d+") && !(Long.parseLong((number)) <= 0L) && (Long.parseLong((number)) < Integer.MAX_VALUE));
    }

    /**
     *
     * @param text that you want to separate.
     * @param when X amount of words have been displayed.
     * @return String with line separators.
     */
    public static String separateText(String text, int when) {
        int i = 0;
        StringBuilder message = new StringBuilder();
        for(String t : text.split(" ")) {
            if(i >= when) {
                i = 0;
                message.append(ReportRTS.getPlugin().lineSeparator);
            }
            message.append(t);
            i++;
        }
        return message.toString();
    }
}
