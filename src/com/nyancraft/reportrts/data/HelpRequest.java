package com.nyancraft.reportrts.data;

public class HelpRequest {

	private int id;
	private int status;
	private int x;
	private int y;
	private int z;
	private int modid;
	
	private long tstamp;
	
	private String text;
    private String name;
    private String world;
    private String modname;
    
	public HelpRequest(String name, int id, long tstamp, String text, int status, int x, int y, int z, String world){
		this.name = name;
		this.id = id;
		this.tstamp = tstamp;
		this.text = text;
		this.status = status;
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;
	}
	
	/**
	 * Retrieves help request message
	 * @return String Message of request
	 */
	public String getMessage(){
		return this.text;
	}
	
	/**
	 * Retrieves name of player
	 * @return String name of player
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
	 * Retrieves world where ticket was created
	 * @return String world name
	 */
	public String getWorld(){
		return this.world;
	}
	
	/**
	 * Retrieves the name of the moderator that handled the ticket, if any
	 * @return
	 */
	public String getModName(){
		return this.modname;
	}
	
	public int getModId(){
		return this.modid;
	}
	
	/**
	 * Sets the status of the ticket
	 * @param status
	 */
	public void setStatus(int status){
		this.status = status;
	}
	
	/**
	 * Sets the moderator that handled this ticket
	 * @param modname
	 */
	public void setModName(String modname){
		this.modname = modname;
	}
}
