package com.nyancraft.reportrts;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.nyancraft.reportrts.RTSDatabaseManager;
import com.nyancraft.reportrts.command.*;
import com.nyancraft.reportrts.data.HelpRequest;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


public class ReportRTS extends JavaPlugin{

	private static ReportRTS plugin;
	private static final Logger log = Logger.getLogger("Minecraft");
	
	public Map<Integer, HelpRequest> requestMap = new LinkedHashMap<Integer, HelpRequest>();
	public boolean notifyStaffOnNewRequest;
	public boolean hideNotification;
	public int maxRequests;
	
	public static Permission permission = null;
	
	public void onDisable(){
		RTSDatabaseManager.disableDB();
		saveConfig();
	}
	
	public void onEnable(){
		plugin = this;
		getServer().getPluginManager().registerEvents(new RTSListener(plugin), plugin);
		RTSDatabaseManager.enableDB();
		reloadPlugin();
		getCommand("modreq").setExecutor(new ModreqCommand(plugin));
		getCommand("check").setExecutor(new CheckCommand(plugin));
		getCommand("complete").setExecutor(new CompleteCommand(plugin));
		getCommand("reopen").setExecutor(new ReopenCommand(plugin));
		getCommand("tp-id").setExecutor(new TeleportCommand(plugin));
		getCommand("reportrts").setExecutor(new ReportRTSCommand(plugin));
		getCommand("hold").setExecutor(new HoldCommand(plugin));
		getCommand("claim").setExecutor(new ClaimCommand(plugin));
		getCommand("unclaim").setExecutor(new UnclaimCommand(plugin));
		getCommand("modlist").setExecutor(new ModlistCommand());
		getCommand("mod-broadcast").setExecutor(new ModBroadcastCommand());
		if(getServer().getPluginManager().getPlugin("Vault") != null) setupPermissions();
	}
	
	public void reloadPlugin(){
		requestMap.clear();
		RTSDatabaseManager.getOpenRequests();
		RTSFunctions.populateHeldRequestsWithData();
		reloadConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();
		notifyStaffOnNewRequest = getConfig().getBoolean("notifyStaff");
		hideNotification = getConfig().getBoolean("hideMessageIfEmpty");
		maxRequests = getConfig().getInt("maxRequests");
	} 
	
    public static ReportRTS getPlugin(){
        return plugin;
    }
    
    private Boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
}
