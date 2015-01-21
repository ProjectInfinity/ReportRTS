package com.nyancraft.reportrts.event;

import com.nyancraft.reportrts.data.Ticket;
import org.bukkit.command.CommandSender;

/**
 * Event that is called when a Ticket is marked
 * as completed by staff.
 * 
 * The completer is the user that sends the command.
 *
 */
public class TicketCompleteEvent extends TicketEvent {

	private CommandSender sender;
	
	public TicketCompleteEvent(Ticket ticket, CommandSender sender) {
            super(ticket);
            this.sender = sender;
	}
	
	/**
	 * This will get the user that
	 * used the complete command.
	 * 
	 * You might want to use the Ticket's
	 * {@link com.nyancraft.reportrts.data.Ticket#getModName() ticket.getModName()}
	 * to get the staff who handled the ticket.
	 * 
	 * @return The user who completed the ticket.
	 */
	public CommandSender getCompleter(){
		return sender;
	}
        
}
