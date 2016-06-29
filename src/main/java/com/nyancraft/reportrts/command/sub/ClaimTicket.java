package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.event.TicketClaimEvent;
import com.nyancraft.reportrts.persistence.DataProvider;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class ClaimTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();
    private static DataProvider data = plugin.getDataProvider();

    private ClaimTicket() {
    }

    /**
     * Initial handling of the Claim sub-command.
     * @param sender player that sent the command
     * @param args arguments
     * @return true if command handled correctly
     */
    public static boolean handleCommand(CommandSender sender, String[] args) {

        if(args.length < 2) return false;

        if(!RTSPermissions.canClaimTicket(sender)) return true;
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

        String name = sender.getName();
        if(name == null) {
            sender.sendMessage(Message.error("Name is null! Try again."));
            return true;
        }

        long timestamp = System.currentTimeMillis() / 1000;

        switch(data.setTicketStatus(ticketId, (sender instanceof Player) ? ((Player) sender).getUniqueId() : data.getConsole().getUuid(),
                sender.getName(), 1, false, System.currentTimeMillis() / 1000)) {

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

        Player player = plugin.getServer().getPlayer(plugin.tickets.get(ticketId).getUUID());
        if(player != null) {
            player.sendMessage(Message.ticketClaimUser(name));
            player.sendMessage(Message.ticketText(plugin.tickets.get(ticketId).getMessage()));
        }

        plugin.tickets.get(ticketId).setStatus(1);
        // Workaround for CONSOLE.
        plugin.tickets.get(ticketId).setStaffUuid((!(sender instanceof Player) ? data.getConsole().getUuid() : ((Player) sender).getUniqueId()));
        plugin.tickets.get(ticketId).setStaffTime(timestamp);
        plugin.tickets.get(ticketId).setStaffName(name);

        try {
            BungeeCord.globalNotify(Message.ticketClaim(name, args[1]), ticketId, NotificationType.MODIFICATION);
        } catch(IOException e) {
            e.printStackTrace();
        }
        RTSFunctions.messageStaff(Message.ticketClaim(name, args[1]), false);

        // Let other plugins know the request was claimed
        plugin.getServer().getPluginManager().callEvent(new TicketClaimEvent(plugin.tickets.get(ticketId)));

        return true;
    }
}
