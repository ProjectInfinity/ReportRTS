package com.nyancraft.reportrts.event;

import com.nyancraft.reportrts.data.Ticket;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Base class for all event regarding Reports.
 *
 */
public abstract class TicketEvent extends Event {

private static final HandlerList handlers = new HandlerList();
	
	/**
	 * The request that the event regards.
	 */
	private Ticket ticket;

	public TicketEvent(Ticket request){
		this.ticket = request;
	}
	
	/**
	 * Get the request that this event regards.
	 * This will have the data of the request after
	 * the event has happened.
	 * 
	 * i.e. a claim event will have the .getModName() equal
	 * to the user claiming the request.
	 * 
	 * @return a HelpRequst object with all the request data in it.
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
