package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.event.TicketUnclaimEvent;
import com.nyancraft.reportrts.persistence.DataProvider;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class UnclaimTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();
    private static DataProvider data = plugin.getDataProvider();

    /**
     * Initial handling of the Unclaim sub-command.
     * @param sender player that sent the command
     * @param args arguments
     * @return true if command handled correctly
     */
    public static boolean handleCommand(CommandSender sender, String[] args) {

        if(args.length < 2) return false;
        if(!RTSPermissions.canClaimTicket(sender)) return true;
        if(!RTSFunctions.isNumber(args[1])) {
            sender.sendMessage(Message.parse("generalInternalError", "Ticket ID must be a number, provided: " + args[1]));
            return true;
        }
        int ticketId = Integer.parseInt(args[1]);

        if(!plugin.tickets.containsKey(ticketId) || plugin.tickets.get(ticketId).getStatus() != 1) {
            sender.sendMessage(Message.parse("unclaimNotClaimed"));
            return true;
        }
        // CONSOLE overrides all.
        if(sender instanceof Player) {
            if(!((Player)sender).getUniqueId().equals(plugin.tickets.get(ticketId).getModUUID()) && !RTSPermissions.canOverride(sender)) return true;
        }

        switch(data.setTicketStatus(ticketId, (sender instanceof Player) ? ((Player) sender).getUniqueId() : data.getConsole().getUuid(),
                sender.getName(), 0, "", false, System.currentTimeMillis() / 1000)) {

            case -3:
                // Ticket does not exist.
                sender.sendMessage(Message.parse("generalInternalError", "Ticket does not exist."));
                return true;

            case -2:
                // Ticket status incompatibilities.
                sender.sendMessage(Message.parse("generalInternalError", "Ticket status incompatibilities! Check status."));
                return true;

            case -1:
                // Username is invalid or does not exist.
                sender.sendMessage(Message.parse("generalInternalError", "Your user does not exist in the user table and was not successfully created."));
                return true;

            case 0:
                // No row was affected...
                sender.sendMessage(Message.parse("generalInternalError", "No entries were affected. Check console for errors."));
                return true;

            case 1:
                // Everything went swimmingly if case is 1.
                break;


            default:
                sender.sendMessage(Message.parse("generalInternalError", "A invalid result code has occurred."));
                return true;

        }

        Player player = sender.getServer().getPlayer(plugin.tickets.get(ticketId).getUUID());
        if(player != null) {
            player.sendMessage(Message.parse("unclaimUser", plugin.tickets.get(ticketId).getModName()));
            player.sendMessage(Message.parse("unclaimText", plugin.tickets.get(ticketId).getMessage()));
        }
        plugin.tickets.get(ticketId).setStatus(0);
        try {
            BungeeCord.globalNotify(Message.parse("unclaimReqMod", plugin.tickets.get(ticketId).getModName(), args[1]), ticketId, NotificationType.MODIFICATION);
        } catch (IOException e) {
            e.printStackTrace();
        }

        RTSFunctions.messageStaff(Message.parse("unclaimReqMod", plugin.tickets.get(ticketId).getModName(), args[1]), false);

        sender.sendMessage(Message.parse("unclaimReqSelf", args[1]));

        plugin.getServer().getPluginManager().callEvent(new TicketUnclaimEvent(plugin.tickets.get(ticketId), plugin.tickets.get(ticketId).getModName(), sender));
        plugin.tickets.get(ticketId).setModName(null);

        return true;
    }
}
