package com.nyancraft.reportrts.command;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.HelpRequest;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;

public class TeleportCommand implements CommandExecutor {

    private ReportRTS plugin;

    public TeleportCommand(ReportRTS plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("[ReportRTS] You need to be player to teleport.");
            return true;
        }
        if(!RTSPermissions.canTeleport(sender)) return true;
        try{
            if(!RTSFunctions.isParsableToInt(args[0])) return false;
        }catch(ArrayIndexOutOfBoundsException e){
            return false;
        }
        int ticketId = Integer.parseInt(args[0]);
        Player player = (Player) sender;

        if(!plugin.requestMap.containsKey(ticketId)){

            ResultSet rs = DatabaseManager.getDatabase().getLocationById(ticketId);
            Location location;
            try {
                if(!rs.isBeforeFirst()){
                    sender.sendMessage(Message.parse("generalRequestNotFound", args[0]));
                    return true;
                }
                if(plugin.useMySQL) rs.first();
                location = new Location(player.getServer().getWorld(rs.getString("world")), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getFloat("yaw"), rs.getFloat("pitch"));
                rs.close();
            } catch (SQLException e) {
                sender.sendMessage(ChatColor.RED + "[ReportRTS] An unexpected error occured when trying to fall back upon the database.");
                e.printStackTrace();
                return true;
            }
            if(!player.teleport(location)){
                sender.sendMessage(ChatColor.RED + "[ReportRTS] Teleportation failed due to an unexpected error.");
                return true;
            }
            player.sendMessage(Message.parse("teleportToRequest", args[0]));
            return true;
        }

        HelpRequest currentRequest = plugin.requestMap.get(ticketId);

        Location location = new Location(player.getServer().getWorld(currentRequest.getWorld()), currentRequest.getX(), currentRequest.getY(), currentRequest.getZ(), currentRequest.getYaw(), currentRequest.getPitch());

        if(!player.teleport(location)){
            sender.sendMessage(ChatColor.RED + "[ReportRTS] Teleportation failed due to an unexpected error.");
            return true;
        }
        player.sendMessage(Message.parse("teleportToRequest", args[0]));
        return true;
    }

}
