package com.nyancraft.reportrts.data;

import java.util.UUID;

public class Ticket {
    
    private int id;
    private int status;
    private int x;
    private int y;
    private int z;
    private int modid;

    private float yaw;
    private float pitch;

    private long tstamp;
    private long modtstamp;

    private String text;
    private String name;
    private String world;
    private String modname;
    private String modcomment;
    private String bc_server;

    private UUID uuid;
    private UUID moduuid;

    public Ticket(String name, UUID uuid, int id, long tstamp, String text, int status, int x, int y, int z, float yaw, float pitch, String world, String bc_server, String modcomment){
        this.name = name;
        this.uuid = uuid;
        this.id = id;
        this.tstamp = tstamp;
        this.text = text;
        this.status = status;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.bc_server = bc_server;
        this.yaw = yaw;
        this.pitch = pitch;
        this.modtstamp = 0;
        this.modcomment = modcomment;
    }

    /**
     * Retrieves help request message
     * @return String Message of request
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
        return this.tstamp;
    }


    /**
     * Retrieves timestamp when ticket was last interacted with by staff
     * @return int modtimestamp of ticket
     */
    public long getModTimestamp(){
        return this.modtstamp;
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
    public String getBungeeCordServer() { return this.bc_server; }

    /**
     * Retrieves the UUID of the moderator that handled the ticket, if any
     * @return UUID moduuid
     */
    public UUID getModUUID(){
        return this.moduuid;
    }

    /**
     * Retrieves name of the moderator that handled the ticket, if any
     * @return String modname
     */
    public String getModName(){
        return this.modname;
    }

    public int getModId(){
        return this.modid;
    }

    /**
     * Retrieves the mod comment on the ticket
     * @return String modcomment
     */
    public String getModComment(){
        return this.modcomment;
    }

    /**
     * @param status
     * Sets the status of the ticket
     */
    public void setStatus(int status){
        this.status = status;
    }

    /**
     * @param moduuid
     * Sets the moderator UUID that handled this ticket
     */
    public void setModUUID(UUID moduuid){
        this.moduuid = moduuid;
    }

    /**
     * @param name
     * Sets the name of the person who filed the ticket
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * @param modname
     * Sets the moderator name that handled this ticket
     */
    public void setModName(String modname){
        this.modname = modname;
    }

    /**
     * @param modTimestamp
     * Sets timestamp when ticket was created
     */
    public void setModTimestamp(long modTimestamp){
        this.modtstamp = modTimestamp;
    }
    
    /**
     * @param modcomment
     * Sets the mod comment on the ticket
     */
    public void setModComment(String modcomment){
        this.modcomment = modcomment;
    }
}
