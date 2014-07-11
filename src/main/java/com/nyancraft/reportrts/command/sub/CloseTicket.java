package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.HelpRequest;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.event.ReportCompleteEvent;
import com.nyancraft.reportrts.persistence.Database;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class CloseTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();
    private static Database dbManager = DatabaseManager.getDatabase();

    /**
     * Initial handling of the Close sub-command.
     * @param sender player that sent the command
     * @param args arguments
     * @return true if command handled correctly
     */
    public static boolean handleCommand(CommandSender sender, String[] args) {

        if(args.length < 2) return false;

        if(!RTSFunctions.isNumber(args[1])) {
            sender.sendMessage(Message.parse("generalInternalError", "Argument must be a number, provided: " + args[1]));
            return true;
        }
        int ticketId = Integer.parseInt(args[1]);

        if(!RTSPermissions.canCompleteRequests(sender)) {
            if(RTSPermissions.canCompleteOwnRequests(sender)) {

                if(!plugin.requestMap.containsKey(ticketId)){
                    sender.sendMessage(Message.parse("generalInternalError", "Ticket not found."));
                    return true;
                }
                Player player = (Player) sender;
                if(!plugin.requestMap.get(ticketId).getUUID().equals(player.getUniqueId())){
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

                return true;
            } else {
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.complete or reportrts.command.complete.self"));
                return true;
            }
        }

        String user = sender.getName();
        if(user == null){
            sender.sendMessage(Message.parse("generalInternalError", "sender.getName() returned NULL! Are you using plugins to modify names?"));
            return true;
        }

        args[0] = "";
        String comment = RTSFunctions.implode(args, " ");

        // TODO: Does this do anything?
        if(comment.length() <= args[1].length())
            comment = null;
        else
            comment = comment.substring(args[1].length()).trim();

        int online = 0;
        boolean isClaimedByOther = false;

        if(plugin.requestMap.containsKey(ticketId)) {
            online = (RTSFunctions.isUserOnline(plugin.requestMap.get(ticketId).getUUID())) ? 1 : 0;
            if(plugin.requestMap.get(ticketId).getStatus() == 1) {
                // Holy shit.
                isClaimedByOther = (!plugin.requestMap.get(ticketId).getModUUID().equals((sender instanceof Player ? ((Player) sender).getUniqueId() : dbManager.getUserUUID(dbManager.getUserId(sender.getName())))));
            }
        }

        if(isClaimedByOther && !sender.hasPermission("reportrts.override")) {
            sender.sendMessage(Message.parse("generalInternalError", "Request #" + ticketId + " is claimed by someone else."));
            return true;
        }

        long timestamp = System.currentTimeMillis() / 1000;
        if(!dbManager.setRequestStatus(ticketId, user, 3, comment, online, timestamp, true)) {
            sender.sendMessage(Message.parse("generalInternalError", "Unable to mark request #" + args[0] + " as complete"));
            return true;
        }

        HelpRequest data = null;
        if(plugin.requestMap.containsKey(ticketId)) {
            Player player = sender.getServer().getPlayer(plugin.requestMap.get(ticketId).getUUID());
            if(online == 0) plugin.notificationMap.put(ticketId, plugin.requestMap.get(ticketId).getUUID());
            if(player != null){
                player.sendMessage(Message.parse("completedUser", user));
                if(comment == null) comment = "";
                player.sendMessage(Message.parse("completedText", plugin.requestMap.get(ticketId).getMessage(), comment));
            }else{
                try{
                    BungeeCord.notifyUser(plugin.requestMap.get(ticketId).getUUID(), Message.parse("completedUser", user), ticketId);
                    if(comment == null) comment = "";
                    BungeeCord.notifyUser(plugin.requestMap.get(ticketId).getUUID(), Message.parse("completedText", plugin.requestMap.get(ticketId).getMessage(), comment), ticketId);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            data = plugin.requestMap.get(ticketId);
            plugin.requestMap.remove(ticketId);
        }

        try {
            BungeeCord.globalNotify(Message.parse("completedReq", ticketId, user), ticketId, NotificationType.COMPLETE);
        } catch(IOException e) {
            e.printStackTrace();
        }
        RTSFunctions.messageMods(Message.parse("completedReq", ticketId, user), false);
        if(data != null) {
            data.setModComment(comment);
            if (data.getModName() == null) {
                data.setModName(sender.getName());
            }
            plugin.getServer().getPluginManager().callEvent(new ReportCompleteEvent(data, sender));
        }
        return true;
    }
}