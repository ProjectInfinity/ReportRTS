package com.nyancraft.reportrts.event;

import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.data.Ticket;

/**
 * Event that is called when a moderator
 * assigns another moderator a request, /assign.
 *
 */
public class TicketReopenEvent extends TicketEvent {

	private final CommandSender sender;
	
	public TicketReopenEvent(Ticket request, CommandSender sender) {
		super(request);
		this.sender = sender;
	}
	
	/**
	 * The user that assigned the request to the other user.
	 * Get the assignee with Ticket's .getModUUID()
	 * @return CommandSender object of the user that assigned the request.
	 */
	public CommandSender getAssigner(){
		return sender;
	}

}
