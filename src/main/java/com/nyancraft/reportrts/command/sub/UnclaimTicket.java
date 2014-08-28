package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.event.TicketUnclaimEvent;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class UnclaimTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();

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

        if(!plugin.requestMap.containsKey(ticketId) || plugin.requestMap.get(ticketId).getStatus() != 1) {
            sender.sendMessage(Message.parse("unclaimNotClaimed"));
            return true;
        }
        // CONSOLE overrides all.
        if(sender instanceof Player) {
            if(!((Player)sender).getUniqueId().equals(plugin.requestMap.get(ticketId).getModUUID()) && !RTSPermissions.canOverride(sender)) return true;
        }

        if(!DatabaseManager.getDatabase().setRequestStatus(ticketId, sender.getName(), 0, "", 0, System.currentTimeMillis() / 1000, true)) {
            sender.sendMessage(Message.parse("generalInternalError", "Unable to unclaim request #" + ticketId));
            return true;
        }

        Player player = sender.getServer().getPlayer(plugin.requestMap.get(ticketId).getUUID());
        if(player != null) {
            player.sendMessage(Message.parse("unclaimUser", plugin.requestMap.get(ticketId).getModName()));
            player.sendMessage(Message.parse("unclaimText", plugin.requestMap.get(ticketId).getMessage()));
        }
        plugin.requestMap.get(ticketId).setStatus(0);
        try {
            BungeeCord.globalNotify(Message.parse("unclaimReqMod", plugin.requestMap.get(ticketId).getModName(), args[1]), ticketId, NotificationType.MODIFICATION);
        } catch (IOException e) {
            e.printStackTrace();
        }

        RTSFunctions.messageMods(Message.parse("unclaimReqMod", plugin.requestMap.get(ticketId).getModName(), args[1]), false);

        sender.sendMessage(Message.parse("unclaimReqSelf", args[1]));

        plugin.getServer().getPluginManager().callEvent(new TicketUnclaimEvent(plugin.requestMap.get(ticketId), plugin.requestMap.get(ticketId).getModName(), sender));
        plugin.requestMap.get(ticketId).setModName(null);

        return true;
    }
}
