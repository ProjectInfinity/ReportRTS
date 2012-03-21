package com.nyancraft.reportrts.command;

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
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;

public class ModreqCommand implements CommandExecutor {

	private ReportRTS plugin;
	
	public ModreqCommand(ReportRTS plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("[ReportRTS] You cannot file requests through the console.");
			return true;
		}
		if(!RTSPermissions.canFileRequest(sender)) return true;
		if(args.length == 0) return false;
		if(RTSFunctions.getOpenRequestsByUser(sender) >= plugin.maxRequests && !(ReportRTS.permission != null ? ReportRTS.permission.has(sender, "reportrts.command.modreq.unlimited") : sender.hasPermission("reportrts.command.modreq.unlimited"))) {
			sender.sendMessage(Message.parse("modreqTooManyOpen"));
			return true;
		}
		if(ReportRTS.getPlugin().requestDelay > 0){
			if(!(ReportRTS.permission != null ? ReportRTS.permission.has(sender, "reportrts.command.modreq.unlimited") : sender.hasPermission("reportrts.command.modreq.unlimited"))){
				long timeBetweenRequest = RTSFunctions.checkTimeBetweenRequests(sender);
				if(timeBetweenRequest > 0){
					sender.sendMessage(Message.parse("modreqTooFast", timeBetweenRequest));
					return true;
				}
			}
		}
		
		Player player = (Player)sender;
		String message = RTSFunctions.implode(args, " ");
		int userId = DatabaseManager.getDatabase().getUserId(player.getName());
		if(!DatabaseManager.getDatabase().fileRequest(player.getName(), player.getWorld().getName(), player.getLocation(), message, userId)) {	
			sender.sendMessage(Message.parse("generalInternalError", "Request could not be filed."));
			return true;
		}
		int ticketId = DatabaseManager.getDatabase().getLatestTicketIdByUser(userId);
		
		Location location = player.getLocation();
		
		sender.sendMessage(Message.parse("modreqFiledUser"));
		plugin.getLogger().log(Level.INFO, "" + player.getName() + " filed a request.");
		if(plugin.notifyStaffOnNewRequest) RTSFunctions.messageMods(Message.parse("modreqFiledMod", player.getName(), ticketId), sender.getServer().getOnlinePlayers());
		
		plugin.requestMap.put(ticketId, new HelpRequest(player.getName(), ticketId, System.currentTimeMillis()/1000, message, 0, location.getBlockX(), location.getBlockY(), location.getBlockZ(), player.getWorld().getName()));
		
		return true;
	}
}
