package com.nyancraft.reportrts.event;

import com.nyancraft.reportrts.data.Ticket;
import org.bukkit.command.CommandSender;

/**
 * Event that is called when a Report has been set to
 * be on hold, /hold. This includes
 * the player that put the request on hold, and the reason.
 */
public class TicketHoldEvent extends TicketEvent {

	private String reason;
	private CommandSender sender;
	
	public TicketHoldEvent(Ticket request, String reason, CommandSender sender) {
		super(request);
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
	 * Get the player that set the Report on hold.
	 * @return CommandSender object of the player that
	 * put the request on hold.
	 */
	public CommandSender getHoldPlayer(){
		return sender;
	}

}
