package com.nyancraft.reportrts.event;

import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.data.Ticket;

/**
 * Event that is called when staff
 * assigns another staff a ticket, /assign.
 *
 */
public class TicketReopenEvent extends TicketEvent {

	private final CommandSender sender;
	
	public TicketReopenEvent(Ticket ticket, CommandSender sender) {
		super(ticket);
		this.sender = sender;
	}
	
	/**
	 * The user that assigned the ticket to the other user.
	 * Get the assignee with Ticket's .getStaffUUID()
	 * @return CommandSender object of the user that assigned the request.
	 */
	public CommandSender getAssigner(){
		return sender;
	}

}
