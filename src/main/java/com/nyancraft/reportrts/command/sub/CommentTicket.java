package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.Comment;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.data.Ticket;
import com.nyancraft.reportrts.data.User;
import com.nyancraft.reportrts.event.TicketCommentEvent;
import com.nyancraft.reportrts.persistence.DataProvider;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.TreeSet;

public class CommentTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();
    private static DataProvider data = plugin.getDataProvider();

    /**
     * Initial handling of the comment sub-command.
     *
     * @param sender player that sent the command
     * @param args arguments
     * @return true if command handled correctly
     */
    public static boolean handleCommand(CommandSender sender, String[] args) {

        if(!RTSPermissions.canComment(sender)) return true;

        if(args.length < 3) return false;

        if(!RTSFunctions.isNumber(args[1])) {
            sender.sendMessage(Message.errorTicketNaN(args[1]));
            return true;
        }

        int ticketId = Integer.parseInt(args[1]);

        // Ticket has to be open in order for us to comment on it.
        if(!plugin.tickets.containsKey(ticketId)) {
            sender.sendMessage(Message.ticketNotOpen(ticketId));
            return true;
        }

        User user = sender instanceof Player ? data.getUser(((Player) sender).getUniqueId(), 0, true) : data.getConsole();
        if(user.getUsername() == null) {
            sender.sendMessage(Message.error("user.getUsername() returned NULL! Are you using plugins to modify names?"));
            return true;
        }

        if(sender instanceof Player && plugin.tickets.get(ticketId).getUUID() != ((Player) sender).getUniqueId() && !RTSPermissions.canOverride(sender)) {
            sender.sendMessage(Message.errorTicketOwner());
            return true;
        }

        Ticket ticket = plugin.tickets.get(ticketId);
        TreeSet<Comment> comments = ticket.getComments();


        // Clean up arguments before combining the remaining into a comment.
        args[0] = null;
        args[1] = null;

        String comment = RTSFunctions.implode(args, " ").trim();

        String name = sender instanceof Player ? plugin.staff.contains(user.getUuid()) ? sender.getName() + " - Staff" : sender.getName() : sender.getName();

        long timestamp = System.currentTimeMillis() / 1000;

        // Create a comment and store the comment ID.
        int commentId = data.createComment(name, timestamp, comment, ticketId);
        // If less than 1, then the creation of the comment failed.
        if(commentId < 1) {
            sender.sendMessage(Message.error("Comment could not be created."));
            return true;
        }

        sender.sendMessage(Message.ticketCommentUser(Integer.toString(ticketId)));

        // Notify staff members about the new comment.
        try {
            // Attempt to notify all servers connected to BungeeCord that run ReportRTS.
            BungeeCord.globalNotify(Message.ticketComment(Integer.toString(ticketId), user.getUsername()), ticketId, NotificationType.NOTIFYONLY);
        } catch(IOException e) {
            e.printStackTrace();
        }

        RTSFunctions.messageStaff(Message.ticketComment(Integer.toString(ticketId), user.getUsername()), true);

        // Add a comment to the comment set.
        comments.add(new Comment(timestamp, ticketId, commentId, sender.getName(), comment));
        // Update the comments on the ticket.
        ticket.setComments(comments);
        plugin.tickets.put(ticketId, ticket);

        plugin.getServer().getPluginManager().callEvent(new TicketCommentEvent(plugin.tickets.get(ticketId), sender, comment));

        return true;
    }

}