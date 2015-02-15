package com.nyancraft.reportrts.event;

import com.nyancraft.reportrts.data.Ticket;

/**
 * Event that is called when a user files a new
 * Ticket to the staff for handling.
 */
public class TicketOpenEvent extends TicketEvent {
	
	public TicketOpenEvent(Ticket ticket){
		super(ticket);
	}
}
