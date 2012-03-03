package com.nyancraft.reportrts;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RTSPermissions {
	
	public static boolean isModerator(Player player){
		return player.hasPermission("reportrts.mod");
	}
	
	public static boolean canFileRequest(CommandSender sender){
		if(!sender.hasPermission("reportrts.command.modreq")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.ban");
			return false;
		}
		return true;
	}
	
	public static boolean canCheckAllRequests(CommandSender sender){
		return sender.hasPermission("reportrts.command.check");
	}
	
	public static boolean canCompleteRequests(CommandSender sender){
		if(!sender.hasPermission("reportrts.command.complete")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.complete");
			return false;
		}
		return true;
	}
	
	public static boolean canTeleport(CommandSender sender){
		if(!sender.hasPermission("reportrts.command.teleport")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.teleport");
			return false;
		}
		return true;
	}
	
	public static boolean canReloadPlugin(CommandSender sender){
		if(!sender.hasPermission("reportrts.command.reload")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.reload");
			return false;
		}
		return true;
	}
	
	public static boolean canBanUser(CommandSender sender){
		if(!sender.hasPermission("reportrts.command.ban")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.ban");
			return false;
		}
		return true;
	}
	
	public static boolean canResetPlugin(CommandSender sender){
		if(!sender.hasPermission("reportrts.command.reset")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.reset");
			return false;
		}
		return true;
	}
	
	public static boolean canPutTicketOnHold(CommandSender sender){
		if(!sender.hasPermission("reportrts.command.hold")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.hold");
			return false;
		}
		return true;
	}
	
	public static boolean canClaimTicket(CommandSender sender){
		if(!sender.hasPermission("reportrts.command.claim")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.claim");
			return false;
		}
		return true;
	}
	
	public static boolean canListStaff(CommandSender sender){
		if(!sender.hasPermission("reportrts.command.modlist")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.modlist");
			return false;
		}
		return true;
	}
	
	public static boolean canBroadcast(CommandSender sender){
		if(!sender.hasPermission("reportrts.command.broadcast")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.broadcast");
			return false;
		}
		return true;
	}
}
