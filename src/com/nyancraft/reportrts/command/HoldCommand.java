package com.nyancraft.reportrts.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;

public class HoldCommand implements CommandExecutor {

	private ReportRTS plugin;
	
	public HoldCommand(ReportRTS plugin) {
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!RTSPermissions.canPutTicketOnHold(sender)) return true;
		if(args.length == 0) return false;
		if(!RTSFunctions.isParsableToInt(args[0])) return false;
		String reason = RTSFunctions.implode(args, " ");
		
		if(reason.length() <= args[0].length()){
			reason = "None specified.";
		}else{
			reason = reason.substring(args[0].length());
		}
		if(!DatabaseManager.getDatabase().setRequestStatus(Integer.parseInt(args[0]), sender.getName(), 2, reason)) {
			sender.sendMessage(Message.parse("generalInternalError", "Unable to put request #" + args[0] + " on hold."));
			return true;	
		}
		if(plugin.requestMap.containsKey(Integer.parseInt(args[0]))){
			Player player = sender.getServer().getPlayer(plugin.requestMap.get(Integer.parseInt(args[0])).getName());
			if(player != null){
				player.sendMessage(Message.parse("holdUser", sender.getName()));
				player.sendMessage(Message.parse("holdText", plugin.requestMap.get(Integer.parseInt(args[0])).getMessage(), reason.trim()));
			}
			plugin.requestMap.remove(Integer.parseInt(args[0]));
		}
			
		RTSFunctions.messageMods(Message.parse("holdRequest", args[0], sender.getName()), sender.getServer().getOnlinePlayers());
		return true;
	}

}
