package com.nyancraft.reportrts.event;

import com.nyancraft.reportrts.data.Ticket;
import org.bukkit.command.CommandSender;

/**
 * Event called when a request is unclaimed, with the
 * mod who last owned the request and the person who unclaimed
 * the request.
 *
 */
public class TicketUnclaimEvent extends TicketEvent {

	private String modName;
	private CommandSender sender;
	
	public TicketUnclaimEvent(Ticket request, String modName, CommandSender sender) {
            super(request);
            this.sender = sender;
            this.modName = modName;
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
