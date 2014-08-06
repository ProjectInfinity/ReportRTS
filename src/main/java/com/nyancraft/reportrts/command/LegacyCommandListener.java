package com.nyancraft.reportrts.command;

import com.nyancraft.reportrts.ReportRTS;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class LegacyCommandListener implements Listener {

    private String readTicket;
    private String openTicket;
    private String closeTicket;
    private String reopenTicket;
    private String claimTicket;
    private String unclaimTicket;
    private String holdTicket;
    private String teleportToTicket;
    private String broadcastToStaff;
    private String listStaff;

    public LegacyCommandListener(String readTicket, String openTicket, String closeTicket, String reopenTicket, String claimTicket, String unclaimTicket, String holdTicket, String teleportToTicket, String broadcastToStaff, String listStaff) {
        this.readTicket = readTicket;
        this.openTicket = openTicket;
        this.closeTicket = closeTicket;
        this.reopenTicket = reopenTicket;
        this.claimTicket = claimTicket;
        this.unclaimTicket = unclaimTicket;
        this.holdTicket = holdTicket;
        this.teleportToTicket = teleportToTicket;
        this.broadcastToStaff = broadcastToStaff;
        this.listStaff = listStaff;
    }
    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        if(event.getMessage().length() < 1) return;
        // Please don't use this unless you have to. :(
        if(event.getMessage().split(" ")[0].equalsIgnoreCase("/" + readTicket)) {
            ReportRTS.getPlugin().getServer().dispatchCommand(event.getPlayer(), "ticket " + event.getMessage().replaceFirst("/", ""));
            event.setCancelled(true);
        }
        if(event.getMessage().split(" ")[0].equalsIgnoreCase("/" + openTicket)) {
            ReportRTS.getPlugin().getServer().dispatchCommand(event.getPlayer(), "ticket " + event.getMessage().replaceFirst("/", ""));
            event.setCancelled(true);
        }
        if(event.getMessage().split(" ")[0].equalsIgnoreCase("/" + closeTicket)) {
            ReportRTS.getPlugin().getServer().dispatchCommand(event.getPlayer(), "ticket " + event.getMessage().replaceFirst("/", ""));
            event.setCancelled(true);
        }
        if(event.getMessage().split(" ")[0].equalsIgnoreCase("/" + reopenTicket)) {
            ReportRTS.getPlugin().getServer().dispatchCommand(event.getPlayer(), "ticket " + event.getMessage().replaceFirst("/", ""));
            event.setCancelled(true);
        }
        if(event.getMessage().split(" ")[0].equalsIgnoreCase("/" + claimTicket)) {
            ReportRTS.getPlugin().getServer().dispatchCommand(event.getPlayer(), "ticket " + event.getMessage().replaceFirst("/", ""));
            event.setCancelled(true);
        }
        if(event.getMessage().split(" ")[0].equalsIgnoreCase("/" + unclaimTicket)) {
            ReportRTS.getPlugin().getServer().dispatchCommand(event.getPlayer(), "ticket " + event.getMessage().replaceFirst("/", ""));
            event.setCancelled(true);
        }
        if(event.getMessage().split(" ")[0].equalsIgnoreCase("/" + holdTicket)) {
            ReportRTS.getPlugin().getServer().dispatchCommand(event.getPlayer(), "ticket " + event.getMessage().replaceFirst("/", ""));
            event.setCancelled(true);
        }
        if(event.getMessage().split(" ")[0].equalsIgnoreCase("/" + teleportToTicket)) {
            ReportRTS.getPlugin().getServer().dispatchCommand(event.getPlayer(), "ticket " + event.getMessage().replaceFirst("/", ""));
            event.setCancelled(true);
        }
        if(event.getMessage().split(" ")[0].equalsIgnoreCase("/" + broadcastToStaff)) {
            ReportRTS.getPlugin().getServer().dispatchCommand(event.getPlayer(), "ticket " + event.getMessage().replaceFirst("/", ""));
            event.setCancelled(true);
        }
        if(event.getMessage().split(" ")[0].equalsIgnoreCase("/" + listStaff)) {
            ReportRTS.getPlugin().getServer().dispatchCommand(event.getPlayer(), "ticket " + event.getMessage().replaceFirst("/", ""));
            event.setCancelled(true);
        }
    }
}
