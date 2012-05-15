package com.nyancraft.reportrts.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.persistence.Database;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;

public class CompleteCommand implements CommandExecutor {

	private ReportRTS plugin;
	private Database dbManager;
	
	public CompleteCommand(ReportRTS plugin) {
		this.plugin = plugin;
		this.dbManager = DatabaseManager.getDatabase();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!RTSPermissions.canCompleteRequests(sender)){
			if(RTSPermissions.canCompleteOwnRequests(sender)){
				if(!RTSFunctions.isParsableToInt(args[0])) return false;
				long start = 0;
				if(plugin.debugMode) start = System.currentTimeMillis();
				if(!plugin.requestMap.containsKey(Integer.parseInt(args[0]))){
					sender.sendMessage(Message.parse("generalInternalError", "That request was not found."));
					return true;
				}
				if(!plugin.requestMap.get(Integer.parseInt(args[0])).getName().equalsIgnoreCase(sender.getName())){
					sender.sendMessage(Message.parse("generalInternalError", "You are not the owner of that ticket."));
					return true;
				}
				dbManager.deleteEntryById("reportrts_request", Integer.parseInt(args[0]));
				plugin.requestMap.remove(Integer.parseInt(args[0]));
				RTSFunctions.messageMods(Message.parse("completedReq", Integer.parseInt(args[0]),"Cancellation System"), sender.getServer().getOnlinePlayers());
				sender.sendMessage(Message.parse("completedUser", "Cancellation System"));
				
				if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
				return true;
			}
			sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.complete or reportrts.command.complete.self"));
			return true;
		}
		if(args.length == 0) return false;
		if(!RTSFunctions.isParsableToInt(args[0])) return false;
		long start = 0;
		if(plugin.debugMode) start = System.currentTimeMillis();
		String comment = RTSFunctions.implode(args, " ");
		
		if(comment.length() <= args[0].length()){
			comment = null;
		}else{
			comment = comment.substring(args[0].length()).trim();
		}
		
		int online = 0;
		boolean isClaimedByOther = false;
		
		if(plugin.requestMap.containsKey(Integer.parseInt(args[0]))){
			online = (RTSFunctions.isUserOnline(plugin.requestMap.get(Integer.parseInt(args[0])).getName(), sender.getServer())) ? 1 : 0;
			if(plugin.requestMap.get(Integer.parseInt(args[0])).getStatus() == 1){
				isClaimedByOther = (!plugin.requestMap.get(Integer.parseInt(args[0])).getModName().equalsIgnoreCase(sender.getName())) ? true : false;
			}
		}
		
		if(isClaimedByOther && !sender.hasPermission("reportrts.override")){
			sender.sendMessage(Message.parse("generalInternalError", "Request #" + args[0] + " is claimed by someone else."));
			return true;
		}
		
		if(!dbManager.setRequestStatus(Integer.parseInt(args[0]), sender.getName(), 3, comment, online)) {
			sender.sendMessage(Message.parse("generalInternalError", "Unable to mark request #" + args[0] + " as complete"));
			return true;	
		}
		
		if(plugin.requestMap.containsKey(Integer.parseInt(args[0]))) {
			Player player = sender.getServer().getPlayer(plugin.requestMap.get(Integer.parseInt(args[0])).getName());
			if(online == 0) plugin.notificationMap.put(Integer.parseInt(args[0]), plugin.requestMap.get(Integer.parseInt(args[0])).getName());
			if(player != null){
				player.sendMessage(Message.parse("completedUser", sender.getName()));
				player.sendMessage(Message.parse("completedText", plugin.requestMap.get(Integer.parseInt(args[0])).getMessage(), comment));
			}
			plugin.requestMap.remove(Integer.parseInt(args[0]));
		}
			
		RTSFunctions.messageMods(Message.parse("completedReq", args[0], sender.getName()), sender.getServer().getOnlinePlayers());
		if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
		return true;
	}

}
