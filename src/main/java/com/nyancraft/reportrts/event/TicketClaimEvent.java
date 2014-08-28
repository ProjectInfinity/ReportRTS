package com.nyancraft.reportrts.event;

import com.nyancraft.reportrts.data.Ticket;

/**
 * Event that is called when a Moderator claims
 * a request for handling.
 *
 */
public class TicketClaimEvent extends TicketEvent {

	public TicketClaimEvent(Ticket request) {
		super(request);
	}

}
