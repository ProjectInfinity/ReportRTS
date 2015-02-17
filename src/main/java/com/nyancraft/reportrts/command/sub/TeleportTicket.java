package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.Ticket;
import com.nyancraft.reportrts.persistence.DataProvider;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class TeleportTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();
    private static DataProvider data = plugin.getDataProvider();

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
        if(!plugin.tickets.containsKey(ticketId)) {

            Ticket ticket = data.getTicket(ticketId);

            if(ticket == null) {
                player.sendMessage(Message.parse("generalRequestNotFound", ticketId));
                return true;
            }

            if(plugin.bungeeCordSupport && !ticket.getServer().equals(BungeeCord.getServer())) {
                try {
                    BungeeCord.teleportUser(player, ticket.getServer(), ticketId);
                } catch(IOException e) {
                    player.sendMessage(ChatColor.RED + "[ReportRTS] BungeeCord teleportation failed due to an unexpected error.");
                }
                return true;
            }

            World world = plugin.getServer().getWorld(ticket.getWorld());

            if(world == null) {
                player.sendMessage(ChatColor.RED + "[ReportRTS] World is null! Attempting to teleport to that ticket will cause a NullPointerException.");
                return true;
            }

            if(!player.teleport(new Location(
                    world,
                    ticket.getX(),
                    ticket.getY(),
                    ticket.getZ(),
                    ticket.getYaw(),
                    ticket.getPitch()
            ))) {

                player.sendMessage(ChatColor.RED + "[ReportRTS] Teleportation failed due to an unexpected error.");
                return true;

            }

            player.sendMessage(Message.parse("teleportToRequest", args[1]));
            return true;
        }

        // Ticket status open.
        Ticket ticket = plugin.tickets.get(ticketId);

        if(plugin.bungeeCordSupport && !ticket.getServer().equals(BungeeCord.getServer())) {
            try {
                BungeeCord.teleportUser(player, ticket.getServer(), ticket.getId());
            } catch(IOException e) {
                sender.sendMessage(ChatColor.RED + "[ReportRTS] BungeeCord teleportation failed due to an unexpected error.");
            }
            return true;
        }

        World world = plugin.getServer().getWorld(ticket.getWorld());

        if(world == null) {
            player.sendMessage(ChatColor.RED + "[ReportRTS] World is null! Attempting to teleport to that ticket will cause a NullPointerException.");
            return true;
        }

        if(!player.teleport(new Location(
                world,
                ticket.getX(),
                ticket.getY(),
                ticket.getZ(),
                ticket.getYaw(),
                ticket.getPitch()
        ))) {

            player.sendMessage(ChatColor.RED + "[ReportRTS] Teleportation failed due to an unexpected error.");
            return true;

        }

        player.sendMessage(Message.parse("teleportToRequest", args[1]));
        return true;
    }
}