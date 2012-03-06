package com.nyancraft.reportrts.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.RTSDatabaseManager;
import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.util.Message;

public class CompleteCommand implements CommandExecutor {

	private ReportRTS plugin;
	
	public CompleteCommand(ReportRTS plugin) {
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!RTSPermissions.canCompleteRequests(sender)) return true;
		if(args.length == 0) return false;
		if(!RTSFunctions.isParsableToInt(args[0])) return false;
		
		if(!RTSDatabaseManager.setRequestStatus(Integer.parseInt(args[0]), sender.getName(), 3)) {
			sender.sendMessage(Message.parse("generalInternalError", "Unable to mark request #" + args[0] + "as complete"));
			return true;	
		}
		if(plugin.requestMap.containsKey(Integer.parseInt(args[0]))) plugin.requestMap.remove(Integer.parseInt(args[0]));
			
		RTSFunctions.messageMods(Message.parse("completedReq", args[0], sender.getName()), sender.getServer().getOnlinePlayers());
		return true;
	}

}
