package com.nyancraft.reportrts.command;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.RTSDatabaseManager;
import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.util.Message;

public class ReportRTSCommand implements CommandExecutor{

	private ReportRTS plugin;
	
	public ReportRTSCommand(ReportRTS plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 0) return false;
		try{
			switch(SubCommands.valueOf(args[0].toString().toUpperCase())){
			
			case RELOAD:
				if(!RTSPermissions.canReloadPlugin(sender)) return true;
				plugin.reloadPlugin();
				sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] Reloaded configuration and requests.");
				break;
				
			case BAN:
				if(!RTSPermissions.canBanUser(sender)) return true;
				if(!RTSDatabaseManager.setUserStatus(args[1], 1)){
					sender.sendMessage(Message.parse("generalInternalError", "Cannot ban " + args[1] + " from filing requests."));
					return true;
				}
				RTSFunctions.messageMods(Message.parse("banUser", sender.getName(), args[1]), sender.getServer().getOnlinePlayers());
				break;
			
			case UNBAN:
				if(!RTSPermissions.canBanUser(sender)) return true;
				if(!RTSDatabaseManager.setUserStatus(args[1], 0)){
					sender.sendMessage(Message.parse("generalInternalError", "Cannot unban " + args[1] + " from filing requests."));
					return true;
				}
				RTSFunctions.messageMods(Message.parse("unbanUser", sender.getName(), args[1]), sender.getServer().getOnlinePlayers());
				break;
				
			case RESET:
				if(!RTSPermissions.canResetPlugin(sender)) return true;
				if(!RTSDatabaseManager.resetDB()){
					sender.sendMessage(ChatColor.RED + "[ReportRTS] An unexpected error occured when attempting to reset the plugin.");
					return true;
				}
				plugin.reloadPlugin();
				sender.sendMessage(ChatColor.GOLD + "[ReportRTS] You deleted all users and requests from ReportRTS.");
				plugin.getLogger().log(Level.INFO, sender.getName() + " deleted all users and requests from ReportRTS!");
				break;
				
			}
		}catch(Exception e){
			return false;
		}

		return true;
	}
	
	private enum SubCommands{
		RELOAD,
		BAN,
		UNBAN,
		RESET
	}
}
