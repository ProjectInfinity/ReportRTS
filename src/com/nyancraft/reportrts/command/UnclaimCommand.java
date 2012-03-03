package com.nyancraft.reportrts.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.RTSDatabaseManager;
import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;

public class UnclaimCommand implements CommandExecutor{

	private ReportRTS plugin;
	public UnclaimCommand(ReportRTS plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 0) return false;
		if(!RTSPermissions.canClaimTicket(sender)) return true;
		if(!RTSFunctions.isParsableToInt(args[0])) return false;
		if(!plugin.requestMap.containsKey(Integer.parseInt(args[0]))){
			sender.sendMessage(ChatColor.RED + "[ReportRTS] You may only unclaim open requests.");
			return true;
		}
		if(plugin.requestMap.get(Integer.parseInt(args[0])).getStatus() != 1){
			sender.sendMessage(ChatColor.RED + "[ReportRTS] You may only unclaim requests that are claimed.");
			return true;
		}
		if(!RTSDatabaseManager.setRequestStatus(Integer.parseInt(args[0]), sender.getName(), 0)){
			sender.sendMessage(ChatColor.RED + "[ReportRTS] Unable to unclaim request #" + args[0] + ".");
			return true;
		}
		plugin.requestMap.get(Integer.parseInt(args[0])).setStatus(0);
		RTSFunctions.messageMods(ChatColor.GOLD + "[ReportRTS] " + plugin.requestMap.get(Integer.parseInt(args[0])).getModName() + " is no longer handling request #" + args[0] + ".", sender.getServer().getOnlinePlayers());
		plugin.requestMap.get(Integer.parseInt(args[0])).setModName(null);
		sender.sendMessage(ChatColor.GOLD + "[ReportRTS] Request #" + args[0] + " has been unclaimed.");
		
		return true;
	}
}
