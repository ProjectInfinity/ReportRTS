package com.nyancraft.reportrts.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.RTSPermissions;

public class ModlistCommand implements CommandExecutor{

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!RTSPermissions.canListStaff(sender)) return true;
		
		Player[] players = sender.getServer().getOnlinePlayers();
		String staff = "";
		
		for(Player player : players){
			if(RTSPermissions.isModerator(player)) staff = staff + player.getName() + ", ";
		}
		if(staff.length() == 0){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] There are no staff members online.");
			return true;
		}
		staff = staff.substring(0, staff.length() - 2);
		
		sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] " + ChatColor.AQUA + "Staff online: " + ChatColor.YELLOW + staff);
		return true;
	}
}
