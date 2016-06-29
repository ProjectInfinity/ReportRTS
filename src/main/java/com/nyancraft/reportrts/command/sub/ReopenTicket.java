package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.data.User;
import com.nyancraft.reportrts.event.TicketReopenEvent;
import com.nyancraft.reportrts.persistence.DataProvider;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class ReopenTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();
    private static DataProvider data = plugin.getDataProvider();

    private ReopenTicket() {
    }

    /**
     * Initial handling of the Reopen sub-command.
     * @param sender player that sent the command
     * @param args arguments
     * @return true if command handled correctly
     */
    public static boolean handleCommand(CommandSender sender, String[] args) {

        if(args.length < 2 || !RTSFunctions.isNumber(args[1])) {
            sender.sendMessage(Message.errorTicketNaN(args[1]));
            return true;
        }

        if(!RTSPermissions.canReopenTicket(sender)) return true;

        int ticketId = Integer.parseInt(args[1]);

        User user = sender instanceof Player ? data.getUser(((Player) sender).getUniqueId(), 0, true) : data.getConsole();

        if(data.setTicketStatus(ticketId, user.getUuid(), sender.getName(), 0, false, System.currentTimeMillis() / 1000) < 1) {
            sender.sendMessage(Message.errorTicketNotClosed(args[0]));
            return true;
        }

        if(RTSFunctions.syncTicket(ticketId)) {
            try {
                BungeeCord.globalNotify(Message.ticketReopen(sender.getName(), args[1]), ticketId, NotificationType.NEW);
            } catch(IOException e) {
                e.printStackTrace();
            }
            RTSFunctions.messageStaff(Message.ticketReopen(sender.getName(), args[1]), true);
            // Let other plugins know the ticket was reopened.
            plugin.getServer().getPluginManager().callEvent(new TicketReopenEvent(plugin.tickets.get(ticketId), sender));

            return true;
        } else {
            sender.sendMessage(Message.error("Unable to reopen request #" + args[1]));
            return true;
        }
    }
}