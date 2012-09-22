package com.nyancraft.reportrts.command;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.HelpRequest;
import com.nyancraft.reportrts.persistence.Database;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;
import com.nyancraft.reportrts.RTSFunctions;

public class CheckCommand implements CommandExecutor {

    private ReportRTS plugin;
    private Database dbManager;
    private String substring = null;

    public CheckCommand(ReportRTS plugin){
        this.plugin = plugin;
        this.dbManager = DatabaseManager.getDatabase();
    }
    private List<Map.Entry<Integer, HelpRequest>> requestList = new ArrayList<Map.Entry<Integer, HelpRequest>>();
    private SimpleDateFormat sdf  = new SimpleDateFormat("MMM.dd kk:mm z");
    private String date = null;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        double start = 0;
        if(plugin.debugMode) start = System.nanoTime();
        if(!RTSPermissions.canCheckAllRequests(sender)){
            if(!RTSPermissions.canCheckOwnRequests(sender)){
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.check or reportrts.command.check.self"));
                return true;
            }
            if(args.length > 0) {
                sender.sendMessage(ChatColor.RED + "You may only use /check to check the status of your requests.");
                return true;
            }
            checkSelf(sender);
            if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
            return true;
        }
        if(args.length == 0){
            checkPage("1", sender);
            if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
        return true;
    }
        if(!RTSFunctions.isParsableToInt(args[0])){
            try{
                switch(SubCommands.valueOf(args[0].toUpperCase())){

                case P:
                case PAGE:
                    try{
                        checkPage(args[1], sender);
                    }catch(ArrayIndexOutOfBoundsException e){
                        checkPage("1", sender);
                    }
                    break;

                case H:
                case HELD:
                    try{
                        checkHeld(args[1], sender);
                    }catch(ArrayIndexOutOfBoundsException e){
                        checkHeld("1", sender);
                    }
                    break;

                case C:
                case CLOSED:
                    try{
                        checkClosed(args[1], sender);
                    }catch(ArrayIndexOutOfBoundsException e){
                        checkClosed("1", sender);
                    }
                    break;
                }
                if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
                return true;
            } catch(IllegalArgumentException e){
                return false;
            }
        }

