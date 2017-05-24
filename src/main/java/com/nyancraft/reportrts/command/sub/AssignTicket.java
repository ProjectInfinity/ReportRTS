package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.data.User;
import com.nyancraft.reportrts.event.TicketAssignEvent;
import com.nyancraft.reportrts.persistence.DataProvider;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class AssignTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();
    private static DataProvider data = plugin.getDataProvider();

    private AssignTicket() {
    }

    /**
     * Initial handling of the AssignTicket sub-command.
     * @param sender player that sent the command
     * @param args arguments
     * @return true if command handled correctly
     */
    public static boolean handleCommand(CommandSender sender, String[] args) {

        if(args.length < 3) return false;

        if(!RTSPermissions.canAssignTickets(sender)) return true;
        if(!RTSFunctions.isNumber(args[1])) {
            sender.sendMessage(Message.errorTicketNaN(args[1]));
            return true;
        }
        int ticketId = Integer.parseInt(args[1]);

        // The ticket the user is trying to claim is not open.
        if(!plugin.tickets.containsKey(ticketId)){
            sender.sendMessage(Message.ticketNotOpen(ticketId));
            return true;
        }

        String assignee = args[2];
        if(assignee == null) {
            sender.sendMessage(Message.error("Your name or assignee is null! Try again."));
            return true;
        }

        User user = data.getUnsafeUser(assignee);

        if(user == null) {
            sender.sendMessage(Message.errorUserNotExists(assignee));
            return true;
        }

        long timestamp = System.currentTimeMillis() / 1000;

        switch(data.setTicketStatus(ticketId, user.getUuid(),
                user.getUsername(), 1, false, System.currentTimeMillis() / 1000)) {

            case -3:
                // Ticket does not exist.
                sender.sendMessage(Message.ticketNotExists(ticketId));
                return true;

            case -2:
                // Ticket status incompatibilities.
                sender.sendMessage(Message.errorTicketStatus());
                return true;

            case -1:
                // Username is invalid or does not exist.
                sender.sendMessage(Message.error("Your user does not exist in the user table and was not successfully created."));
                return true;

            case 0:
                // No row was affected...
                sender.sendMessage(Message.error("No entries were affected. Check console for errors."));
                return true;

            case 1:
                // Everything went swimmingly if case is 1.
                break;


            default:
                sender.sendMessage(Message.error("A invalid result code has occurred."));
                return true;

        }

        Player player = sender.getServer().getPlayer(plugin.tickets.get(ticketId).getUUID());
        if(player != null) {
            player.sendMessage(Message.ticketAssignUser(assignee));
            player.sendMessage(Message.ticketText(plugin.tickets.get(ticketId).getMessage()));
        }
        plugin.tickets.get(ticketId).setStatus(1);
        plugin.tickets.get(ticketId).setStaffUuid(user.getUuid());
        plugin.tickets.get(ticketId).setStaffTime(timestamp);
        plugin.tickets.get(ticketId).setStaffName(assignee);

        try {
            BungeeCord.globalNotify(Message.ticketAssign(assignee, ticketId), ticketId, NotificationType.MODIFICATION);
        } catch(IOException e) {
            e.printStackTrace();
        }
        RTSFunctions.messageStaff(Message.ticketAssign(assignee, ticketId), false);
        // Let other plugins know the request was assigned.
        plugin.getServer().getPluginManager().callEvent(new TicketAssignEvent(plugin.tickets.get(ticketId), sender));

        return true;
    }
}
