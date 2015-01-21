package com.nyancraft.reportrts.event;

import com.nyancraft.reportrts.data.Ticket;

/**
 * Event that is called when staff claims
 * a ticket for handling.
 */
public class TicketClaimEvent extends TicketEvent {

	public TicketClaimEvent(Ticket ticket) {
		super(ticket);
	}

}