        checkId(Integer.parseInt(args[0]), sender);
        if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
        return true;
    }
    private enum SubCommands{
        P,
        PAGE,
        H,
        HELD,
        C,
        CLOSED
    }

    private void checkPage(String page, CommandSender sender){
        // TODO: Fix page issue occurring when hideWhenOffline = true
        requestList.clear();
        requestList.addAll(plugin.requestMap.entrySet());

        int pageNumber = Integer.parseInt(page);
        sender.sendMessage(ChatColor.AQUA + "--------- " + requestList.size() + " Requests -" + ChatColor.YELLOW + " Open " + ChatColor.AQUA + "---------");
        if(plugin.requestMap.size() == 0) sender.sendMessage(Message.parse("checkNoRequests"));
        for(int i = (pageNumber * plugin.requestsPerPage) - plugin.requestsPerPage; i < (pageNumber * plugin.requestsPerPage) && i < requestList.size(); i++){
            HelpRequest currentRequest = requestList.get(i).getValue();
            if(plugin.hideWhenOffline && !RTSFunctions.isUserOnline(currentRequest.getName(), sender.getServer())) continue;
            substring = RTSFunctions.shortenMessage(currentRequest.getMessage());

            date = sdf.format(new java.util.Date(currentRequest.getTimestamp() * 1000));
            ChatColor online = (RTSFunctions.isUserOnline(currentRequest.getName(), sender.getServer())) ? ChatColor.GREEN : ChatColor.RED;
            substring = (currentRequest.getStatus() == 1) ? ChatColor.LIGHT_PURPLE + "Claimed by " + currentRequest.getModName() : ChatColor.GRAY + substring;
            sender.sendMessage(ChatColor.GOLD + "#" + currentRequest.getId() + " " + date + " by " + online + currentRequest.getName() + ChatColor.GOLD + " - " + substring);
        }
    }

    private void checkHeld(String page, CommandSender sender){
        int pageNumber = Integer.parseInt(page);
        int i = (pageNumber * plugin.requestsPerPage) - plugin.requestsPerPage;

        ResultSet rs = dbManager.getHeldRequests(i, plugin.requestsPerPage);

        try {
            int heldRequests = dbManager.getNumberHeldRequests();
            sender.sendMessage(ChatColor.AQUA + "--------- " + heldRequests + " Requests -" + ChatColor.YELLOW + " Held " + ChatColor.AQUA + "---------");
            if(heldRequests == 0) sender.sendMessage(Message.parse("holdNoRequests"));
            while(rs.next()){
                substring = RTSFunctions.shortenMessage(rs.getString("text"));
                date = sdf.format(new java.util.Date(rs.getLong("tstamp") * 1000));
                ChatColor online = (RTSFunctions.isUserOnline(rs.getString("name"), sender.getServer())) ? ChatColor.GREEN : ChatColor.RED;
                sender.sendMessage(ChatColor.GOLD + "#" + rs.getInt(1) + " " + date + " by " + online + rs.getString("name") + ChatColor.GOLD + " - " + ChatColor.GRAY + substring);
            }
            rs.close();
        } catch (SQLException e) {
            sender.sendMessage(Message.parse("generalInternalError", "Cannot check held requests, see console for errors."));
            e.printStackTrace();
            return;
        }
    }

    private void checkClosed(String page, CommandSender sender){
        int pageNumber = Integer.parseInt(page);
        int i = (pageNumber * plugin.requestsPerPage) - plugin.requestsPerPage;

        ResultSet rs = dbManager.getClosedRequests(i, plugin.requestsPerPage);
        try{
            int closedRequests = dbManager.countRequests(3);
            sender.sendMessage(ChatColor.AQUA + "--------- " + closedRequests + " Requests -" + ChatColor.YELLOW + " Closed " + ChatColor.AQUA + "--------- ");
            if(closedRequests == 0) sender.sendMessage(Message.parse("closedNoRequests"));
            while(rs.next()){
                substring = RTSFunctions.shortenMessage(rs.getString("text"));
                date = sdf.format(new java.util.Date(rs.getLong("tstamp") * 1000));
                ChatColor online = (RTSFunctions.isUserOnline(rs.getString("name"), sender.getServer())) ? ChatColor.GREEN : ChatColor.RED;
                sender.sendMessage(ChatColor.GOLD + "#" + rs.getInt(1) + " " + date + " by " + online + rs.getString("name") + ChatColor.GOLD + " - " + ChatColor.GRAY + substring);
            }
            rs.close();
        }catch(SQLException e){
            sender.sendMessage(Message.parse("generalInternalError", "Cannot check closed requests, see console for errors."));
            e.printStackTrace();
        }
    }

    private void checkId(int id, CommandSender sender){
        HelpRequest currentRequest = plugin.requestMap.get(id);

        if(currentRequest == null) {
            ResultSet rs = dbManager.getTicketById(id);

            ChatColor online;
            try {
                if(plugin.useMySQL){
                    if(rs.isBeforeFirst()) rs.first();
                }
                online = (RTSFunctions.isUserOnline(rs.getString("name"), sender.getServer())) ? ChatColor.GREEN : ChatColor.RED;
                date = sdf.format(new java.util.Date(rs.getLong("tstamp") * 1000));
                String status = null;
                ChatColor statusColor = null;

                if(rs.getInt("status") == 0){
                    status = "Open";
                    statusColor = ChatColor.YELLOW;
                }
                if(rs.getInt("status") == 1){
                    status = "Claimed";
                    statusColor = ChatColor.RED;
                }
                if(rs.getInt("status") == 2){
                    status = "On Hold";
                    statusColor = ChatColor.LIGHT_PURPLE;
                }
                if(rs.getInt("status") == 3){
                    status = "Closed";
                    statusColor = ChatColor.GREEN;
                }
                String text = rs.getString("text");
                String modComment = rs.getString("mod_comment");

                sender.sendMessage(ChatColor.AQUA + "--------- " + "Request #" + rs.getInt(1) + " - " + statusColor + status + ChatColor.AQUA + " ---------");
                sender.sendMessage(ChatColor.YELLOW + "Filed by" + online + " " + rs.getString("name") + ChatColor.YELLOW + " at " +  ChatColor.GREEN + date + ChatColor.YELLOW + " at " + ChatColor.GREEN + rs.getInt("x") + ", " + rs.getInt("y") + ", " + rs.getInt("z"));

                if(rs.getInt("status") == 3){
                    int modId = rs.getInt("mod_id");
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "Handled by " + dbManager.getUserName(modId) + ".");
                    int Millis = (rs.getInt("mod_timestamp") - rs.getInt("tstamp")) * 1000;
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + String.format("Time spent: %d hours, %d minutes, %d seconds",
                            Millis/(1000*60*60), (Millis%(1000*60*60))/(1000*60), ((Millis%(1000*60*60))%(1000*60))/1000));
                }

                if(modComment != null) sender.sendMessage(ChatColor.YELLOW + "Comment: " + ChatColor.DARK_GREEN + modComment);
                sender.sendMessage(ChatColor.GRAY + text);

                rs.close();
                return;
            } catch (SQLException e) {
                sender.sendMessage(Message.parse("generalRequestNotFound", id));
                return;
            }

        }

        ChatColor online = (RTSFunctions.isUserOnline(currentRequest.getName(), sender.getServer())) ? ChatColor.GREEN : ChatColor.RED;
        date = sdf.format(new java.util.Date(currentRequest.getTimestamp() * 1000));
        String status;
        if (currentRequest.getStatus() == 1){
            status = ChatColor.RED + "Claimed";
        }else{
            status = ChatColor.YELLOW + "Open";
        }

        sender.sendMessage(ChatColor.AQUA + "--------- " + " Request #" + currentRequest.getId() + " -" + ChatColor.YELLOW + " " + status + " " + ChatColor.AQUA + "---------");
        sender.sendMessage(ChatColor.YELLOW + "Filed by" + online + " " + currentRequest.getName() + ChatColor.YELLOW + " at " +  ChatColor.GREEN + date + ChatColor.YELLOW + " at " + ChatColor.GREEN + currentRequest.getX() + ", " + currentRequest.getY() + ", " + currentRequest.getZ());
        sender.sendMessage(ChatColor.GRAY + currentRequest.getMessage());

        if(currentRequest.getStatus() == 1){
            long Millis = (System.currentTimeMillis() - (currentRequest.getModTimestamp()) * 1000);
            sender.sendMessage(ChatColor.LIGHT_PURPLE + String.format("Claimed for: %d hours, %d minutes, %d seconds",
                    Millis/(1000*60*60), (Millis%(1000*60*60))/(1000*60), ((Millis%(1000*60*60))%(1000*60))/1000));
        }
    }

    private void checkSelf(CommandSender sender){
        int openRequests = 0;
        for(Map.Entry<Integer, HelpRequest> entry : plugin.requestMap.entrySet()){
            if(entry.getValue().getName().equals(sender.getName())) openRequests++;
        }
        int i = 0;
        sender.sendMessage(ChatColor.AQUA + "--------- " + ChatColor.YELLOW + " You have " + openRequests + " open requests " + ChatColor.AQUA + "----------");
        if(openRequests == 0) sender.sendMessage(ChatColor.GOLD + "You have no open requests at this time.");
        for(Map.Entry<Integer, HelpRequest> entry : plugin.requestMap.entrySet()){
            if(entry.getValue().getName().equals(sender.getName())){
                i++;
                if(i > 5) break;

                HelpRequest currentRequest = entry.getValue();
                substring = RTSFunctions.shortenMessage(currentRequest.getMessage());
                date = sdf.format(new java.util.Date(currentRequest.getTimestamp() * 1000));
                ChatColor online = (RTSFunctions.isUserOnline(currentRequest.getName(), sender.getServer())) ? ChatColor.GREEN : ChatColor.RED;
                substring = (currentRequest.getStatus() == 1) ? ChatColor.LIGHT_PURPLE + "Claimed by " + currentRequest.getModName() : ChatColor.GRAY + substring;
                sender.sendMessage(ChatColor.GOLD + "#" + currentRequest.getId() + " " + date + " by " + online + currentRequest.getName() + ChatColor.GOLD + " - " + substring);
            }
        }
    }
}
