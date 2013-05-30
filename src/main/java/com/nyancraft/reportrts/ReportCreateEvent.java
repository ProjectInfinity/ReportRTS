package com.nyancraft.reportrts;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.nyancraft.reportrts.data.HelpRequest;

public class ReportCreateEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	
	private HelpRequest request;
	
	public ReportCreateEvent(HelpRequest request){
		this.request = request;
	}
	
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
