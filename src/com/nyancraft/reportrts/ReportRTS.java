package com.nyancraft.reportrts;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.command.*;
import com.nyancraft.reportrts.data.HelpRequest;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ReportRTS extends JavaPlugin{

	private static ReportRTS plugin;
	private static final Logger log = Logger.getLogger("Minecraft");
	
	public Map<Integer, HelpRequest> requestMap = new LinkedHashMap<Integer, HelpRequest>();
	public Map<String, String> messageMap = new HashMap<String, String>();
	public Map<Integer, String> notificationMap = new HashMap<Integer, String>();
	
	public boolean notifyStaffOnNewRequest;
	public boolean hideNotification;
	public boolean hideWhenOffline;
	public boolean useMySQL;
	public boolean debugMode;
	public int maxRequests;
	public int requestDelay;
	public String mysqlPort;
	public String mysqlHostname;
	public String mysqlDatabase;
	public String mysqlUsername;
	public String mysqlPassword;
	
	public static Permission permission = null;
	
	public void onDisable(){
		DatabaseManager.getDatabase().disconnect();
		saveConfig();
	}
	
	public void onEnable(){
		plugin = this;
		reloadSettings();
		final PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new RTSListener(plugin), plugin);
		if(!DatabaseManager.load()){
			log.severe("Encountered an error while attempting to connect to the database.  Disabling...");
			pm.disablePlugin(this);
		}
		DatabaseManager.getDatabase().populateRequestMap();
		RTSFunctions.populateHeldRequestsWithData();
		RTSFunctions.populateNotificationMapWithData();
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
		getCommand("mod-broadcast").setExecutor(new ModBroadcastCommand(plugin));
		if(getServer().getPluginManager().getPlugin("Vault") != null) setupPermissions();
	}
	
	public void reloadPlugin(){
		requestMap.clear();
		messageMap.clear();
		reloadSettings();
		DatabaseManager.getDatabase().populateRequestMap();
		RTSFunctions.populateHeldRequestsWithData();
	} 
	
	public void reloadSettings(){
		reloadConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();
		notifyStaffOnNewRequest = getConfig().getBoolean("notifyStaff");
		hideNotification = getConfig().getBoolean("hideMessageIfEmpty");
		hideWhenOffline = getConfig().getBoolean("request.hideOfflineSenders");
		maxRequests = getConfig().getInt("request.max");
		requestDelay = getConfig().getInt("request.delay");
		useMySQL = getConfig().getBoolean("mysql.enable");
		mysqlPort = getConfig().getString("mysql.port");
		mysqlHostname = getConfig().getString("mysql.hostname");
		mysqlDatabase = getConfig().getString("mysql.database");
		mysqlUsername = getConfig().getString("mysql.username");
		mysqlPassword = getConfig().getString("mysql.password");
		debugMode = getConfig().getBoolean("debug");
		ConfigurationSection Messages = getConfig().getConfigurationSection("messages");
		for(String message : Messages.getKeys(false)){
			messageMap.put(message, Messages.getString(message));
		}
	}
	
    public static ReportRTS getPlugin(){
        return plugin;
    }
    
    private Boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
            log.info("[ReportRTS] Vault and a compatible permissions manager was found. Using Vault for permissions.");
        }
        return (permission != null);
    }
}
