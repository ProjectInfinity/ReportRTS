package com.nyancraft.reportrts.event;

import com.nyancraft.reportrts.data.Ticket;

/**
 * Event that is called when a user files a new
 * Ticket to the moderators for handling.
 */
public class TicketCreateEvent extends TicketEvent {
	
	public TicketCreateEvent(Ticket request){
		super(request);
	}
}
