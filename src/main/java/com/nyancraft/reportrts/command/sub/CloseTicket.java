package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.Comment;
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
import java.util.TreeSet;

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
            sender.sendMessage(Message.errorTicketNaN(args[1]));
            return true;
        }
        int ticketId = Integer.parseInt(args[1]);

        if(!RTSPermissions.canCloseTicket(sender)) {
            if(RTSPermissions.canCloseOwnTicket(sender)) {

                if(!plugin.tickets.containsKey(ticketId)){
                    sender.sendMessage(Message.ticketNotExists(ticketId));
                    return true;
                }
                Player player = (Player) sender;
                if(!plugin.tickets.get(ticketId).getUUID().equals(player.getUniqueId())){
                    sender.sendMessage(Message.errorTicketOwner());
                    return true;
                }
                data.deleteTicket(ticketId);
                plugin.tickets.remove(ticketId);
                try {
                    BungeeCord.globalNotify(Message.ticketClose(args[1], "Cancellation System"), ticketId, NotificationType.DELETE);
                } catch(IOException e) {
                    e.printStackTrace();
                }
                RTSFunctions.messageStaff(Message.ticketClose(args[1],"Cancellation System"), false);
                sender.sendMessage(Message.ticketCloseUser(args[1], "Cancellation System"));

                return true;
            } else {
                sender.sendMessage(Message.errorPermission("reportrts.command.close or reportrts.command.close.self"));
                return true;
            }
        }

        User user = sender instanceof Player ? data.getUser(((Player) sender).getUniqueId(), 0, true) : data.getConsole();
        if(user.getUsername() == null) {
            sender.sendMessage(Message.error("user.getUsername() returned NULL! Are you using plugins to modify names?"));
            return true;
        }

        args[0] = null;
        int commentId = 0;
        String comment = RTSFunctions.implode(args, " ");
        String name = sender.getName();

        long timestamp = System.currentTimeMillis() / 1000;


        if(args[1].length() == comment.length()) {
            comment = null;
        } else {
            comment = comment.substring(args[1].length()).trim();

            name = sender instanceof Player ? plugin.staff.contains(user.getUuid()) ? sender.getName() + " - Staff" : sender.getName() : sender.getName();

            // Create a comment and store the comment ID.
            commentId = data.createComment(name, timestamp, comment, ticketId);
            // If less than 1, then the creation of the comment failed.
            if(commentId < 1) {
                sender.sendMessage(Message.error("Comment could not be created."));
                return true;
            }
        }


        int online = 0;
        boolean isClaimedByOther = false;

        if(plugin.tickets.containsKey(ticketId)) {
            online = (RTSFunctions.isUserOnline(plugin.tickets.get(ticketId).getUUID())) ? 1 : 0;
            if(plugin.tickets.get(ticketId).getStatus() == 1) {
                // Holy shit.
                isClaimedByOther = (!plugin.tickets.get(ticketId).getStaffUuid().equals((sender instanceof Player ? ((Player) sender).getUniqueId() : data.getConsole())));
            }
        }

        if(isClaimedByOther && !sender.hasPermission("reportrts.bypass.claim")) {
            sender.sendMessage(Message.errorTicketClaim(ticketId, plugin.tickets.get(ticketId).getStaffName()));
            return true;
        }

        if(data.setTicketStatus(ticketId, user.getUuid(), sender.getName(), 3, comment, online > 0, timestamp) < 1) {
            sender.sendMessage(Message.error("Unable to close ticket #" + args[0]));
            return true;
        }

        Ticket ticket = null;
        if(plugin.tickets.containsKey(ticketId)) {

            Player player = sender.getServer().getPlayer(plugin.tickets.get(ticketId).getUUID());

            if(online == 0) plugin.notifications.put(ticketId, plugin.tickets.get(ticketId).getUUID());

            if(player != null) {
                // If player is online, send him closing message and comments.
                player.sendMessage(Message.ticketCloseUser(args[1], user.getUsername()));
                player.sendMessage(Message.ticketCloseText(plugin.tickets.get(ticketId).getMessage()));
                if(commentId > 0) player.sendMessage(Message.ticketCommentText(name, comment));
            } else {
                try {
                    BungeeCord.notifyUser(plugin.tickets.get(ticketId).getUUID(), Message.ticketCloseUser(Integer.toString(ticketId), user.getUsername()), ticketId);
                    BungeeCord.notifyUser(plugin.tickets.get(ticketId).getUUID(), Message.ticketCloseText(plugin.tickets.get(ticketId).getMessage()), ticketId);
                    if(commentId > 0) BungeeCord.notifyUser(plugin.tickets.get(ticketId).getUUID(), Message.ticketCommentText(name, comment), ticketId);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            ticket = plugin.tickets.get(ticketId);
            plugin.tickets.remove(ticketId);
        }

        try {
            BungeeCord.globalNotify(Message.ticketClose(args[1], user.getUsername()), ticketId, NotificationType.COMPLETE);
        } catch(IOException e) {
            e.printStackTrace();
        }
        RTSFunctions.messageStaff(Message.ticketClose(args[1], user.getUsername()), false);
        if(ticket != null) {

            if(commentId > 0) {
                TreeSet<Comment> comments = plugin.tickets.get(ticketId).getComments();
                comments.add(new Comment(timestamp, ticketId, commentId, name, comment));
                ticket.setComments(comments);
            }

            if (ticket.getStaffName() == null) {
                ticket.setStaffName(sender.getName());
            }
            plugin.getServer().getPluginManager().callEvent(new TicketCloseEvent(ticket, sender));
        }
        return true;
    }
}