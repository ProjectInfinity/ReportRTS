package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.Ticket;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TeleportTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();

    /**
     * Initial handling of the Teleport sub-command.
     * @param sender player that sent the command
     * @param args arguments
     * @return true if command handled correctly
     */
    public static boolean handleCommand(CommandSender sender, String[] args) {

        if(!(sender instanceof Player)) {
            sender.sendMessage("[ReportRTS] You need to be a player to teleport.");
            return true;
        }
        if(!RTSPermissions.canTeleport(sender)) return true;
        if(args.length < 2 || !RTSFunctions.isNumber(args[1])) return false;

        int ticketId = Integer.parseInt(args[1]);
        Player player = (Player) sender;

        // Ticket status not open.
        if(!plugin.requestMap.containsKey(ticketId)) {

            Location location;
            try(ResultSet rs = DatabaseManager.getDatabase().getLocationById(ticketId)) {
                if(!rs.isBeforeFirst()) {
                    player.sendMessage(Message.parse("generalRequestNotFound", ticketId));
                    return true;
                }
                rs.first();

                String bungeeServer = rs.getString("bc_server");
                if(plugin.bungeeCordSupport && !bungeeServer.equals(BungeeCord.getServer())){
                    try{
                        BungeeCord.teleportUser(player, bungeeServer, ticketId);
                    }catch(IOException e){
                        player.sendMessage(ChatColor.RED + "[ReportRTS] BungeeCord teleportation failed due to an unexpected error.");
                    }
                    return true;
                }
                location = new Location(plugin.getServer().getWorld(rs.getString("world")), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getFloat("yaw"), rs.getFloat("pitch"));
            } catch (SQLException e) {
                player.sendMessage(ChatColor.RED + "[ReportRTS] An unexpected error occurred when trying to fall back upon the database.");
                e.printStackTrace();
                return true;
            }
            if(location.getWorld() == null) {
                player.sendMessage(ChatColor.RED + "[ReportRTS] World is null! Attempting to teleport to that ticket will cause a NullPointerException.");
                return true;
            }
            if(!player.teleport(location)) {
                player.sendMessage(ChatColor.RED + "[ReportRTS] Teleportation failed due to an unexpected error.");
                return true;
            }
            player.sendMessage(Message.parse("teleportToRequest", args[1]));
            return true;
        }

        // Ticket status open.
        Ticket currentRequest = plugin.requestMap.get(ticketId);

        if(plugin.bungeeCordSupport && !currentRequest.getBungeeCordServer().equals(BungeeCord.getServer())) {
            try {
                BungeeCord.teleportUser(player, currentRequest.getBungeeCordServer(), currentRequest.getId());
            } catch(IOException e) {
                sender.sendMessage(ChatColor.RED + "[ReportRTS] BungeeCord teleportation failed due to an unexpected error.");
            }
            return true;
        }

        Location location = new Location(player.getServer().getWorld(currentRequest.getWorld()), currentRequest.getX(), currentRequest.getY(), currentRequest.getZ(), currentRequest.getYaw(), currentRequest.getPitch());
        if(location.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "[ReportRTS] World is null! Attempting to teleport to that ticket will cause a NullPointerException.");
            return true;
        }
        if(!player.teleport(location)) {
            sender.sendMessage(ChatColor.RED + "[ReportRTS] Teleportation failed due to an unexpected error.");
            return true;
        }
        player.sendMessage(Message.parse("teleportToRequest", args[1]));
        return true;
    }
}