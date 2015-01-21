package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.Ticket;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.data.User;
import com.nyancraft.reportrts.event.TicketCreateEvent;
import com.nyancraft.reportrts.persistence.DataProvider;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

public class OpenTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();
    private static DataProvider data = plugin.getDataProvider();

    /**
     * Initial handling of the Open sub-command.
     * @param sender player that sent the command
     * @param args arguments
     * @return true if command handled correctly
     */
    public static boolean handleCommand(CommandSender sender, String[] args) {

        if(!RTSPermissions.canFileRequest(sender)) return true;
        if(args.length < 2) return false;

        // Check if ticket message is too short.
        if(plugin.ticketMinimumWords > (args.length - 1)) {
            sender.sendMessage(Message.parse("modreqTooShort", plugin.ticketMinimumWords));
            return true;
        }

        // Store these variables, we're gonna need them.
        User user = new User();
        user.setUsername(sender.getName());
        Location location;

        if(!(sender instanceof Player)) {
            // Sender is more than likely Console.
            user = data.getConsole();
            location = plugin.getServer().getWorlds().get(0).getSpawnLocation();
        }
        else {
            // Sender is a Player.
            Player player = (Player) sender;
            user = data.getUser(player.getUniqueId(), 0, true);
            location = player.getLocation();
        }

        // The user is banned and can not create a ticket.
        if(user.getBanned()) {
            sender.sendMessage(Message.parse("generalInternalError", "You are banned from opening tickets."));
            return true;
        }

        if(RTSFunctions.getOpenTicketsByUser(user.getUuid()) >= plugin.maxTickets && !(ReportRTS.permission != null ? ReportRTS.permission.has(sender, "reportrts.command.modreq.unlimited") : sender.hasPermission("reportrts.command.modreq.unlimited"))) {
            sender.sendMessage(Message.parse("modreqTooManyOpen"));
            return true;
        }

        // Check if the sender can open another ticket yet.
        if(plugin.ticketDelay > 0) {
            if(!(ReportRTS.permission != null ? ReportRTS.permission.has(sender, "reportrts.command.modreq.unlimited") : sender.hasPermission("reportrts.command.modreq.unlimited"))){
                long timeBetweenRequest = RTSFunctions.checkTimeBetweenTickets(user.getUuid());
                if(timeBetweenRequest > 0) {
                    sender.sendMessage(Message.parse("modreqTooFast", timeBetweenRequest));
                    return true;
                }
            }
        }

        args[0] = null;
        String message = RTSFunctions.implode(args, " ");

        // Prevent duplicate requests by comparing UUID and message to other currently open requests.
        if(plugin.ticketPreventDuplicate) {
            for(Map.Entry<Integer, Ticket> entry : plugin.tickets.entrySet()){
                if(!entry.getValue().getUUID().equals(user.getUuid())) continue;
                if(!entry.getValue().getMessage().equalsIgnoreCase(message)) continue;
                sender.sendMessage(Message.parse("modreqDuplicate"));
                return true;
            }
        }

        // Create a ticket and store the ticket ID.
        int ticketId = data.createTicket(user, location, message);
        // If less than 1, then the creation of the ticket failed.
        if(ticketId < 1) {
            sender.sendMessage(Message.parse("generalInternalError", "Ticket could not be opened."));
            return true;
        }

        sender.sendMessage(Message.parse("modreqFiledUser"));
        plugin.getLogger().log(Level.INFO, "" + user.getUsername() + " filed a request.");

        // Notify staff members about the new request.
        if(plugin.notifyStaffOnNewRequest) {
            try {
                // Attempt to notify all servers connected to BungeeCord that run ReportRTS.
                BungeeCord.globalNotify(Message.parse("modreqFiledMod", user.getUsername(), Integer.toString(ticketId)), ticketId, NotificationType.NEW);
            } catch(IOException e) {
                e.printStackTrace();
            }
            RTSFunctions.messageMods(Message.parse("modreqFiledMod", user.getUsername(), Integer.toString(ticketId)), true);
        }

        Ticket request = new Ticket(user.getUsername(), user.getUuid(), ticketId, System.currentTimeMillis()/1000, message, 0, location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getPitch(), location.getWorld().getName(), BungeeCord.getServer(), "");
        plugin.getServer().getPluginManager().callEvent(new TicketCreateEvent(request));
        plugin.tickets.put(ticketId, request);

        return true;
    }
}