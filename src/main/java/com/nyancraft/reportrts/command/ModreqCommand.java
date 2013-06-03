package com.nyancraft.reportrts.command;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportCreateEvent;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.HelpRequest;
import com.nyancraft.reportrts.persistence.Database;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;

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
            int userId = dbManager.getUserId("CONSOLE", true);
            String message = RTSFunctions.implode(args, " ");
            Location location = plugin.getServer().getWorlds().get(0).getSpawnLocation();
            String world = plugin.getServer().getWorlds().get(0).getName();
            if(!dbManager.fileRequest("CONSOLE", world, location, message, userId)){
                sender.sendMessage(Message.parse("generalInternalError", "Request could not be filed."));
                return true;
            }
            int ticketId = dbManager.getLatestTicketIdByUser(userId);
            HelpRequest request = new HelpRequest("CONSOLE", ticketId, System.currentTimeMillis()/1000, message, 0, location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getPitch(), world);
            plugin.getServer().getPluginManager().callEvent(new ReportCreateEvent(request));
            plugin.requestMap.put(ticketId, request);
            if(plugin.notifyStaffOnNewRequest) RTSFunctions.messageMods(Message.parse("modreqFiledMod","CONSOLE", ticketId), true);
            return true;
        }
        if(!RTSPermissions.canFileRequest(sender)) return true;
        if(plugin.requestMinimumWords > args.length){
            sender.sendMessage(Message.parse("modreqTooShort", plugin.requestMinimumWords));
            return true;
        }
        if(RTSFunctions.getOpenRequestsByUser(sender) >= plugin.maxRequests && !(ReportRTS.permission != null ? ReportRTS.permission.has(sender, "reportrts.command.modreq.unlimited") : sender.hasPermission("reportrts.command.modreq.unlimited"))) {
            sender.sendMessage(Message.parse("modreqTooManyOpen"));
            return true;
        }
        if(plugin.requestDelay > 0){
            if(!(ReportRTS.permission != null ? ReportRTS.permission.has(sender, "reportrts.command.modreq.unlimited") : sender.hasPermission("reportrts.command.modreq.unlimited"))){
                long timeBetweenRequest = RTSFunctions.checkTimeBetweenRequests(sender);
                if(timeBetweenRequest > 0){
                    sender.sendMessage(Message.parse("modreqTooFast", timeBetweenRequest));
                    return true;
                }
            }
        }
        double start = 0;
        if(plugin.debugMode) start = System.nanoTime(); // Production value: System.currentTimeMillis(); Development value: System.nanoTime();

        Player player = (Player)sender;
        String message = RTSFunctions.implode(args, " ");
        int userId = dbManager.getUserId(player.getName(), true);
        if(!dbManager.fileRequest(player.getName(), player.getWorld().getName(), player.getLocation(), message, userId)) {
            sender.sendMessage(Message.parse("generalInternalError", "Request could not be filed."));
            return true;
        }
        int ticketId = dbManager.getLatestTicketIdByUser(userId);

        Location location = player.getLocation();

        sender.sendMessage(Message.parse("modreqFiledUser"));
        plugin.getLogger().log(Level.INFO, "" + player.getName() + " filed a request.");
        if(plugin.notifyStaffOnNewRequest) RTSFunctions.messageMods(Message.parse("modreqFiledMod", player.getName(), ticketId), true);

        HelpRequest request = new HelpRequest(player.getName(), ticketId, System.currentTimeMillis()/1000, message, 0, location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getPitch(), player.getWorld().getName());
        plugin.getServer().getPluginManager().callEvent(new ReportCreateEvent(request));
        plugin.requestMap.put(ticketId, request);
        if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
        return true;
    }
}
