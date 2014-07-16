package com.nyancraft.reportrts.command.sub;


import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.event.ReportHoldEvent;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class HoldTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();

    /**
     * Initial handling of the Hold sub-command.
     * @param sender player that sent the command
     * @param args arguments
     * @return true if command handled correctly
     */
    public static boolean handleCommand(CommandSender sender, String[] args) {

        if(!RTSPermissions.canPutTicketOnHold(sender) || args.length < 2 || !RTSFunctions.isNumber(args[1])) return false;

        args[0] = "";
        String reason = RTSFunctions.implode(args, " ");
        int ticketId = Integer.parseInt(args[1]);

        // TODO: I forgot what this does.
        if(reason.length() <= args[1].length()) {
            reason = "None specified.";
        } else {
            reason = reason.substring(args[1].length());
        }

        if(!DatabaseManager.getDatabase().setRequestStatus(ticketId, sender.getName(), 2, reason, 0, System.currentTimeMillis() / 1000, true)) {
            sender.sendMessage(Message.parse("generalInternalError", "Unable to put request #" + ticketId + " on hold."));
            return true;
        }

        if(plugin.requestMap.containsKey(ticketId)) {

            Player player = sender.getServer().getPlayer(plugin.requestMap.get(ticketId).getUUID());
            if(player != null) {
                player.sendMessage(Message.parse("holdUser", sender.getName()));
                player.sendMessage(Message.parse("holdText", plugin.requestMap.get(ticketId).getMessage(), reason.trim()));
            }

            plugin.getServer().getPluginManager().callEvent(new ReportHoldEvent(plugin.requestMap.get(ticketId), reason, sender));

            plugin.requestMap.remove(ticketId);
        }

        try {
            BungeeCord.globalNotify(Message.parse("holdRequest", args[1], sender.getName()), ticketId, NotificationType.HOLD);
        } catch(IOException e) {
            e.printStackTrace();
        }
        RTSFunctions.messageMods(Message.parse("holdRequest", args[1], sender.getName()), false);
        return true;
    }
}