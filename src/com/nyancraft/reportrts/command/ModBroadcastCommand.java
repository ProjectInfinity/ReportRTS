package com.nyancraft.reportrts.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;

public class ModBroadcastCommand implements CommandExecutor{

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(!RTSPermissions.canBroadcast(sender)) return true;
		if(args.length == 0) return false;
		String message = RTSFunctions.implode(args, " ");
		RTSFunctions.messageMods(ChatColor.RED + "[Mod-Broadcast] " + sender.getName() + ": " + ChatColor.GREEN + message, sender.getServer().getOnlinePlayers());
		
		return true;
	}
}
