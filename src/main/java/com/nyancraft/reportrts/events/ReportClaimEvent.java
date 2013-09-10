package com.nyancraft.reportrts.events;

import com.nyancraft.reportrts.data.HelpRequest;

/**
 * Event that is called when a Moderator claims
 * a request for handling.
 *
 */
public class ReportClaimEvent extends ReportEvent {

	public ReportClaimEvent(HelpRequest request) {
		super(request);
	}

}
