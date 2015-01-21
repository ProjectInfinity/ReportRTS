package com.nyancraft.reportrts.event;

import com.nyancraft.reportrts.data.Ticket;
import org.bukkit.command.CommandSender;

/**
 * Event that is called when a Ticket has been set to
 * be on hold, /hold. This includes
 * the player that put the ticket on hold, and the reason.
 */
public class TicketHoldEvent extends TicketEvent {

	private String reason;
	private CommandSender sender;
	
	public TicketHoldEvent(Ticket ticket, String reason, CommandSender sender) {
		super(ticket);
		this.reason = reason;
		this.sender = sender;
	}
	
	/**
	 * Get the reason a ticket was put on hold.
	 * @return String reason
	 */
	public String getHoldReason(){
		return reason;
	}
	
	/**
	 * Get the player that set the Ticket on hold.
	 * @return CommandSender object of the player that
	 * put the ticket on hold.
	 */
	public CommandSender getHoldPlayer(){
		return sender;
	}

}
