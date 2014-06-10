package com.nyancraft.reportrts.command;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.HelpRequest;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.event.ReportCreateEvent;
import com.nyancraft.reportrts.persistence.Database;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;
import com.nyancraft.reportrts.util.BungeeCord;

public class ModreqCommand implements CommandExecutor {

    private ReportRTS plugin;
    private Database dbManager;

    public ModreqCommand(ReportRTS plugin) {
        this.plugin = plugin;
        this.dbManager = DatabaseManager.getDatabase();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0) return false;
        if(!(sender instanceof Player)) {
            sender.sendMessage("[ReportRTS] Some information will not be correct, such as location.");
            int userId = dbManager.getUserId("CONSOLE");
            UUID uuid = dbManager.getUserUUID(userId);
            String message = RTSFunctions.implode(args, " ");
            Location location = plugin.getServer().getWorlds().get(0).getSpawnLocation();
            String world = plugin.getServer().getWorlds().get(0).getName();
            if(!dbManager.fileRequest("CONSOLE", world, location, message, userId)){
                sender.sendMessage(Message.parse("generalInternalError", "Request could not be filed."));
                return true;
            }
            int ticketId = dbManager.getLatestTicketIdByUser(userId);
            HelpRequest request = new HelpRequest("CONSOLE", uuid, ticketId, System.currentTimeMillis()/1000, message, 0, location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getPitch(), world, BungeeCord.getServer(), "");
            plugin.getServer().getPluginManager().callEvent(new ReportCreateEvent(request));
            plugin.requestMap.put(ticketId, request);
            if(plugin.notifyStaffOnNewRequest){
                try{
                    BungeeCord.globalNotify(Message.parse("modreqFiledMod", "CONSOLE", ticketId), ticketId, NotificationType.NEW);
                }catch(IOException e){
                    e.printStackTrace();
                }
                RTSFunctions.messageMods(Message.parse("modreqFiledMod","CONSOLE", ticketId), true);
            }
            return true;
        }
        if(!RTSPermissions.canFileRequest(sender)) return true;
        if(plugin.requestMinimumWords > args.length){
            sender.sendMessage(Message.parse("modreqTooShort", plugin.requestMinimumWords));
            return true;
        }
        Player player = (Player) sender;
        if(RTSFunctions.getOpenRequestsByUser(player) >= plugin.maxRequests && !(ReportRTS.permission != null ? ReportRTS.permission.has(sender, "reportrts.command.modreq.unlimited") : sender.hasPermission("reportrts.command.modreq.unlimited"))) {
            sender.sendMessage(Message.parse("modreqTooManyOpen"));
            return true;
        }
        if(plugin.requestDelay > 0){
            if(!(ReportRTS.permission != null ? ReportRTS.permission.has(sender, "reportrts.command.modreq.unlimited") : sender.hasPermission("reportrts.command.modreq.unlimited"))){
                long timeBetweenRequest = RTSFunctions.checkTimeBetweenRequests(player);
                if(timeBetweenRequest > 0){
                    sender.sendMessage(Message.parse("modreqTooFast", timeBetweenRequest));
                    return true;
                }
            }
        }
        double start = 0;
        if(plugin.debugMode) start = System.nanoTime();

        String message = RTSFunctions.implode(args, " ");
        if(plugin.requestPreventDuplicate){
            for(Map.Entry<Integer, HelpRequest> entry : plugin.requestMap.entrySet()){
                if(!entry.getValue().getUUID().equals(player.getUniqueId())) continue;
                if(!entry.getValue().getMessage().equalsIgnoreCase(message)) continue;
                player.sendMessage(Message.parse("modreqDuplicate"));
                return true;
            }
        }
        int userId = dbManager.getUserId(player.getName(), player.getUniqueId(), true);
        if(!dbManager.fileRequest(player.getName(), player.getWorld().getName(), player.getLocation(), message, userId)) {
            sender.sendMessage(Message.parse("generalInternalError", "Request could not be filed."));
            return true;
        }
        int ticketId = dbManager.getLatestTicketIdByUser(userId);

        Location location = player.getLocation();

        sender.sendMessage(Message.parse("modreqFiledUser"));
        plugin.getLogger().log(Level.INFO, "" + player.getName() + " filed a request.");
        if(plugin.notifyStaffOnNewRequest){
            try{
                BungeeCord.globalNotify(Message.parse("modreqFiledMod", player.getName(), ticketId), ticketId, NotificationType.NEW);
            }catch(IOException e){
                e.printStackTrace();
            }
            RTSFunctions.messageMods(Message.parse("modreqFiledMod", player.getName(), ticketId), true);
        }

        HelpRequest request = new HelpRequest(player.getName(), player.getUniqueId(), ticketId, System.currentTimeMillis()/1000, message, 0, location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getPitch(), player.getWorld().getName(), BungeeCord.getServer(), "");
        plugin.getServer().getPluginManager().callEvent(new ReportCreateEvent(request));
        plugin.requestMap.put(ticketId, request);
        if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
        return true;
    }
}
