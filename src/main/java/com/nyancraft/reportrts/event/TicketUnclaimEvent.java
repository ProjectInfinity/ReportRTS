package com.nyancraft.reportrts.event;

import com.nyancraft.reportrts.data.Ticket;
import org.bukkit.command.CommandSender;

/**
 * Event called when a ticket is unclaimed, with the
 * staff who last owned the ticket and the person who unclaimed
 * the ticket.
 */
public class TicketUnclaimEvent extends TicketEvent {

	private String staffName;
	private CommandSender sender;
	
	public TicketUnclaimEvent(Ticket ticket, String staffName, CommandSender sender) {
            super(ticket);
            this.sender = sender;
            this.staffName = staffName;
	}
	
	/**
	 * Get the staff member that owned the ticket before
	 * it was unclaimed.
	 * 
	 * @return String the staff's name.
	 */
	public String getLastStaffName(){
		return staffName;
	}
	
	/**
	 * Get the user that unclaimed the ticket.
	 * @return CommandSender object of the user.
	 */
	public CommandSender getSender(){
		return sender;
	}
}
