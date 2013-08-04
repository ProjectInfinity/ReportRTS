package com.nyancraft.reportrts.events;

import com.nyancraft.reportrts.data.HelpRequest;

/**
 * Event that is called when a user files a new
 * HelpRequest to the moderators for handling.
 */
public class ReportCreateEvent extends ReportEvent {
	
	public ReportCreateEvent(HelpRequest request){
		super(request);
	}
}
