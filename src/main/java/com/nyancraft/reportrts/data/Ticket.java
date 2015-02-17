package com.nyancraft.reportrts.data;

import java.util.UUID;

public class Ticket {
    
    private int id;
    private int status;
    private int x;
    private int y;
    private int z;
    private int staffId;

    private float yaw;
    private float pitch;

    private long timestamp;
    private long staffTime;

    private String text;
    private String name;
    private String world;
    private String staffName;
    private String comment;
    private String server;

    private UUID uuid;
    private UUID staffUuid;
    private boolean notified;

    public Ticket(String name, UUID uuid, int id, long timestamp, String text, int status, int x, int y, int z, float yaw, float pitch, String world, String server, String comment){
        this.name = name;
        this.uuid = uuid;
        this.id = id;
        this.timestamp = timestamp;
        this.text = text;
        this.status = status;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.server = server;
        this.yaw = yaw;
        this.pitch = pitch;
        this.staffTime = 0;
        this.comment = comment;
    }

    /**
     * Retrieves the ticket message
     * @return String Message of ticket
     */
    public String getMessage(){
        return this.text;
    }

    /**
     * Retrieves UUID of player
     * @return UUID unique user id of player
     */
    public UUID getUUID(){
        return this.uuid;
    }

    /**
     * Retrieves name of player
     * @return String username
     */
    public String getName(){
        return this.name;
    }

    /**
     * Retrieves id of ticket
     * @return int id of ticket
     */
    public int getId(){
        return this.id;
    }

    /**
     * Retrieves ticket status
     * @return int status of ticket
     */
    public int getStatus(){
        return this.status;
    }

    /**
     * Retrieves timestamp when ticket was created
     * @return int timestamp of ticket
     */
    public long getTimestamp(){
        return this.timestamp;
    }


    /**
     * Retrieves timestamp when ticket was last interacted with by staff
     * @return int modtimestamp of ticket
     */
    public long getStaffTime(){
        return this.staffTime;
    }

    /**
     * Retrieves X where ticket was created
     * @return int X of ticket
     */
    public long getX(){
        return this.x;
    }

    /**
     * Retrieves Y where ticket was created
     * @return int Y of ticket
     */
    public long getY(){
        return this.y;
    }

    /**
     * Retrieves Z where ticket was created
     * @return int Z of ticket
     */
    public long getZ(){
        return this.z;
    }

    /**
     * Retrieves Yaw where ticket was created
     * @return int Yaw of ticket
     */
    public float getYaw(){
        return this.yaw;
    }

    /**
     * Retrieves Pitch where ticket was created
     * @return int Pitch of ticket
     */
    public float getPitch(){
        return this.pitch;
    }

    /**
     * Retrieves world where ticket was created
     * @return String world name
     */
    public String getWorld(){
        return this.world;
    }

    /**
     * Retrieves the BungeeCord server where the ticket was created
     * @return String BungeeCord server name
     */
    public String getServer() { return this.server; }

    /**
     * Retrieves the UUID of the player that handled the ticket, if any
     * @return UUID staffUuid
     */
    public UUID getStaffUuid(){
        return this.staffUuid;
    }

    /**
     * Retrieves name of the player that handled the ticket, if any
     * @return String staffName
     */
    public String getStaffName(){
        return this.staffName;
    }

    public int getStaffId(){
        return this.staffId;
    }

    /**
     * Retrieves the comment on the ticket
     * @return String comment
     */
    public String getComment(){
        return this.comment;
    }

    /**
     * @param status
     * Sets the status of the ticket
     */
    public void setStatus(int status){
        this.status = status;
    }

    /**
     * @param staffUuid
     * Sets the player UUID that handled this ticket
     */
    public void setStaffUuid(UUID staffUuid){
        this.staffUuid = staffUuid;
    }

    /**
     * @param name
     * Sets the name of the player who filed the ticket
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * @param staffName
     * Sets the staff name that handled this ticket
     */
    public void setStaffName(String staffName){
        this.staffName = staffName;
    }

    /**
     * @param staffTime
     * Sets timestamp when ticket was created
     */
    public void setStaffTime(long staffTime){
        this.staffTime = staffTime;
    }
    
    /**
     * @param comment
     * Sets the comment on the ticket
     */
    public void setComment(String comment){
        this.comment = comment;
    }

    /**
     * @return Whether the player is notified of the ticket or not.
     */
    public boolean isNotified() {
        return this.notified;
    }

    /**
     * @param notified
     * Set whether a player has been notified or not.
     */
    public void setNotified(boolean notified) {
        this.notified = notified;
    }
}
