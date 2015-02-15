package com.nyancraft.reportrts.event;

import com.nyancraft.reportrts.data.Ticket;
import org.bukkit.command.CommandSender;

/**
 * Event that is called when a Ticket is marked
 * as closed by staff.
 * 
 * The completer is the user that sends the command.
 *
 */
public class TicketCloseEvent extends TicketEvent {

	private CommandSender sender;
	
	public TicketCloseEvent(Ticket ticket, CommandSender sender) {
            super(ticket);
            this.sender = sender;
	}
	
	/**
	 * This will get the user that
	 * used the close command.
	 * 
	 * You might want to use the Ticket's getStaffName
     * to get the staff who handled the ticket.
	 * 
	 * @return The user who closed the ticket.
	 */
	public CommandSender getSender(){
		return sender;
	}
        
}
