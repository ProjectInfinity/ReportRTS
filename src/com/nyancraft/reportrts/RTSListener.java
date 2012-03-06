package com.nyancraft.reportrts;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.util.Message;

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
			event.getPlayer().sendMessage(Message.parse("generalNoRequests"));
		
		if(openRequests > 0)
			event.getPlayer().sendMessage(Message.parse("generalOpenRequests", openRequests));
	}
}
