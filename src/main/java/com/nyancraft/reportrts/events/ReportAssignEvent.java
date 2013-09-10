package com.nyancraft.reportrts.events;

import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.data.HelpRequest;

/**
 * Event that is called when a moderator
 * assigns another moderator a request, <code>/assign.</code>
 *
 */
public class ReportAssignEvent extends ReportEvent {

	private CommandSender sender;
	
	public ReportAssignEvent(HelpRequest request, CommandSender sender) {
		super(request);
		this.sender = sender;
	}
	
	/**
	 * The user that assigned the request to the other user.
	 * Get the assignee with HelpRequest's .getModName()
	 * @return CommandSender object of the user that assigned the request.
	 */
	public CommandSender getAssigner(){
		return sender;
	}

}
