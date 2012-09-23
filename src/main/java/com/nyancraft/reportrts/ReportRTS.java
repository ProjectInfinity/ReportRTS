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
import net.milkbowl.vault.permission.Permission;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ReportRTS extends JavaPlugin{

    private static ReportRTS plugin;
    private static final Logger log = Logger.getLogger("Minecraft");
    private static MessageHandler messageHandler = new MessageHandler();

    public Map<Integer, HelpRequest> requestMap = new LinkedHashMap<Integer, HelpRequest>();
    public Map<Integer, String> notificationMap = new HashMap<Integer, String>();
    public ArrayList<String> moderatorMap = new ArrayList<String>();

    public boolean notifyStaffOnNewRequest;
    public boolean hideNotification;
    public boolean hideWhenOffline;
    public boolean useMySQL;
    public boolean debugMode;
    public int maxRequests;
    public int requestDelay;
    public int requestsPerPage;
    public long requestNagging;
    public String mysqlPort;
    public String mysqlHostname;
    public String mysqlDatabase;
    public String mysqlUsername;
    public String mysqlPassword;

    public static Permission permission = null;

    public void onDisable(){
        DatabaseManager.getDatabase().disconnect();
        saveConfig();
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
                    if(openRequests > 0) RTSFunctions.messageMods(Message.parse("generalOpenRequests", openRequests));
                }
            }, 120L, (requestNagging * 60) * 20);
        }
    }

    public void reloadPlugin(){
        requestMap.clear();
        notificationMap.clear();
        reloadSettings();
        DatabaseManager.getDatabase().populateRequestMap();
        RTSFunctions.populateHeldRequestsWithData();
        RTSFunctions.populateNotificationMapWithData();
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
        requestsPerPage = getConfig().getInt("request.perPage");
        requestNagging = getConfig().getLong("request.nag");
        useMySQL = getConfig().getBoolean("mysql.enable");
        mysqlPort = getConfig().getString("mysql.port");
        mysqlHostname = getConfig().getString("mysql.hostname");
        mysqlDatabase = getConfig().getString("mysql.database");
        mysqlUsername = getConfig().getString("mysql.username");
        mysqlPassword = getConfig().getString("mysql.password");
        debugMode = getConfig().getBoolean("debug");
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
            log.info("[ReportRTS] Vault and a compatible permissions manager was found. Using Vault for permissions.");
        }
        return (permission != null);
    }
}
