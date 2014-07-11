package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.HelpRequest;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.event.ReportCreateEvent;
import com.nyancraft.reportrts.persistence.Database;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class OpenTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();
    private static Database dbManager = DatabaseManager.getDatabase();

    // We store everything in variables then poll the information later.
    private static String username;
    private static Location location;
    private static int userId;
    private static UUID uuid;

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
        if(plugin.requestMinimumWords > (args.length - 1)) {
            sender.sendMessage(Message.parse("modreqTooShort", plugin.requestMinimumWords));
            return true;
        }

        username = sender.getName();
        if(!(sender instanceof Player)) {
            // Sender is more than likely Console.
            userId = dbManager.getUserId(username);
            location = plugin.getServer().getWorlds().get(0).getSpawnLocation();
            uuid = dbManager.getUserUUID(userId);
        }
        else {
            Player player = (Player) sender;
            userId = dbManager.getUserId(sender.getName(), player.getUniqueId(), true);
            location = player.getLocation();
            uuid = player.getUniqueId();
        }

        if(RTSFunctions.getOpenRequestsByUser(uuid) >= plugin.maxRequests && !(ReportRTS.permission != null ? ReportRTS.permission.has(sender, "reportrts.command.modreq.unlimited") : sender.hasPermission("reportrts.command.modreq.unlimited"))) {
            sender.sendMessage(Message.parse("modreqTooManyOpen"));
            return true;
        }

        if(plugin.requestDelay > 0){
            if(!(ReportRTS.permission != null ? ReportRTS.permission.has(sender, "reportrts.command.modreq.unlimited") : sender.hasPermission("reportrts.command.modreq.unlimited"))){
                long timeBetweenRequest = RTSFunctions.checkTimeBetweenRequests(uuid);
                if(timeBetweenRequest > 0){
                    sender.sendMessage(Message.parse("modreqTooFast", timeBetweenRequest));
                    return true;
                }
            }
        }

        args[0] = null;
        String message = RTSFunctions.implode(args, " ");

        // Prevent duplicate requests by comparing UUID and message to other currently open requests.
        if(plugin.requestPreventDuplicate) {
            for(Map.Entry<Integer, HelpRequest> entry : plugin.requestMap.entrySet()){
                if(!entry.getValue().getUUID().equals(uuid)) continue;
                if(!entry.getValue().getMessage().equalsIgnoreCase(message)) continue;
                sender.sendMessage(Message.parse("modreqDuplicate"));
                return true;
            }
        }

        if(!dbManager.fileRequest(username, location.getWorld().getName(), location, message, userId)) {
            sender.sendMessage(Message.parse("generalInternalError", "Request could not be filed."));
            return true;
        }
        int ticketId = dbManager.getLatestTicketIdByUser(userId);

        sender.sendMessage(Message.parse("modreqFiledUser"));
        plugin.getLogger().log(Level.INFO, "" + username + " filed a request.");

        // Notify staff members about the new request.
        if(plugin.notifyStaffOnNewRequest) {
            try {
                // Attempt to notify all servers connected to BungeeCord that run ReportRTS.
                BungeeCord.globalNotify(Message.parse("modreqFiledMod", username, Integer.toString(ticketId)), ticketId, NotificationType.NEW);
            } catch(IOException e) {
                e.printStackTrace();
            }
            RTSFunctions.messageMods(Message.parse("modreqFiledMod", username, Integer.toString(ticketId)), true);
        }

        HelpRequest request = new HelpRequest(username, uuid, ticketId, System.currentTimeMillis()/1000, message, 0, location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getPitch(), location.getWorld().getName(), BungeeCord.getServer(), "");
        plugin.getServer().getPluginManager().callEvent(new ReportCreateEvent(request));
        plugin.requestMap.put(ticketId, request);

        return true;
    }
}