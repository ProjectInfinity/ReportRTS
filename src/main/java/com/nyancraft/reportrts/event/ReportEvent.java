package com.nyancraft.reportrts.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.nyancraft.reportrts.data.HelpRequest;

/**
 * Base class for all event regarding Reports.
 *
 */
public abstract class ReportEvent extends Event {

private static final HandlerList handlers = new HandlerList();
	
	/**
	 * The request that the event regards.
	 */
	private HelpRequest request;
	
	public ReportEvent(HelpRequest request){
		this.request = request;
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
	public HelpRequest getRequest(){
		return this.request;
	}
	
	public HandlerList getHandlers(){
		return handlers;
	}
	
	public static HandlerList getHandlerList(){
		return handlers;
	}
}
