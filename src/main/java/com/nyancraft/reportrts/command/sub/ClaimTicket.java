package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.event.TicketClaimEvent;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class ClaimTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();

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
            sender.sendMessage(Message.parse("generalInternalError", "Ticket ID must be a number, provided: " + args[1]));
            return true;
        }
        int ticketId = Integer.parseInt(args[1]);

        // The ticket the user is trying to claim is not open.
        if(!plugin.requestMap.containsKey(ticketId)){
            sender.sendMessage(Message.parse("claimNotOpen"));
            return true;
        }

        String name = sender.getName();
        if(name == null) {
            sender.sendMessage(Message.parse("generalInternalError", "Name is null! Try again."));
            return true;
        }

        long timestamp = System.currentTimeMillis() / 1000;

        if(!DatabaseManager.getDatabase().setRequestStatus(ticketId, name, 1, "", 0, timestamp, true)) {
            sender.sendMessage(Message.parse("generalInternalError", "Unable to claim request #" + ticketId));
            return true;
        }

        Player player = plugin.getServer().getPlayer(plugin.requestMap.get(ticketId).getUUID());
        if(player != null) {
            player.sendMessage(Message.parse("claimUser", name));
            player.sendMessage(Message.parse("claimText", plugin.requestMap.get(ticketId).getMessage()));
        }

        plugin.requestMap.get(ticketId).setStatus(1);
        // Workaround for CONSOLE.
        plugin.requestMap.get(ticketId).setModUUID((!(sender instanceof Player) ? plugin.consoleUUID : ((Player) sender).getUniqueId()));
        plugin.requestMap.get(ticketId).setModTimestamp(timestamp);
        plugin.requestMap.get(ticketId).setModName(name);

        try {
            BungeeCord.globalNotify(Message.parse("claimRequest", name, args[1]), ticketId, NotificationType.MODIFICATION);
        } catch(IOException e) {
            e.printStackTrace();
        }
        RTSFunctions.messageMods(Message.parse("claimRequest", name, args[1]), false);

        // Let other plugins know the request was claimed
        plugin.getServer().getPluginManager().callEvent(new TicketClaimEvent(plugin.requestMap.get(ticketId)));

        return true;
    }
}
