package com.nyancraft.reportrts;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.nyancraft.reportrts.RTSPermissions;

public class RTSListener implements Listener{
	private final ReportRTS plugin;
	private int openRequests;
	
	public RTSListener(ReportRTS plugin){
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event){
		if(!RTSPermissions.isModerator(event.getPlayer())) return;
		
		openRequests = plugin.requestMap.size();
		
		if(openRequests < 1 && !plugin.hideNotification)
			event.getPlayer().sendMessage("There are no requests at this time.");
		
		if(openRequests > 0)
			event.getPlayer().sendMessage(ChatColor.GREEN + "There are " + openRequests + " open requests, type /check to see them.");
	}
}
