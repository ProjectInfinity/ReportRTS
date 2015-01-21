package com.nyancraft.reportrts.event;

import com.nyancraft.reportrts.data.Ticket;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Base class for all event regarding Tickets.
 *
 */
public abstract class TicketEvent extends Event {

private static final HandlerList handlers = new HandlerList();
	
	/**
	 * The ticket that the event regards.
	 */
	private Ticket ticket;

	public TicketEvent(Ticket ticket){
		this.ticket = ticket;
	}
	
	/**
	 * Get the ticket that this event regards.
	 * This will have the data of the ticket after
	 * the event has happened.
	 * 
	 * i.e. a claim event will have the .getModName() equal
	 * to the user claiming the ticket.
	 * 
	 * @return a Ticket object with all the request data in it.
	 */
	public Ticket getTicket(){
		return this.ticket;
	}
	
	public HandlerList getHandlers(){
		return handlers;
	}
	
	public static HandlerList getHandlerList(){
		return handlers;
	}
}
