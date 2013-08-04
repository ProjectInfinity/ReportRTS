package com.nyancraft.reportrts.events;

import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.data.HelpRequest;

/**
 * Event called when a request is unclaimed, with the
 * mod who last owned the request and the person who unclaimed
 * the request.
 *
 */
public class ReportUnclaimEvent extends ReportEvent {

	private String modName;
	private CommandSender sender;
	
	public ReportUnclaimEvent(HelpRequest request, String modName, CommandSender sender) {
		super(request);
	}
	
	/**
	 * Get the mod that owned the request before
	 * it was unclaimed.
	 * 
	 * @return String the mod's name.
	 */
	public String getLastModName(){
		return modName;
	}
	
	/**
	 * Get the user that set this request
	 * to be unclaimed.
	 * @return CommandSender object of the user.
	 */
	public CommandSender getUnclaimer(){
		return sender;
	}

}
