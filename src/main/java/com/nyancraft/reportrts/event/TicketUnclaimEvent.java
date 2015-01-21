package com.nyancraft.reportrts.event;

import com.nyancraft.reportrts.data.Ticket;
import org.bukkit.command.CommandSender;

/**
 * Event called when a ticket is unclaimed, with the
 * staff who last owned the ticket and the person who unclaimed
 * the ticket.
 */
public class TicketUnclaimEvent extends TicketEvent {

	private String modName;
	private CommandSender sender;
	
	public TicketUnclaimEvent(Ticket ticket, String modName, CommandSender sender) {
            super(ticket);
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
