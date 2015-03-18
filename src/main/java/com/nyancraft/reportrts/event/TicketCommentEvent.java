package com.nyancraft.reportrts.event;

import com.nyancraft.reportrts.data.Ticket;
import org.bukkit.command.CommandSender;

/**
 * Event that is called when a comment is added
 * to a ticket by a player.
 *
 * The commenter is the user that sends the command.
 *
 */
public class TicketCommentEvent extends TicketEvent {

    private CommandSender sender;
    private String comment;

    public TicketCommentEvent(Ticket ticket, CommandSender sender, String comment) {
        super(ticket);
        this.sender = sender;
        this.comment = comment;
    }

    /**
     * This will get the user that
     * used the comment command.
     *
     * @return The user who commented on the ticket.
     */
    public CommandSender getSender() {
        return sender;
    }

    /**
     * This will get the comment that
     * the user made.
     *
     * @return The comment that was made.
     */
    public String getComment() {
        return this.comment;
    }

}
