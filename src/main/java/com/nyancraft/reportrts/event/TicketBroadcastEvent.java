package com.nyancraft.reportrts.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a Moderator broadcasts a message.
 *
 */
public class TicketBroadcastEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    String message;
    CommandSender sender;

    public TicketBroadcastEvent(CommandSender sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    /**
     * Get the mod-broadcast message.
     *
     * @return a String with the mod-broadcast message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the sender of the mod-broadcast message.
     *
     * @return a CommandSender object of the user that sent the broadcast.
     */
    public CommandSender getSender() {
        return sender;
    }

    /**
     *
     * @return
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     *
     * @return
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
