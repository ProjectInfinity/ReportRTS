package com.nyancraft.reportrts;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.data.HelpRequest;
import com.nyancraft.reportrts.persistence.DatabaseManager;
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
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onSignChange(SignChangeEvent event){
	
		Block block = event.getBlock();
		Sign sign = null;
	    if (block.getState() instanceof Sign)
	      sign = (Sign)block.getState();
	    else
	      return;
	    if(!event.getLine(0).equalsIgnoreCase("[help]")) return;
		
	    if(!RTSPermissions.canFileRequest(event.getPlayer())) {
	    	block.breakNaturally();
	    	return;
	    }
	    String[] text = new String[3]; System.arraycopy(event.getLines(), 1, text, 0, 3);
	    String message = RTSFunctions.cleanUpSign(text);
	    if(message.length() == 0) {
	    	block.breakNaturally();
	    	return;
	    }
	    if(RTSFunctions.getOpenRequestsByUser(event.getPlayer()) >= plugin.maxRequests && !(ReportRTS.permission != null ? ReportRTS.permission.has(event.getPlayer(), "reportrts.command.modreq.unlimited") : event.getPlayer().hasPermission("reportrts.command.modreq.unlimited"))){
	    	event.getPlayer().sendMessage(Message.parse("modreqTooManyOpen"));
	    	block.breakNaturally();
	    	return;
	    }
	    int userId = DatabaseManager.getDatabase().getUserId(event.getPlayer().getName());
	    if(DatabaseManager.getDatabase().fileRequest(event.getPlayer().getName(), event.getPlayer().getWorld().getName(), event.getPlayer().getLocation(), message, userId)){
	    	int ticketId = DatabaseManager.getDatabase().getLatestTicketIdByUser(userId);
	    	plugin.requestMap.put(ticketId, new HelpRequest(event.getPlayer().getName(), ticketId, System.currentTimeMillis()/1000, message, 0, event.getPlayer().getLocation().getBlockX(), event.getPlayer().getLocation().getBlockY(), event.getPlayer().getLocation().getBlockZ(), event.getPlayer().getWorld().getName()));
	    	event.getPlayer().sendMessage(Message.parse("modreqFiledUser"));
	    	RTSFunctions.messageMods(Message.parse("modreqFiledMod", event.getPlayer().getName()), event.getPlayer().getServer().getOnlinePlayers());
	    }
	}
}
