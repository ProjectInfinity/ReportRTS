package com.nyancraft.reportrts.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.RTSDatabaseManager;
import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.util.Message;

public class ClaimCommand implements CommandExecutor{

	private ReportRTS plugin;
	
	public ClaimCommand(ReportRTS plugin) {
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 0) return false;
		if(!RTSPermissions.canClaimTicket(sender)) return true;
		if(!RTSFunctions.isParsableToInt(args[0])) return false;
		if(!plugin.requestMap.containsKey(Integer.parseInt(args[0]))){
			sender.sendMessage(Message.parse("claimNotOpen"));
			return true;
		}
		if(!RTSDatabaseManager.setRequestStatus(Integer.parseInt(args[0]), sender.getName(), 1)){
			sender.sendMessage(Message.parse("generalInternalError", "Unable to claim request #" + args[0]));
			return true;
		}
		plugin.requestMap.get(Integer.parseInt(args[0])).setStatus(1);
		plugin.requestMap.get(Integer.parseInt(args[0])).setModName(sender.getName());
		RTSFunctions.messageMods(Message.parse("claimRequest", sender.getName(), args[0]), sender.getServer().getOnlinePlayers());
		return true;
	}

}
