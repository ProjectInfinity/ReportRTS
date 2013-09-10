package com.nyancraft.reportrts.events;

import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.data.HelpRequest;

/**
 * Event that is called when a Request is marked
 * as completed by a moderator.
 * 
 * The completer is the user that sends the command.
 *
 */
public class ReportCompleteEvent extends ReportEvent {

	private CommandSender sender;
	
	public ReportCompleteEvent(HelpRequest request, CommandSender sender) {
            super(request);
            this.sender = sender;
	}
	
	/**
	 * This will get the user that
	 * <b>used the complete command</b>.
	 * 
	 * You might want to use the request's
	 * {@link com.nyancraft.reportrts.data.HelpRequest.java#getModName() request.getModName()}
	 * to get the mod who handled the request.
	 * 
	 * @return The user who completed the request.
	 */
	public CommandSender getCompleter(){
		return sender;
	}

}
