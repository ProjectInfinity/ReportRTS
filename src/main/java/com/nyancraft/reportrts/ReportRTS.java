package com.nyancraft.reportrts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.command.*;
import com.nyancraft.reportrts.data.HelpRequest;

import com.nyancraft.reportrts.util.Message;
import com.nyancraft.reportrts.util.MessageHandler;
import com.nyancraft.reportrts.util.VersionChecker;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ReportRTS extends JavaPlugin{

    private static ReportRTS plugin;
    private final Logger log = Logger.getLogger("Minecraft");
    private static MessageHandler messageHandler = new MessageHandler();
    private VersionChecker versionChecker = new VersionChecker();

    public Map<Integer, HelpRequest> requestMap = new LinkedHashMap<Integer, HelpRequest>();
    public Map<Integer, String> notificationMap = new HashMap<Integer, String>();
    public ArrayList<String> moderatorMap = new ArrayList<String>();

    public boolean notifyStaffOnNewRequest;
    public boolean hideNotification;
    public boolean hideWhenOffline;
    public boolean debugMode;
    public boolean outdated;
    public boolean vanishSupport;
    public int maxRequests;
    public int requestDelay;
    public int requestMinimumWords;
    public int requestsPerPage;
    public int storagePort;
    public long requestNagging;
    public long storageRefreshTime;
    public String storageType;
    public String storageHostname;
    public String storageDatabase;
    public String storageUsername;
    public String storagePassword;
    public String storagePrefix;
    public String versionString;

    public static Permission permission = null;

    public void onDisable(){
        DatabaseManager.getDatabase().disconnect();
        messageHandler.saveMessageConfig();
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
        reloadPlugin();
        outdated = !versionChecker.upToDate();
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
        getCommand("assign").setExecutor(new AssignCommand(plugin));
        if(getServer().getPluginManager().getPlugin("Vault") != null) setupPermissions();
        try{
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        }catch(IOException e){
            log.info("Unable to submit stats!");
        }
        if(requestNagging > 0){
            getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
                public void run(){
                    int openRequests = requestMap.size();
                    if(openRequests > 0) RTSFunctions.messageMods(Message.parse("generalOpenRequests", openRequests), false);
                }
            }, 120L, (requestNagging * 60) * 20);
        }

        if(plugin.storageRefreshTime > 0){
            getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
                public void run(){
                    DatabaseManager.getDatabase().refresh();
                }
            }, 4000L, plugin.storageRefreshTime * 20);
        }
    }

    public void reloadPlugin(){
        requestMap.clear();
        notificationMap.clear();
        moderatorMap.clear();
        reloadSettings();
        DatabaseManager.getDatabase().populateRequestMap();
        RTSFunctions.populateHeldRequestsWithData();
        RTSFunctions.populateNotificationMapWithData();
        RTSFunctions.populateModeratorMapWithData();
    }

    public void reloadSettings(){
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        messageHandler.reloadMessageConfig();
        messageHandler.saveMessageConfig();
        messageHandler.reloadMessageMap();
        notifyStaffOnNewRequest = getConfig().getBoolean("notifyStaff");
        hideNotification = getConfig().getBoolean("hideMessageIfEmpty");
        hideWhenOffline = getConfig().getBoolean("request.hideOffline");
        maxRequests = getConfig().getInt("request.max");
        requestDelay = getConfig().getInt("request.delay");
        requestMinimumWords = getConfig().getInt("request.minimumWords");
        requestsPerPage = getConfig().getInt("request.perPage");
        requestNagging = getConfig().getLong("request.nag");
        storageRefreshTime = getConfig().getLong("storage.refreshTime");
        storageType = getConfig().getString("storage.type", "mysql");
        storagePort = getConfig().getInt("storage.port");
        storageHostname = getConfig().getString("storage.hostname");
        storageDatabase = getConfig().getString("storage.database");
        storageUsername = getConfig().getString("storage.username");
        storagePassword = getConfig().getString("storage.password");
        storagePrefix = getConfig().getString("storage.prefix");
        debugMode = getConfig().getBoolean("debug");
        vanishSupport = getConfig().getBoolean("VanishSupport", false);
    }

    public static ReportRTS getPlugin(){
        return plugin;
    }

    public static MessageHandler getMessageHandler(){
        return messageHandler;
    }

    private Boolean setupPermissions(){
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if(permissionProvider != null){
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
}
