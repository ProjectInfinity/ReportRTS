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

public class CompleteCommand implements CommandExecutor {

	private ReportRTS plugin;
	
	public CompleteCommand(ReportRTS plugin) {
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!RTSPermissions.canCompleteRequests(sender)) return true;
		if(args.length == 0) return false;
		if(!RTSFunctions.isParsableToInt(args[0])) return false;
		
		if(!DatabaseManager.getDatabase().setRequestStatus(Integer.parseInt(args[0]), sender.getName(), 3)) {
			sender.sendMessage(Message.parse("generalInternalError", "Unable to mark request #" + args[0] + " as complete"));
			return true;	
		}
		if(plugin.requestMap.containsKey(Integer.parseInt(args[0]))) {
			Player player = sender.getServer().getPlayer(plugin.requestMap.get(Integer.parseInt(args[0])).getName());
			if(player != null){
				player.sendMessage(Message.parse("completedUser", sender.getName()));
				player.sendMessage(Message.parse("completedText", plugin.requestMap.get(Integer.parseInt(args[0])).getMessage()));
			}
			plugin.requestMap.remove(Integer.parseInt(args[0]));
		}
			
		RTSFunctions.messageMods(Message.parse("completedReq", args[0], sender.getName()), sender.getServer().getOnlinePlayers());
		return true;
	}

}
