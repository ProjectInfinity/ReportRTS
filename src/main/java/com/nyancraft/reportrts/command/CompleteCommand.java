package com.nyancraft.reportrts.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.HelpRequest;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.event.ReportCompleteEvent;
import com.nyancraft.reportrts.persistence.Database;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;
import com.nyancraft.reportrts.util.BungeeCord;

import java.io.IOException;

public class CompleteCommand implements CommandExecutor {

    private ReportRTS plugin;
    private Database dbManager;

    public CompleteCommand(ReportRTS plugin) {
        this.plugin = plugin;
        this.dbManager = DatabaseManager.getDatabase();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0) return false;
        if(!RTSPermissions.canCompleteRequests(sender)){
            if(RTSPermissions.canCompleteOwnRequests(sender)){
                if(!RTSFunctions.isParsableToInt(args[0])) return false;
                double start = 0;
                if(plugin.debugMode) start = System.nanoTime();
                int ticketId = Integer.parseInt(args[0]);
                if(!plugin.requestMap.containsKey(ticketId)){
                    sender.sendMessage(Message.parse("generalInternalError", "That request was not found."));
                    return true;
                }
                if(!plugin.requestMap.get(ticketId).getName().equalsIgnoreCase(sender.getName())){
                    sender.sendMessage(Message.parse("generalInternalError", "You are not the owner of that ticket."));
                    return true;
                }
                dbManager.deleteEntryById(plugin.storagePrefix + "reportrts_request", ticketId);
                plugin.requestMap.remove(ticketId);
                try{
                    BungeeCord.globalNotify(Message.parse("completedReq", ticketId, "Cancellation System"), ticketId, NotificationType.DELETE);
                }catch(IOException e){
                    e.printStackTrace();
                }
                RTSFunctions.messageMods(Message.parse("completedReq", ticketId,"Cancellation System"), false);
                sender.sendMessage(Message.parse("completedUser", "Cancellation System"));

                if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
                return true;
            }
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.complete or reportrts.command.complete.self"));
            return true;
        }
        if(!RTSFunctions.isParsableToInt(args[0])) return false;
        long start = 0;
        if(plugin.debugMode) start = System.nanoTime();
        String user = sender.getName();
        if(user == null){
            sender.sendMessage(Message.parse("generalInternalError", "sender.getName() returned NULL! Are you using plugins to modify names?"));
            return true;
        }
        int ticketId = Integer.parseInt(args[0]);
        String comment = RTSFunctions.implode(args, " ");

        if(comment.length() <= args[0].length()){
            comment = null;
        }else{
            comment = comment.substring(args[0].length()).trim();
        }

        int online = 0;
        boolean isClaimedByOther = false;

        if(plugin.requestMap.containsKey(ticketId)){
            online = (RTSFunctions.isUserOnline(plugin.requestMap.get(ticketId).getName())) ? 1 : 0;
            if(plugin.requestMap.get(ticketId).getStatus() == 1){
                isClaimedByOther = (!plugin.requestMap.get(ticketId).getModName().equalsIgnoreCase(user));
            }
        }

        if(isClaimedByOther && !sender.hasPermission("reportrts.override")){
            sender.sendMessage(Message.parse("generalInternalError", "Request #" + args[0] + " is claimed by someone else."));
            return true;
        }

        long timestamp = System.currentTimeMillis() / 1000;
        if(!dbManager.setRequestStatus(ticketId, user, 3, comment, online, timestamp)) {
            sender.sendMessage(Message.parse("generalInternalError", "Unable to mark request #" + args[0] + " as complete"));
            return true;
        }
        HelpRequest data = null;
        if(plugin.requestMap.containsKey(ticketId)) {
            Player player = sender.getServer().getPlayer(plugin.requestMap.get(ticketId).getName());
            if(online == 0) plugin.notificationMap.put(ticketId, plugin.requestMap.get(ticketId).getName());
            if(player != null){
                player.sendMessage(Message.parse("completedUser", user));
                if(comment == null) comment = "";
                player.sendMessage(Message.parse("completedText", plugin.requestMap.get(ticketId).getMessage(), comment));
            }else{
                try{
                    BungeeCord.notifyUser(plugin.requestMap.get(ticketId).getName(), Message.parse("completedUser", user), ticketId);
                    if(comment == null) comment = "";
                    BungeeCord.notifyUser(plugin.requestMap.get(ticketId).getName(), Message.parse("completedText",plugin.requestMap.get(ticketId).getMessage(), comment), ticketId);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            data = plugin.requestMap.get(ticketId);
            plugin.requestMap.remove(ticketId);
        }

        try{
            BungeeCord.globalNotify(Message.parse("completedReq", args[0], user), ticketId, NotificationType.COMPLETE);
        }catch(IOException e){
            e.printStackTrace();
        }
        RTSFunctions.messageMods(Message.parse("completedReq", args[0], user), false);
        if(data != null){
            data.setModComment(comment);
            if (data.getModName() == null) {
                data.setModName(sender.getName());
            }
            plugin.getServer().getPluginManager().callEvent(new ReportCompleteEvent(data, sender));
        }
        if(plugin.debugMode) Message.debug(user, this.getClass().getSimpleName(), start, cmd.getName(), args);
        return true;
    }
}
