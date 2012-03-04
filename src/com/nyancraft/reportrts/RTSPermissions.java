package com.nyancraft.reportrts;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.ReportRTS;

public class RTSPermissions {
	
	public static boolean isModerator(Player player){
		if(ReportRTS.permission != null) return ReportRTS.permission.playerHas(player, "reportrts.mod");
		return player.hasPermission("reportrts.mod");
	}
	
	public static boolean canFileRequest(CommandSender sender){
		if(ReportRTS.permission != null){
			if(!ReportRTS.permission.has(sender, "reportrts.command.modreq")){
				sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.modreq");
				return false;
			}
			return true;
		}
		if(!sender.hasPermission("reportrts.command.modreq")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.modreq");
			return false;
		}
		return true;
	}
	
	public static boolean canCheckAllRequests(CommandSender sender){
		if(ReportRTS.permission != null) return ReportRTS.permission.has(sender, "reportrts.command.check");
		return sender.hasPermission("reportrts.command.check");
	}
	
	public static boolean canCheckOwnRequests(CommandSender sender){
		if(ReportRTS.permission != null) return ReportRTS.permission.has(sender, "reportrts.command.check.self");
		return sender.hasPermission("reportrts.command.check.self");
	}
	
	public static boolean canCompleteRequests(CommandSender sender){
		if(ReportRTS.permission != null){
			if(!ReportRTS.permission.has(sender, "reportrts.command.complete")){
				sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.complete");
				return false;
			}
			return true;
		}
		if(!sender.hasPermission("reportrts.command.complete")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.complete");
			return false;
		}
		return true;
	}
	
	public static boolean canTeleport(CommandSender sender){
		if(ReportRTS.permission != null){
			if(!ReportRTS.permission.has(sender, "reportrts.command.teleport")){
				sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.teleport");
				return false;
			}
			return true;
		}
		if(!sender.hasPermission("reportrts.command.teleport")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.teleport");
			return false;
		}
		return true;
	}
	
	public static boolean canReloadPlugin(CommandSender sender){
		if(ReportRTS.permission != null){
			if(!ReportRTS.permission.has(sender, "reportrts.command.reload")){
				sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.reload");
				return false;
			}
			return true;
		}
		if(!sender.hasPermission("reportrts.command.reload")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.reload");
			return false;
		}
		return true;
	}
	
	public static boolean canBanUser(CommandSender sender){
		if(ReportRTS.permission != null){
			if(!ReportRTS.permission.has(sender, "reportrts.command.ban")){
				sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.ban");
				return false;
			}
			return true;
		}
		if(!sender.hasPermission("reportrts.command.ban")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.ban");
			return false;
		}
		return true;
	}
	
	public static boolean canResetPlugin(CommandSender sender){
		if(ReportRTS.permission != null){
			if(!ReportRTS.permission.has(sender, "reportrts.command.reset")){
				sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.reset");
				return false;
			}
			return true;
		}
		if(!sender.hasPermission("reportrts.command.reset")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.reset");
			return false;
		}
		return true;
	}
	
	public static boolean canPutTicketOnHold(CommandSender sender){
		if(ReportRTS.permission != null){
			if(!ReportRTS.permission.has(sender, "reportrts.command.hold")){
				sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.hold");
				return false;
			}
			return true;
		}
		if(!sender.hasPermission("reportrts.command.hold")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.hold");
			return false;
		}
		return true;
	}
	
	public static boolean canClaimTicket(CommandSender sender){
		if(ReportRTS.permission != null){
			if(!ReportRTS.permission.has(sender, "reportrts.command.claim")){
				sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.claim");
				return false;
			}
			return true;
		}
		if(!sender.hasPermission("reportrts.command.claim")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.claim");
			return false;
		}
		return true;
	}
	
	public static boolean canListStaff(CommandSender sender){
		if(ReportRTS.permission != null){
			if(!ReportRTS.permission.has(sender, "reportrts.command.modlist")){
				sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.modlist");
				return false;
			}
			return true;
		}
		if(!sender.hasPermission("reportrts.command.modlist")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.modlist");
			return false;
		}
		return true;
	}
	
	public static boolean canBroadcast(CommandSender sender){
		if(ReportRTS.permission != null){
			if(!ReportRTS.permission.has(sender, "reportrts.command.broadcast")){
				sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.broadcast");
				return false;
			}
			return true;
		}
		if(!sender.hasPermission("reportrts.command.broadcast")){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.broadcast");
			return false;
		}
		return true;
	}
}
