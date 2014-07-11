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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class OpenTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();
    private static Database dbManager = DatabaseManager.getDatabase();
    private static Map<String, Object> tempMap = new HashMap<>();

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

        // Make sure the map is empty before adding elements.
        tempMap.clear();
        // TODO: Make sure sender.getName() returns CONSOLE when console is the sender.
        /* We put everything into a temporary map then pull the information later
        in order to cut down on conditional statements. */

        tempMap.put("username", sender.getName());
        if(!(sender instanceof Player)) {
            // Sender is more than likely Console.
            tempMap.put("userid", Integer.toString(dbManager.getUserId(sender.getName())));
            tempMap.put("location", plugin.getServer().getWorlds().get(0).getSpawnLocation());
            tempMap.put("world", plugin.getServer().getWorlds().get(0).getName());
            tempMap.put("uuid", dbManager.getUserUUID((int) tempMap.get("userid")));
        }
        else {
            Player player = (Player) sender;
            tempMap.put("userid", Integer.toString(dbManager.getUserId(sender.getName(), player.getUniqueId(), true)));
            tempMap.put("location", player.getLocation());
            tempMap.put("world", player.getLocation().getWorld().getName());
            tempMap.put("uuid", dbManager.getUserId(sender.getName(), player.getUniqueId(), true));
        }

        if(RTSFunctions.getOpenRequestsByUser((UUID) tempMap.get("uuid")) >= plugin.maxRequests && !(ReportRTS.permission != null ? ReportRTS.permission.has(sender, "reportrts.command.modreq.unlimited") : sender.hasPermission("reportrts.command.modreq.unlimited"))) {
            sender.sendMessage(Message.parse("modreqTooManyOpen"));
            return true;
        }

        if(plugin.requestDelay > 0){
            if(!(ReportRTS.permission != null ? ReportRTS.permission.has(sender, "reportrts.command.modreq.unlimited") : sender.hasPermission("reportrts.command.modreq.unlimited"))){
                long timeBetweenRequest = RTSFunctions.checkTimeBetweenRequests((UUID) tempMap.get("uuid"));
                if(timeBetweenRequest > 0){
                    sender.sendMessage(Message.parse("modreqTooFast", timeBetweenRequest));
                    return true;
                }
            }
        }
        // TODO: Make sure start arguments are not part of the message.
        args[0] = "";
        String message = RTSFunctions.implode(args, " ");

        // Prevent duplicate requests by comparing UUID and message to other currently open requests.
        if(plugin.requestPreventDuplicate) {
            for(Map.Entry<Integer, HelpRequest> entry : plugin.requestMap.entrySet()){
                if(!entry.getValue().getUUID().equals(tempMap.get("uuid"))) continue;
                if(!entry.getValue().getMessage().equalsIgnoreCase(message)) continue;
                sender.sendMessage(Message.parse("modreqDuplicate"));
                return true;
            }
        }

        Location location = (Location) tempMap.get("location");

        if(!dbManager.fileRequest((String) tempMap.get("username"), (String) tempMap.get("world"), location, message, (int) tempMap.get("userid"))) {
            sender.sendMessage(Message.parse("generalInternalError", "Request could not be filed."));
            return true;
        }
        int ticketId = dbManager.getLatestTicketIdByUser((int) tempMap.get("userid"));

        sender.sendMessage(Message.parse("modreqFiledUser"));
        plugin.getLogger().log(Level.INFO, "" + tempMap.get("username") + " filed a request.");

        // Notify staff members about the new request.
        if(plugin.notifyStaffOnNewRequest) {
            try {
                // Attempt to notify all servers connected to BungeeCord that run ReportRTS.
                BungeeCord.globalNotify(Message.parse("modreqFiledMod", tempMap.get("username"), ticketId), ticketId, NotificationType.NEW);
            } catch(IOException e) {
                e.printStackTrace();
            }
            RTSFunctions.messageMods(Message.parse("modreqFiledMod", tempMap.get("username"), ticketId), true);
        }

        HelpRequest request = new HelpRequest((String) tempMap.get("username"), (UUID) tempMap.get("uuid"), ticketId, System.currentTimeMillis()/1000, message, 0, location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getPitch(), location.getWorld().getName(), BungeeCord.getServer(), "");
        plugin.getServer().getPluginManager().callEvent(new ReportCreateEvent(request));
        plugin.requestMap.put(ticketId, request);

        return true;
    }
}