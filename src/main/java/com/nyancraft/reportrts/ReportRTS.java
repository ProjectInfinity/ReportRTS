package com.nyancraft.reportrts;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import com.nyancraft.reportrts.api.ApiServer;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.command.*;
import com.nyancraft.reportrts.data.HelpRequest;
import com.nyancraft.reportrts.util.*;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class ReportRTS extends JavaPlugin implements PluginMessageListener {

    private static ReportRTS plugin;
    private final Logger log = Logger.getLogger("Minecraft");
    private static MessageHandler messageHandler = new MessageHandler();
    private VersionChecker versionChecker = new VersionChecker();

    public Map<Integer, HelpRequest> requestMap = new LinkedHashMap<Integer, HelpRequest>();
    public Map<Integer, String> notificationMap = new HashMap<Integer, String>();
    public Map<String, Integer> teleportMap = new HashMap<String, Integer>();
    public ArrayList<String> moderatorMap = new ArrayList<String>();

    public boolean notifyStaffOnNewRequest;
    public boolean notificationSound;
    public boolean hideNotification;
    public boolean hideWhenOffline;
    public boolean debugMode;
    public boolean outdated;
    public boolean vanishSupport;
    public boolean bungeeCordSupport;
    public boolean setupDone = true;
    public boolean requestNagHeld;
    public boolean apiEnabled;

    public int maxRequests;
    public int requestDelay;
    public int requestMinimumWords;
    public int requestsPerPage;
    public int storagePort;
    public long requestNagging;
    public long storageRefreshTime;
    public long bungeeCordSync;
    public String storageType;
    public String storageHostname;
    public String storageDatabase;
    public String storageUsername;
    public String storagePassword;
    public String storagePrefix;
    public String versionString;
    public String bungeeCordServerPrefix;

    public static Permission permission = null;

    private ApiServer apiServer;
    private int apiPort;
    private List<String> apiAllowedIPs = new ArrayList<>();

    private String serverIP;

    public void onDisable(){
        DatabaseManager.getDatabase().disconnect();
        if(apiEnabled){
            try{
                apiServer.getListener().close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        messageHandler.saveMessageConfig();
    }

    public void onEnable(){
        plugin = this;
        reloadSettings();
        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new RTSListener(plugin), plugin);
        if(!(storageHostname.equalsIgnoreCase("localhost") && storagePort == 3306 && storageDatabase.equalsIgnoreCase("minecraft")
        && storageUsername.equalsIgnoreCase("username") && storagePassword.equalsIgnoreCase("password")
        && storagePrefix.equalsIgnoreCase("") && storageRefreshTime == 600)){
            if(!DatabaseManager.load()){
                log.severe("Encountered an error while attempting to connect to the database.  Disabling...");
                pm.disablePlugin(this);
            }
            reloadPlugin();
        }else{
            setupDone = false;
        }
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

        getCommand("check").setTabCompleter(new TabCompleteHelper(plugin));
        getCommand("complete").setTabCompleter(new TabCompleteHelper(plugin));
        getCommand("tp-id").setTabCompleter(new TabCompleteHelper(plugin));
        getCommand("hold").setTabCompleter(new TabCompleteHelper(plugin));
        getCommand("claim").setTabCompleter(new TabCompleteHelper(plugin));
        getCommand("unclaim").setTabCompleter(new TabCompleteHelper(plugin));
        getCommand("assign").setTabCompleter(new TabCompleteHelper(plugin));

        if(getServer().getPluginManager().getPlugin("Vault") != null) setupPermissions();
        try{
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        }catch(IOException e){
            log.info("Unable to submit stats!");
        }
        if(apiEnabled){
            try{
                Properties props = new Properties();
                props.load(new FileReader("server.properties"));
                serverIP = props.getProperty("server-ip", "ANY");
                if(serverIP.isEmpty()) serverIP = "ANY";
                apiServer = new ApiServer(plugin, serverIP, apiPort, apiAllowedIPs);
            }catch(IOException e){
                log.warning("[ReportRTS] Unable to start API server!");
                e.printStackTrace();
            }
            apiServer.start();
        }
        if(requestNagging > 0){
            getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
                public void run(){
                    int openRequests = requestMap.size();
                    if(requestNagHeld){
                        int heldRequests = DatabaseManager.getDatabase().getNumberHeldRequests();
                        if(heldRequests > 0){
                            if(openRequests > 0) RTSFunctions.messageMods(Message.parse("generalOpenHeldRequests", openRequests, heldRequests), false);
                        }else{
                            if(openRequests > 0) RTSFunctions.messageMods(Message.parse("generalOpenRequests", openRequests), false);
                        }
                    }else{
                        if(openRequests > 0) RTSFunctions.messageMods(Message.parse("generalOpenRequests", openRequests), false);
                    }
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

        if(bungeeCordSupport){
            // Register BungeeCord channels.
            getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

            // Schedule a offline-sync incase no players are online.
            getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
                public void run(){
                    if(BungeeCord.isServerEmpty()){
                        RTSFunctions.sync();
                    }
                }
            }, plugin.bungeeCordSync * 20, plugin.bungeeCordSync * 20);
        }
    }

    public void reloadPlugin(){;
        reloadSettings();
        RTSFunctions.sync();
    }

    public void reloadSettings(){
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        messageHandler.reloadMessageConfig();
        messageHandler.saveMessageConfig();
        messageHandler.reloadMessageMap();
        notifyStaffOnNewRequest = getConfig().getBoolean("notifyStaff");
        notificationSound = getConfig().getBoolean("notifySound");
        hideNotification = getConfig().getBoolean("hideMessageIfEmpty");
        hideWhenOffline = getConfig().getBoolean("request.hideOffline");
        maxRequests = getConfig().getInt("request.max");
        requestDelay = getConfig().getInt("request.delay");
        requestMinimumWords = getConfig().getInt("request.minimumWords");
        requestsPerPage = getConfig().getInt("request.perPage");
        requestNagging = getConfig().getLong("request.nag");
        requestNagHeld = getConfig().getBoolean("request.nagHeld", false);
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
        bungeeCordSupport = getConfig().getBoolean("bungeecord.enable", false);
        bungeeCordSync = getConfig().getLong("bungeecord.sync", 300L);
        bungeeCordServerPrefix = getConfig().getString("bungeecord.serverPrefix");
        BungeeCord.setServer(getConfig().getString("bungeecord.serverName"));
        apiEnabled = getConfig().getBoolean("api.enable", false);
        apiPort = getConfig().getInt("api.port", 25567);
        apiAllowedIPs = getConfig().getStringList("api.whitelist");
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

    public void onPluginMessageReceived(String pluginChannel, Player player, byte[] bytes){
        if(!pluginChannel.equals("BungeeCord")) return;

        BungeeCord.handleNotify(bytes);
    }
}
