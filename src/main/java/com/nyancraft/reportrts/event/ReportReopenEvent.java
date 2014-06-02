package com.nyancraft.reportrts.event;

import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.data.HelpRequest;

/**
 * Event that is called when a moderator
 * assigns another moderator a request, /assign.
 *
 */
public class ReportReopenEvent extends ReportEvent {

	private final CommandSender sender;
	
	public ReportReopenEvent(HelpRequest request, CommandSender sender) {
		super(request);
		this.sender = sender;
	}
	
	/**
	 * The user that assigned the request to the other user.
	 * Get the assignee with HelpRequest's .getModUUID()
	 * @return CommandSender object of the user that assigned the request.
	 */
	public CommandSender getAssigner(){
		return sender;
	}

}
