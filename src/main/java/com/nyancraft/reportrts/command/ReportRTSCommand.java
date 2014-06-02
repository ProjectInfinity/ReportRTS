package com.nyancraft.reportrts.command;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.persistence.Database;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;
import com.nyancraft.reportrts.util.BungeeCord;
import org.bukkit.entity.Player;

public class ReportRTSCommand implements CommandExecutor{

    private ReportRTS plugin;
    private ResultSet rs;
    private Database dbManager;

    private boolean storageHostname = false;
    private boolean storagePort = false;
    private boolean storageDatabase = false;
    private boolean storageUsername = false;
    private boolean storagePassword = false;
    private boolean storageRefresh = false;

    public ReportRTSCommand(ReportRTS plugin) {
        this.plugin = plugin;
        this.dbManager = DatabaseManager.getDatabase();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0) return false;
        try{
            switch(args[0].toUpperCase()){

            case "RELOAD":
                if(!RTSPermissions.canReloadPlugin(sender)) return true;
                plugin.reloadPlugin();
                sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] Reloaded configuration and requests.");
                break;

            case "BAN":
                if(args.length < 2) return false;
                if(!RTSPermissions.canBanUser(sender)) return true;
                if(!dbManager.setUserStatus(args[1], 1)){
                    sender.sendMessage(Message.parse("generalInternalError", "Cannot ban " + args[1] + " from filing requests."));
                    return true;
                }
                BungeeCord.globalNotify(Message.parse("banUser", sender.getName(), args[1]), -1, NotificationType.NOTIFYONLY);
                RTSFunctions.messageMods(Message.parse("banUser", sender.getName(), args[1]), false);
                break;

            case "UNBAN":
                if(args.length < 2) return false;
                if(!RTSPermissions.canBanUser(sender)) return true;
                if(!dbManager.setUserStatus(args[1], 0)){
                    sender.sendMessage(Message.parse("generalInternalError", "Cannot unban " + args[1] + " from filing requests."));
                    return true;
                }
                BungeeCord.globalNotify(Message.parse("unbanUser", sender.getName(), args[1]), -1, NotificationType.NOTIFYONLY);
                RTSFunctions.messageMods(Message.parse("unbanUser", sender.getName(), args[1]), false);
                break;

            case "RESET":
                if(!RTSPermissions.canResetPlugin(sender)) return true;
                if(!dbManager.resetDB()){
                    sender.sendMessage(ChatColor.RED + "[ReportRTS] An unexpected error occurred when attempting to reset the plugin.");
                    return true;
                }
                plugin.reloadPlugin();
                sender.sendMessage(ChatColor.GOLD + "[ReportRTS] You deleted all users and requests from ReportRTS.");
                plugin.getLogger().log(Level.INFO, sender.getName() + " deleted all users and requests from ReportRTS!");
                break;

            case "STATS":
                if(args.length < 2) return false;
                if(!RTSPermissions.canCheckStats(sender)) return true;
                    rs = dbManager.getHandledBy(args[1]);
                    int currentHeld = 0;
                    int currentClaimed = 0;
                    int totalCompleted = 0;
                    while(rs.next()){
                        if(rs.getInt("status") == 1) currentClaimed++;
                        if(rs.getInt("status") == 2) currentHeld++;
                        if(rs.getInt("status") == 3) totalCompleted++;
                    }
                    rs.close();
                    sender.sendMessage(ChatColor.YELLOW + "---- Stats for " + args[1] + " ----");
                    sender.sendMessage(ChatColor.YELLOW + "Currently claimed requests: " + currentClaimed);
                    sender.sendMessage(ChatColor.YELLOW + "Currently held requests: " + currentHeld);
                    sender.sendMessage(ChatColor.YELLOW + "Total completed requests: " + totalCompleted);
                break;

            case "SEARCH":
            case "FIND":
                if(args.length < 3) return false;
                if(!RTSPermissions.canCheckStats(sender)) return true;
                String action = args[2];
                if(!action.equalsIgnoreCase("completed") && !action.equalsIgnoreCase("created")) return false;
                String player = args[1];
                int pageNumber = 1;

                if(action.equalsIgnoreCase("completed")){
                    if(args.length == 4) pageNumber =  Integer.parseInt(args[3]);
                    ResultSet result = dbManager.getLimitedHandledBy(player, (pageNumber * plugin.requestsPerPage) - plugin.requestsPerPage, plugin.requestsPerPage);
                    sender.sendMessage(ChatColor.AQUA + "------ Page " + pageNumber + " - " + ChatColor.YELLOW + " Completed by " + player + ChatColor.AQUA + " ------");
                    String substring;
                    SimpleDateFormat sdf  = new SimpleDateFormat("MMM.dd kk:mm z");
                    String date;
                    if(plugin.storageType.equalsIgnoreCase("mysql")) result.beforeFirst();
                    while(result.next()){
                        substring = RTSFunctions.shortenMessage(result.getString("text"));
                        date = sdf.format(new java.util.Date(result.getLong("tstamp") * 1000));
                        ChatColor online = (RTSFunctions.isUserOnline((UUID) result.getObject("uuid"))) ? ChatColor.GREEN : ChatColor.RED;
                        sender.sendMessage(ChatColor.GOLD + "#" + result.getInt(1) + " " + date + " by " + online + result.getString("name") + ChatColor.GOLD + " - " + ChatColor.GRAY + substring);
                    }
                    result.close();
                }

                if(action.equalsIgnoreCase("created")){
                    if(args.length == 4) pageNumber =  Integer.parseInt(args[3]);
                    ResultSet result = dbManager.getLimitedCreatedBy(player, (pageNumber * plugin.requestsPerPage) - plugin.requestsPerPage, plugin.requestsPerPage);
                    sender.sendMessage(ChatColor.AQUA + "------ Page " + pageNumber + " - " + ChatColor.YELLOW + " Created by " + player + ChatColor.AQUA + " ------");
                    String substring;
                    SimpleDateFormat sdf  = new SimpleDateFormat("MMM.dd kk:mm z");
                    String date;
                    if(plugin.storageType.equalsIgnoreCase("mysql")) result.beforeFirst();
                    while(result.next()){
                        substring = RTSFunctions.shortenMessage(result.getString("text"));
                        date = sdf.format(new java.util.Date(result.getLong("tstamp") * 1000));
                        ChatColor online = (RTSFunctions.isUserOnline((UUID) result.getObject("uuid"))) ? ChatColor.GREEN : ChatColor.RED;
                        sender.sendMessage(ChatColor.GOLD + "#" + result.getInt(1) + " " + date + " by " + online + result.getString("name") + ChatColor.GOLD + " - " + ChatColor.GRAY + substring);
                    }
                    result.close();
                }
                break;

            case "HELP":
                if(!RTSPermissions.canSeeHelpPage(sender)) return true;
                sender.sendMessage(ChatColor.GREEN + "====[ " + ChatColor.GOLD + "ReportRTS Help " + ChatColor.GREEN + "]====");
                sender.sendMessage(ChatColor.RED + "/check " + ChatColor.GOLD + ChatColor.BOLD + " [STATUS] [ID/PAGE]" + ChatColor.RESET + ChatColor.YELLOW + " - See request details");
                sender.sendMessage(ChatColor.RED + "/claim " + ChatColor.GOLD + ChatColor.BOLD + " [ID]" + ChatColor.RESET + ChatColor.YELLOW + " - Claim request, stops toe stepping");
                sender.sendMessage(ChatColor.RED + "/complete " + ChatColor.GOLD + ChatColor.BOLD + " [ID] [COMMENT]" + ChatColor.RESET + ChatColor.YELLOW + " - Mark request as complete");
                sender.sendMessage(ChatColor.RED + "/hold " + ChatColor.GOLD + ChatColor.BOLD + " [ID] [COMMENT]" + ChatColor.RESET + ChatColor.YELLOW + " - Put request on hold");
                sender.sendMessage(ChatColor.RED + "/modreq " + ChatColor.GOLD + ChatColor.BOLD + " [MESSAGE]" + ChatColor.RESET + ChatColor.YELLOW + " - File a request");
                sender.sendMessage(ChatColor.RED + "/modlist " + ChatColor.YELLOW + " - See online staff");
                sender.sendMessage(ChatColor.RED + "/mod-broadcast " + ChatColor.GOLD + ChatColor.BOLD + " [MESSAGE]" + ChatColor.RESET + ChatColor.YELLOW + " - Send a message to all online staff");
                sender.sendMessage(ChatColor.RED + "/reportrts " + ChatColor.GOLD + ChatColor.BOLD + " [ACTION]" + ChatColor.RESET + ChatColor.YELLOW + " - General command for ReportRTS");
                sender.sendMessage(ChatColor.RED + "/reopen " + ChatColor.GOLD + ChatColor.BOLD + " [ID]" + ChatColor.RESET + ChatColor.YELLOW + " - Reopen a held or closed request");
                sender.sendMessage(ChatColor.RED + "/tp-id " + ChatColor.GOLD + ChatColor.BOLD + " [ID]" + ChatColor.RESET + ChatColor.YELLOW + " - Teleports to specified request");
                sender.sendMessage(ChatColor.RED + "/unclaim " + ChatColor.GOLD + ChatColor.BOLD + " [ID]" + ChatColor.RESET + ChatColor.YELLOW + " - Unclaim request");
                break;

            case "NOTIFICATIONS":
                if(!RTSPermissions.canManageNotifications(sender)) return true;
                if(args.length <= 1){
                    sender.sendMessage(ChatColor.YELLOW + "There are currently " +  plugin.notificationMap.size() + " players left to notify.");
                    sender.sendMessage("Reset them using /reportrts notifications reset");
                    return true;
                }
                if(!args[1].equalsIgnoreCase("reset")){
                    sender.sendMessage(ChatColor.RED + "Syntax is /reportrts notifications reset");
                    return true;
                }
                DatabaseManager.getConnection().createStatement().executeUpdate("UPDATE `" + plugin.storagePrefix + "reportrts_request` SET `notified_of_completion` = 1 WHERE `notified_of_completion` = 0");
                plugin.notificationMap.clear();
                sender.sendMessage(ChatColor.GREEN + "Notifications have been reset.");
                break;

            case "DUTY":
                if(!(sender instanceof Player)){
                    sender.sendMessage("[ReportRTS] You cannot change your duty status from the console.");
                    return true;
                }
                Player player1 = (Player) sender;
                if(!RTSPermissions.isModerator((Player)sender)) return true;
                if(args.length <= 1){
                    if(plugin.moderatorMap.contains(player1.getUniqueId()))
                        sender.sendMessage(ChatColor.GREEN + "You are currently on duty.");
                    else
                        sender.sendMessage(ChatColor.RED + "You are currently off duty.");
                    return true;
                }
                String duty = args[1];
                if(!duty.equalsIgnoreCase("on") && !duty.equalsIgnoreCase("off")){
                    sender.sendMessage(ChatColor.RED + "Syntax is /reportrts duty on|off");
                    return true;
                }
                if(duty.equalsIgnoreCase("on")){
                    if(!plugin.moderatorMap.contains(player1.getUniqueId())) plugin.moderatorMap.add(player1.getUniqueId());
                    sender.sendMessage(ChatColor.YELLOW + "You are now on duty.");
                }else{
                    if(plugin.moderatorMap.contains(player1.getUniqueId())) plugin.moderatorMap.remove(player1.getUniqueId());
                    sender.sendMessage(ChatColor.YELLOW + "You are now off duty.");
                }
                break;

            case "SETUP":
                if(args.length <= 1){
                    sender.sendMessage(ChatColor.RED + "Missing argument! Arguments: HOSTNAME, PORT, DATABASE, USERNAME, PASSWORD, PREFIX, REFRESH");
                    return true;
                }
                if(args[1].equalsIgnoreCase("HOSTNAME")){
                    if(args.length < 3 || args[2] == null || args[2].isEmpty()){
                        sender.sendMessage(ChatColor.RED + "Hostname cannot be empty or null! Default is localhost or 127.0.0.1");
                        return true;
                    }
                    plugin.getConfig().set("storage.hostname", args[2]);
                    plugin.saveConfig();
                    plugin.reloadConfig();
                    this.storageHostname = true;
                    sender.sendMessage(ChatColor.GREEN + "Hostname set to " + args[2] + ", you should configure PORT next.");

                    if(storageHostname && storagePort && storageDatabase && storageUsername && storagePassword && storageRefresh){
                        plugin.setupDone = true;
                        plugin.reloadSettings();
                        if(DatabaseManager.load()){
                            sender.sendMessage(ChatColor.GREEN + "ReportRTS should be set up now! Restart for the plugin to work correctly.");
                            plugin.getServer().getPluginManager().disablePlugin(plugin);
                        }
                    }
                    return true;
                }
                if(args[1].equalsIgnoreCase("PORT")){
                    if(args.length < 3 || args[2] == null || !StringUtils.isNumeric(args[2])){
                        sender.sendMessage(ChatColor.RED + "Port can only contain numbers and may not be left blank! Default is 3306.");
                        return true;
                    }
                    plugin.getConfig().set("storage.port", args[2]);
                    plugin.saveConfig();
                    plugin.reloadConfig();
                    this.storagePort = true;
                    sender.sendMessage(ChatColor.GREEN + "Port set to " + args[2] + ", you should configure DATABASE next.");

                    if(storageHostname && storagePort && storageDatabase && storageUsername && storagePassword && storageRefresh){
                        plugin.setupDone = true;
                        plugin.reloadSettings();
                        if(DatabaseManager.load()){
                            sender.sendMessage(ChatColor.GREEN + "ReportRTS should be set up now! Restart for the plugin to work correctly.");
                            plugin.getServer().getPluginManager().disablePlugin(plugin);
                        }
                    }
                    return true;
                }
                if(args[1].equalsIgnoreCase("DATABASE")){
                    if(args.length < 3 || args[2] == null || args[2].isEmpty()){
                        sender.sendMessage(ChatColor.RED + "Database cannot be empty or null!");
                        return true;
                    }
                    plugin.getConfig().set("storage.database", args[2]);
                    plugin.saveConfig();
                    plugin.reloadConfig();
                    this.storageDatabase = true;
                    sender.sendMessage(ChatColor.GREEN + "Database set to " + args[2] + ", you should configure USERNAME next.");

                    if(storageHostname && storagePort && storageDatabase && storageUsername && storagePassword && storageRefresh){
                        plugin.setupDone = true;
                        plugin.reloadSettings();
                        if(DatabaseManager.load()){
                            sender.sendMessage(ChatColor.GREEN + "ReportRTS should be set up now! Restart for the plugin to work correctly.");
                            plugin.getServer().getPluginManager().disablePlugin(plugin);
                        }
                    }
                    return true;
                }
                if(args[1].equalsIgnoreCase("USERNAME")){
                    if(args.length < 3 || args[2] == null || args[2].isEmpty()){
                        sender.sendMessage(ChatColor.RED + "Username cannot be empty or null!");
                        return true;
                    }
                    plugin.getConfig().set("storage.username", args[2]);
                    plugin.saveConfig();
                    plugin.reloadConfig();
                    this.storageUsername = true;
                    sender.sendMessage(ChatColor.GREEN + "Username set to " + args[2] + ", you should configure PASSWORD next.");

                    if(storageHostname && storagePort && storageDatabase && storageUsername && storagePassword && storageRefresh){
                        plugin.setupDone = true;
                        plugin.reloadSettings();
                        if(DatabaseManager.load()){
                            sender.sendMessage(ChatColor.GREEN + "ReportRTS should be set up now! Restart for the plugin to work correctly.");
                            plugin.getServer().getPluginManager().disablePlugin(plugin);
                        }
                    }
                    return true;
                }
                if(args[1].equalsIgnoreCase("PASSWORD")){
                    if(args.length < 3 || args[2] == null || args[2].isEmpty()){
                        sender.sendMessage(ChatColor.RED + "Password cannot be empty or null!");
                        return true;
                    }
                    plugin.getConfig().set("storage.password", args[2]);
                    plugin.saveConfig();
                    plugin.reloadConfig();
                    this.storagePassword = true;
                    sender.sendMessage(ChatColor.GREEN + "Password set to " + args[2] + ", next up is PREFIX. Prefix is optional, if you want to have no prefix, do NOT configure it! You may skip PREFIX and jump straight to REFRESH.");

                    if(storageHostname && storagePort && storageDatabase && storageUsername && storagePassword && storageRefresh){
                        plugin.setupDone = true;
                        plugin.reloadSettings();
                        if(DatabaseManager.load()){
                            sender.sendMessage(ChatColor.GREEN + "ReportRTS should be set up now! Restart for the plugin to work correctly.");
                            plugin.getServer().getPluginManager().disablePlugin(plugin);
                        }
                    }
                    return true;
                }
                if(args[1].equalsIgnoreCase("PREFIX")){
                    if(args.length < 3 || args[2] == null || args[2].isEmpty()){
                        plugin.getConfig().set("storage.prefix", "");
                    }else{
                        plugin.getConfig().set("storage.prefix", args[2]);
                    }
                    plugin.saveConfig();
                    plugin.reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "Prefix set to " + args[2] + ", you should configure REFRESH next.");

                    if(storageHostname && storagePort && storageDatabase && storageUsername && storagePassword && storageRefresh){
                        plugin.setupDone = true;
                        plugin.reloadSettings();
                        if(DatabaseManager.load()){
                            sender.sendMessage(ChatColor.GREEN + "ReportRTS should be set up now! Restart for the plugin to work correctly.");
                            plugin.getServer().getPluginManager().disablePlugin(plugin);
                        }
                    }
                    return true;
                }
                if(args[1].equalsIgnoreCase("REFRESH")){
                    if(args.length < 3 || args[2] == null || !StringUtils.isNumeric(args[2])){
                        sender.sendMessage(ChatColor.RED + "Refresh time can only contain numbers and may not be left blank! Default is 600 seconds.");
                        return true;
                    }
                    plugin.getConfig().set("storage.refreshTime", args[2]);
                    plugin.saveConfig();
                    plugin.reloadConfig();
                    this.storageRefresh = true;
                    sender.sendMessage(ChatColor.GREEN + "Refresh set to " + args[2] + ", if you have followed the instructions you should now be done!");

                    if(storageHostname && storagePort && storageDatabase && storageUsername && storagePassword && storageRefresh){
                        plugin.setupDone = true;
                        plugin.reloadSettings();
                        if(DatabaseManager.load()){
                            sender.sendMessage(ChatColor.GREEN + "ReportRTS should be set up now! Restart for the plugin to work correctly.");
                            plugin.getServer().getPluginManager().disablePlugin(plugin);
                        }
                    }
                    return true;
                }

                sender.sendMessage(ChatColor.RED + "Wrong argument! Valid arguments: HOSTNAME, PORT, DATABASE, USERNAME, PASSWORD, PREFIX, REFRESH");
                break;

            default:
                return false;
            }
        }catch(SQLException | IOException e){
            return false;
        }
        return true;
    }
}
