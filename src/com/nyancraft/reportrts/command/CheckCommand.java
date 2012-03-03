package com.nyancraft.reportrts.command;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.RTSDatabaseManager;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.HelpRequest;
import com.nyancraft.reportrts.RTSFunctions;

public class CheckCommand implements CommandExecutor {

	private ReportRTS plugin;
	private String substring = null;
	
	public CheckCommand(ReportRTS plugin){
		this.plugin = plugin;
	}
	private List<Map.Entry<Integer, HelpRequest>> requestList = new ArrayList<Map.Entry<Integer, HelpRequest>>(); 
	private SimpleDateFormat sdf  = new SimpleDateFormat("MMM.dd kk:mm z");
	private String date = null;
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!RTSPermissions.canCheckAllRequests(sender)){
			sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] You need permission to do that: reportrts.command.check");
			return true;
		}
		if(args.length == 0){
			int i = 0;
			sender.sendMessage(ChatColor.AQUA + "--------- " + plugin.requestMap.size() + " Requests -" + ChatColor.YELLOW + " Open " + ChatColor.AQUA + "---------");
			if(plugin.requestMap.size() == 0) sender.sendMessage(ChatColor.GOLD + "There are no requests right now.");
			for(Map.Entry<Integer, HelpRequest> entry : plugin.requestMap.entrySet()){
				i++;
				if(i > 5) break;
	
				HelpRequest currentRequest = entry.getValue();
				 substring = currentRequest.getMessage();
		            if (substring.length() >= 20) {
		                substring = substring.substring(0, 20) + "...";
		            }
		            date = sdf.format(new java.util.Date(currentRequest.getTimestamp() * 1000));
		            ChatColor online = (RTSFunctions.isUserOnline(currentRequest.getName(), sender.getServer())) ? ChatColor.GREEN : ChatColor.RED;
		            substring = (currentRequest.getStatus() == 1) ? ChatColor.LIGHT_PURPLE + "Claimed by " + currentRequest.getModName() : ChatColor.GRAY + substring;
		            sender.sendMessage(ChatColor.GOLD + "#" + currentRequest.getId() + " " + date + " by " + online + currentRequest.getName() + ChatColor.GOLD + " - " + substring);
			}
		return true;
	}
		if(!RTSFunctions.isParsableToInt(args[0])){
			try{
				switch(SubCommands.valueOf(args[0].toString().toUpperCase())){
				
				case PAGE:		
					try{
						checkPage(args[1], sender);
					}catch(ArrayIndexOutOfBoundsException e){
						checkPage("1", sender);
					}
					break;
					
				case P:		
					try{
						checkPage(args[1], sender);
					}catch(ArrayIndexOutOfBoundsException e){
						checkPage("1", sender);
					}
					break;
				
				case HELD:
					try{
						checkHeld(args[1], sender);
					}catch(ArrayIndexOutOfBoundsException e){
						checkHeld("1", sender);
					}
					break;
						
				case H:
					try{
						checkHeld(args[1], sender);
					}catch(ArrayIndexOutOfBoundsException e){
						checkHeld("1", sender);
					}
					break;
				}

				return true;
			} catch(IllegalArgumentException e){
				return false;
			}
		}
		
		checkId(Integer.parseInt(args[0]), sender);
		
		return true;
	}
	private enum SubCommands{
		PAGE,
		P,
		HELD,
		H
	}
	
	private void checkPage(String page, CommandSender sender){
		requestList.clear();
		requestList.addAll(plugin.requestMap.entrySet());
		
		int pageNumber = Integer.parseInt(page);
		sender.sendMessage(ChatColor.AQUA + "--------- " + requestList.size() + " Requests -" + ChatColor.YELLOW + " Open " + ChatColor.AQUA + "---------");
		if(plugin.requestMap.size() == 0) sender.sendMessage(ChatColor.GOLD + "There are no requests right now.");
		for(int i = (pageNumber * 5) - 5; i < (pageNumber * 5) && i < requestList.size(); i++){
			HelpRequest currentRequest = requestList.get(i).getValue();
			 substring = currentRequest.getMessage();

            if (substring.length() >= 20) {
                substring = substring.substring(0, 20) + "...";
            }
            date = sdf.format(new java.util.Date(currentRequest.getTimestamp() * 1000));
            ChatColor online = (RTSFunctions.isUserOnline(currentRequest.getName(), sender.getServer())) ? ChatColor.GREEN : ChatColor.RED;
            substring = (currentRequest.getStatus() == 1) ? ChatColor.LIGHT_PURPLE + "Claimed by " + currentRequest.getModName() : ChatColor.GRAY + substring;
            sender.sendMessage(ChatColor.GOLD + "#" + currentRequest.getId() + " " + date + " by " + online + currentRequest.getName() + ChatColor.GOLD + " - " + substring);
		}
	}
	
	private void checkHeld(String page, CommandSender sender){
		int pageNumber = Integer.parseInt(page);
		int i = (pageNumber * 5) - 5;

		ResultSet result = RTSDatabaseManager.db.query("SELECT * FROM reportrts_request as request INNER JOIN reportrts_user as user ON request.user_id = user.id WHERE request.status = '2' AND request.id > '" + i + "' LIMIT 5");
		try {
			int heldRequests = RTSDatabaseManager.getHeldRequests();
			sender.sendMessage(ChatColor.AQUA + "--------- " + heldRequests + " Requests -" + ChatColor.YELLOW + " Held " + ChatColor.AQUA + "---------");
			if(heldRequests == 0) sender.sendMessage(ChatColor.GOLD + "There are no held requests right now.");
			while(result.next()){
				 substring = result.getString("text");

	            if (substring.length() >= 20) {
	                substring = substring.substring(0, 20) + "...";
	            }
	            date = sdf.format(new java.util.Date(result.getLong("tstamp") * 1000));
	            ChatColor online = (RTSFunctions.isUserOnline(result.getString("name"), sender.getServer())) ? ChatColor.GREEN : ChatColor.RED;
	            sender.sendMessage(ChatColor.GOLD + "#" + result.getInt(1) + " " + date + " by " + online + result.getString("name") + ChatColor.GOLD + " - " + ChatColor.GRAY + substring);	
			}
			result.close();
		} catch (SQLException e) {
			sender.sendMessage(ChatColor.RED + "[ReportRTS] Unable to check held requests. Check the console for errors.");
			e.printStackTrace();
			return;
		}
	}
	
	private void checkId(int id, CommandSender sender){
		HelpRequest currentRequest = plugin.requestMap.get(id);
		
		if(currentRequest == null) {
			ResultSet result = RTSDatabaseManager.db.query("SELECT * FROM reportrts_request as request INNER JOIN reportrts_user as user ON request.user_id = user.id WHERE request.id = '" + id + "'");

			ChatColor online;
			try {
				online = (RTSFunctions.isUserOnline(result.getString("name"), sender.getServer())) ? ChatColor.GREEN : ChatColor.RED;
				date = sdf.format(new java.util.Date(result.getLong("tstamp") * 1000));
				String status = null;
				ChatColor statusColor = null;
				
				if(result.getInt("status") == 0){
					status = "Open";
					statusColor = ChatColor.YELLOW;
				}
				if(result.getInt("status") == 1){
					status = "Claimed";
					statusColor = ChatColor.RED;
				}
				if(result.getInt("status") == 2){
					status = "On Hold";
					statusColor = ChatColor.LIGHT_PURPLE;
				}
				if(result.getInt("status") == 3){
					status = "Closed";
					statusColor = ChatColor.GREEN;
				}
				
				sender.sendMessage(ChatColor.AQUA + "--------- " + "Request #" + result.getInt(1) + " - " + statusColor + status + ChatColor.AQUA + " ---------");
				sender.sendMessage(ChatColor.YELLOW + "Filed by" + online + " " + result.getString("name") + ChatColor.YELLOW + " at " +  ChatColor.GREEN + date + ChatColor.YELLOW + " at " + ChatColor.GREEN + result.getInt("x") + ", " + result.getInt("y") + ", " + result.getInt("z"));
				sender.sendMessage(ChatColor.GRAY + result.getString("text"));
				
				result.close();
				return;
			} catch (SQLException e) {
				sender.sendMessage(ChatColor.RED + "[ReportRTS]" + " Request #" + id + " was not found.");
				return;
			}
			
		}
		
		ChatColor online = (RTSFunctions.isUserOnline(currentRequest.getName(), sender.getServer())) ? ChatColor.GREEN : ChatColor.RED;
		date = sdf.format(new java.util.Date(currentRequest.getTimestamp() * 1000));
		String status;
		if (currentRequest.getStatus() == 1){
			status = ChatColor.RED + "Claimed";
		}else{
			status = ChatColor.YELLOW + "Open";
		}
		
		sender.sendMessage(ChatColor.AQUA + "--------- " + " Request #" + currentRequest.getId() + " -" + ChatColor.YELLOW + " " + status + " " + ChatColor.AQUA + "---------");
		sender.sendMessage(ChatColor.YELLOW + "Filed by" + online + " " + currentRequest.getName() + ChatColor.YELLOW + " at " +  ChatColor.GREEN + date + ChatColor.YELLOW + " at " + ChatColor.GREEN + currentRequest.getX() + ", " + currentRequest.getY() + ", " + currentRequest.getZ());
		sender.sendMessage(ChatColor.GRAY + currentRequest.getMessage());
		
	}
}