package com.nyancraft.reportrts.event;

import com.nyancraft.reportrts.data.Ticket;
import org.bukkit.command.CommandSender;

/**
 * Event that is called when a Request is marked
 * as completed by a moderator.
 * 
 * The completer is the user that sends the command.
 *
 */
public class TicketCompleteEvent extends TicketEvent {

	private CommandSender sender;
	
	public TicketCompleteEvent(Ticket request, CommandSender sender) {
            super(request);
            this.sender = sender;
	}
	
	/**
	 * This will get the user that
	 * used the complete command.
	 * 
	 * You might want to use the request's
	 * {@link com.nyancraft.reportrts.data.Ticket#getModName() request.getModName()}
	 * to get the mod who handled the request.
	 * 
	 * @return The user who completed the request.
	 */
	public CommandSender getCompleter(){
		return sender;
	}
        
}
