package com.nyancraft.reportrts.persistence;

import com.nyancraft.reportrts.data.Ticket;
import com.nyancraft.reportrts.data.User;

import org.bukkit.Location;

import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.logging.Logger;

public interface DataProvider {

    public Logger log = null;

    /**
     * Check if data-provider is loaded.
     * @return true if loaded
     */
    public boolean isLoaded();

    public void close();

    public void reset();

    public boolean load();

    /**
     * Marks all notifications as read.
     * @return Boolean
     */
    public boolean resetNotifications();

    /**
     * Creates a user and returns the
     * ID of the newly created user.
     * @param username Name of the user
     * @return User ID
     */
    public int createUser(String username);

    /**
     * Creates a user and returns the
     * ID of the newly created user.
     * @param uuid UUID of the user
     * @return User ID
     */
    public int createUser(UUID uuid);

    /**
     * Creates a new comment and returns
     * the ID of the newly created comment.
     * @param name of player
     * @param timestamp when comment was made
     * @param comment in a string
     * @param ticketId of the ticket
     * @return Comment ID
     */
    public int createComment(String name, long timestamp, String comment, int ticketId);

    /**
     * Creates a ticket and returns the
     * ID of the newly created ticket.
     * @param user Map containing all info about the user
     * @param location location of the user
     * @param message ticket message
     * @return ticket ID
     */
    public int createTicket(User user, Location location, String message);

    /**
     * Count all tickets of a specific status.
     * @param status Status of tickets to be counted
     * @return a number of total tickets of specified status
     */
    public int countTickets(int status);

    /**
     * Gets a User with information
     * regarding the target player.
     * If player does not exist
     * the player's ID will be 0.
     * @param uuid UUID of the user
     * @param id ID of the user
     * @param create Create user if not exists
     * @return User class
     */
    public User getUser(UUID uuid, int id, boolean create);

    /**
     * Attempts to get a player by
     * name, this will only be used
     * as a LAST RESORT in case we cannot
     * find a player without it.
     * @param name Player's name
     * @return User object
     */
    public User getUnsafeUser(String name);

    /**
     * Gets the User class for console.
     * @return Console's User class
     */
    public User getConsole();

    /**
     * Gets all tickets of specified status starting at cursor and ending at limit.
     * @param status Status of the ticket
     * @param cursor Start position
     * @param limit End position
     * @return LinkedHashMap containing all tickets of specified status
     */
    public LinkedHashMap<Integer, Ticket> getTickets(int status, int cursor, int limit);

    /**
     * Gets ALL tickets of specified ticket status.
     * @param status Status of the ticket
     * @return LinkedHashMap containing all tickets of specified status
     */
    public LinkedHashMap<Integer, Ticket> getTickets(int status);

    /**
     * Gets the ticket with the specified ID, or returns null.
     * @param id Ticket ID
     * @return Ticket or null
     */
    public Ticket getTicket(int id);

    /**
     * Get tickets closed by user that starts at cursor and ends with limit.
     * @param uuid The UUID of the player
     * @param cursor Start position of query
     * @param limit End position of query
     * @return LinkedHashMap containing closed tickets by the player
     */
    public LinkedHashMap<Integer, Ticket> getHandledBy(UUID uuid, int cursor, int limit);

    /**
     * Get tickets opened by user that starts at cursor and ends with limit.
     * @param uuid The UUID of the player
     * @param cursor Start position of query
     * @param limit End position of query
     * @return LinkedHashMap containing opened tickets by the player
     */
    public LinkedHashMap<Integer, Ticket> getOpenedBy(UUID uuid, int cursor, int limit);

    /**
     * Gets the top X amount of players sorted by resolved tickets.
     * @param limit Number of players to get
     * @return LinkedHashMap containing player name and ticket numers
     */
    public LinkedHashMap<String, Integer> getTop(int limit);

    /**
     * Set the ticket status of a ticket and return the result code.
     * @param id Id of ticket
     * @param uuid uuid of player changing ticket status
     * @param username name of player changing ticket status
     * @param status status to change ticket to
     * @param comment closing comment on ticket
     * @param notified whether the player is notified or not
     * @param timestamp timestamp of when the ticket was changed
     * @return resultcode as a number
     */
    public int setTicketStatus(int id, UUID uuid, String username, int status, String comment, boolean notified, long timestamp);

    /**
     * Sets the status of a notification to the provided status and
     * returns a result code.
     * @param ticketId Id of ticket
     * @param status Status as a boolean
     * @return Resultcode as a number
     */
    public int setNotificationStatus(int ticketId, boolean status);

    /**
     * Sets the status of a user to the provided status and
     * returns a result code.
     * @param uuid UUID of the player
     * @param status Status as a boolean
     * @return Resultcode as a number
     */
    public int setUserStatus(UUID uuid, boolean status);

    /**
     * Delete the specified ticket.
     * @param ticketId ID of ticket
     */
    public void deleteTicket(int ticketId);
}