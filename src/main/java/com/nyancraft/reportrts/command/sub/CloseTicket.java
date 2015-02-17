package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.Ticket;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.data.User;
import com.nyancraft.reportrts.event.TicketCloseEvent;
import com.nyancraft.reportrts.persistence.DataProvider;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class CloseTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();
    private static DataProvider data = plugin.getDataProvider();

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

        if(!RTSPermissions.canCloseTicket(sender)) {
            if(RTSPermissions.canCloseOwnTicket(sender)) {

                if(!plugin.tickets.containsKey(ticketId)){
                    sender.sendMessage(Message.parse("generalInternalError", "Ticket not found."));
                    return true;
                }
                Player player = (Player) sender;
                if(!plugin.tickets.get(ticketId).getUUID().equals(player.getUniqueId())){
                    sender.sendMessage(Message.parse("generalInternalError", "You are not the owner of that ticket."));
                    return true;
                }
                data.deleteTicket(ticketId);
                plugin.tickets.remove(ticketId);
                try {
                    BungeeCord.globalNotify(Message.parse("completedReq", args[1], "Cancellation System"), ticketId, NotificationType.DELETE);
                } catch(IOException e) {
                    e.printStackTrace();
                }
                RTSFunctions.messageStaff(Message.parse("completedReq", args[1],"Cancellation System"), false);
                sender.sendMessage(Message.parse("completedUser", "Cancellation System"));

                return true;
            } else {
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.complete or reportrts.command.complete.self"));
                return true;
            }
        }

        User user = sender instanceof Player ? data.getUser(((Player) sender).getUniqueId(), 0, true) : data.getConsole();
        if(user.getUsername() == null) {
            sender.sendMessage(Message.parse("generalInternalError", "user.getUsername() returned NULL! Are you using plugins to modify names?"));
            return true;
        }

        args[0] = null;
        String comment = RTSFunctions.implode(args, " ");

        if(args[1].length() == comment.length())
            comment = null;
        else
            comment = comment.substring(args[1].length()).trim();

        int online = 0;
        boolean isClaimedByOther = false;

        if(plugin.tickets.containsKey(ticketId)) {
            online = (RTSFunctions.isUserOnline(plugin.tickets.get(ticketId).getUUID())) ? 1 : 0;
            if(plugin.tickets.get(ticketId).getStatus() == 1) {
                // Holy shit.
                isClaimedByOther = (!plugin.tickets.get(ticketId).getStaffUuid().equals((sender instanceof Player ? ((Player) sender).getUniqueId() : data.getConsole())));
            }
        }

        if(isClaimedByOther && !sender.hasPermission("reportrts.override")) {
            sender.sendMessage(Message.parse("generalInternalError", "Request #" + ticketId + " is claimed by someone else."));
            return true;
        }

        long timestamp = System.currentTimeMillis() / 1000;
        if(data.setTicketStatus(ticketId, user.getUuid(), sender.getName(), 3, comment, online > 0, timestamp) < 1) {
            sender.sendMessage(Message.parse("generalInternalError", "Unable to close ticket #" + args[0]));
            return true;
        }

        Ticket data = null;
        if(plugin.tickets.containsKey(ticketId)) {
            Player player = sender.getServer().getPlayer(plugin.tickets.get(ticketId).getUUID());
            if(online == 0) plugin.notifications.put(ticketId, plugin.tickets.get(ticketId).getUUID());
            if(player != null){
                player.sendMessage(Message.parse("completedUser", user.getUsername()));
                if(comment == null) comment = "";
                player.sendMessage(Message.parse("completedText", plugin.tickets.get(ticketId).getMessage(), comment));
            } else {
                try {
                    BungeeCord.notifyUser(plugin.tickets.get(ticketId).getUUID(), Message.parse("completedUser", user.getUsername()), ticketId);
                    if(comment == null) comment = "";
                    BungeeCord.notifyUser(plugin.tickets.get(ticketId).getUUID(), Message.parse("completedText", plugin.tickets.get(ticketId).getMessage(), comment), ticketId);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            data = plugin.tickets.get(ticketId);
            plugin.tickets.remove(ticketId);
        }

        try {
            BungeeCord.globalNotify(Message.parse("completedReq", args[1], user.getUsername()), ticketId, NotificationType.COMPLETE);
        } catch(IOException e) {
            e.printStackTrace();
        }
        RTSFunctions.messageStaff(Message.parse("completedReq", args[1], user.getUsername()), false);
        if(data != null) {
            data.setComment(comment);
            if (data.getStaffName() == null) {
                data.setStaffName(sender.getName());
            }
            plugin.getServer().getPluginManager().callEvent(new TicketCloseEvent(data, sender));
        }
        return true;
    }
}