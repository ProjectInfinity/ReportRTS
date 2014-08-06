package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.event.ReportReopenEvent;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class ReopenTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();

    /**
     * Initial handling of the Reopen sub-command.
     * @param sender player that sent the command
     * @param args arguments
     * @return true if command handled correctly
     */
    public static boolean handleCommand(CommandSender sender, String[] args) {

        if(args.length < 2 || !RTSFunctions.isNumber(args[1])) {
            sender.sendMessage(Message.parse("generalInternalError", "Ticket ID must be a number, provided: " + args[1]));
            return true;
        }

        if(!RTSPermissions.canCompleteRequests(sender)) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.complete"));
            return true;
        }

        int ticketId = Integer.parseInt(args[1]);

        if(!DatabaseManager.getDatabase().setRequestStatus(ticketId, sender.getName(), 0, "", 0, System.currentTimeMillis() / 1000, true)) {
            sender.sendMessage(Message.parse("generalInternalError", "Unable to reopen request #" + args[1]));
            return true;
        }

        if(RTSFunctions.syncTicket(ticketId)) {
            try {
                BungeeCord.globalNotify(Message.parse("reopenedRequest", sender.getName(), args[1]), ticketId, NotificationType.NEW);
            } catch(IOException e) {
                e.printStackTrace();
            }
            RTSFunctions.messageMods(Message.parse("reopenedRequest", sender.getName(), args[1]), true);
            // Let other plugins know the request was assigned.
            plugin.getServer().getPluginManager().callEvent(new ReportReopenEvent(plugin.requestMap.get(ticketId), sender));
            sender.sendMessage(Message.parse("reopenedRequestSelf", ticketId));

            return true;
        } else {
            sender.sendMessage(Message.parse("generalInternalError", "Unable to reopen request #" + args[1]));
            return true;
        }
    }
}