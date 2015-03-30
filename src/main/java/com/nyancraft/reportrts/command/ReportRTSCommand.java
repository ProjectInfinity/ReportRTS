package com.nyancraft.reportrts.command;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

import com.nyancraft.reportrts.data.Ticket;
import com.nyancraft.reportrts.data.User;
import com.nyancraft.reportrts.persistence.DataProvider;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.util.Message;
import com.nyancraft.reportrts.util.BungeeCord;
import org.bukkit.entity.Player;

public class ReportRTSCommand implements CommandExecutor{

    private ReportRTS plugin;
    private DataProvider data;

    private boolean storageHostname = false;
    private boolean storagePort = false;
    private boolean storageDatabase = false;
    private boolean storageUsername = false;
    private boolean storagePassword = false;
    private boolean storageRefresh = false;

    public ReportRTSCommand(ReportRTS plugin) {
        this.plugin = plugin;
        this.data = plugin.getDataProvider();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0) return false;
        switch(args[0].toUpperCase()) {

            case "RELOAD":
                if(!RTSPermissions.canReloadPlugin(sender)) return true;
                plugin.reloadPlugin();
                sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] Reloaded configuration and requests.");
                break;

            case "BAN":

                if(!RTSPermissions.canBanUser(sender)) return true;

                if(args.length < 2) {
                    sender.sendMessage(Message.errorUserNotSpecified());
                    return true;
                }

                // Attempt to get the target that you wish to ban.
                Player target = plugin.getServer().getPlayer(args[1]);

                if(target == null) {

                    UUID uuid = null;

                    // Target is not online, let's attempt to find them by open tickets.
                    for(Map.Entry<Integer, Ticket> entry : plugin.tickets.entrySet()) {

                        if(!entry.getValue().getName().equalsIgnoreCase(args[1])) continue;

                        uuid = entry.getValue().getUUID();
                        break;

                    }

                    // User did not have any open tickets, we have to get him/her from the data-provider.
                    if(uuid == null) {

                        // LAST RESORT ONLY!
                        User user = data.getUnsafeUser(args[1]);

                        // User doesn't actually exist.
                        if(user == null) {
                            sender.sendMessage(Message.errorUserNotExists(args[1]));
                            return true;
                        }

                        if(data.setUserStatus(user.getUuid(), true) < 1) {
                            sender.sendMessage(Message.errorBanUser(args[1]));
                            return true;
                        }

                    } else {

                        // We found the data using open tickets.
                        if(data.setUserStatus(uuid, true) < 1) {
                            sender.sendMessage(Message.errorBanUser(args[1]));
                            return true;
                        }

                    }

                } else {

                    // Target is online.
                    if(data.setUserStatus(target.getUniqueId(), true) < 1) {
                        sender.sendMessage(Message.errorBanUser(target.getName()));
                        return true;
                    }

                }

                RTSFunctions.messageStaff(Message.banUser(sender.getName(), args[1]), false);
                try {
                    BungeeCord.globalNotify(Message.banUser(sender.getName(), args[1]), -1, NotificationType.NOTIFYONLY);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;

            case "UNBAN":

                if(!RTSPermissions.canBanUser(sender)) return true;

                if(args.length < 2) {
                    sender.sendMessage(Message.errorUserNotSpecified());
                    return true;
                }

                // Attempt to get the target that you wish to un-ban.
                Player target1 = plugin.getServer().getPlayer(args[1]);

                if(target1 == null) {

                    UUID uuid = null;

                    // Target is not online, let's attempt to find them by open tickets.
                    for(Map.Entry<Integer, Ticket> entry : plugin.tickets.entrySet()) {

                        if(!entry.getValue().getName().equalsIgnoreCase(args[1])) continue;

                        uuid = entry.getValue().getUUID();
                        break;

                    }

                    // User did not have any open tickets, we have to get him/her from the data-provider.
                    if(uuid == null) {

                        // LAST RESORT ONLY!
                        User user = data.getUnsafeUser(args[1]);

                        // User doesn't actually exist.
                        if(user == null) {
                            sender.sendMessage(Message.errorUserNotExists(args[1]));
                            return true;
                        }

                        if(data.setUserStatus(user.getUuid(), false) < 1) {
                            sender.sendMessage(Message.errorUnbanUser(args[1]));
                            return true;
                        }

                    } else {

                        // We found the data using open tickets.
                        if(data.setUserStatus(uuid, false) < 1) {
                            sender.sendMessage(Message.errorUnbanUser(args[1]));
                            return true;
                        }

                    }

                } else {

                    // Target is online.
                    if(data.setUserStatus(target1.getUniqueId(), false) < 1) {
                        sender.sendMessage(Message.errorUnbanUser(target1.getName()));
                        return true;
                    }

                }

                RTSFunctions.messageStaff(Message.banRemove(sender.getName(), args[1]), false);
                try {
                    BungeeCord.globalNotify(Message.banRemove(sender.getName(), args[1]), -1, NotificationType.NOTIFYONLY);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "RESET":
                if(!RTSPermissions.canResetPlugin(sender)) return true;

                data.reset();
                // Reload the plugin after resetting the data-provider.
                plugin.reloadPlugin();

                sender.sendMessage(ChatColor.GOLD + "[ReportRTS] You deleted all users and tickets from ReportRTS.");
                plugin.getLogger().log(Level.INFO, sender.getName() + " deleted all users and tickets from ReportRTS!");

                break;

            case "STATS":
                if(!RTSPermissions.canCheckStats(sender)) return true;

                // Make sure map is sorted by insert order.

                LinkedHashMap<String, Integer> result = data.getTop(10);

                if(result == null) {
                    sender.sendMessage(Message.error("No results!"));
                    return true;
                }

                sender.sendMessage(ChatColor.YELLOW + "---- Top 10 ----");
                sender.sendMessage(ChatColor.YELLOW + "<Player> : <Resolved Tickets>");

                for(Map.Entry<String, Integer> entry : result.entrySet()) {
                    sender.sendMessage(ChatColor.YELLOW + entry.getKey() + " : " + entry.getValue().toString());
                }

                break;

            case "SEARCH":
            case "FIND":

                if(!RTSPermissions.canCheckStats(sender)) return true;

                if(args.length < 3) return false;

                String action = args[2];
                // Check if the provided action is valid.
                if(!action.equalsIgnoreCase("closed") && !action.equalsIgnoreCase("opened")) return false;

                int pageNumber = 1;
                // If a page has been specified, use that instead of the default 1.
                if(args.length == 4 && RTSFunctions.isNumber(args[3])) pageNumber =  Integer.parseInt(args[3]);
                // Also ensure that pageNumber is 1 or higher.
                if(pageNumber < 1) pageNumber = 1;
                // Start position of query.
                int cursor = (pageNumber * plugin.ticketsPerPage) - plugin.ticketsPerPage;

                User user = data.getUnsafeUser(args[1]);

                if(user == null) {
                    sender.sendMessage(Message.errorUserNotExists(args[1]));
                    return true;
                }

                // Store map as null so we can check it later.
                LinkedHashMap<Integer, Ticket> tickets = null;

                if(action.equalsIgnoreCase("closed")) {

                    tickets = data.getHandledBy(user.getUuid(), cursor, plugin.ticketsPerPage);

                } else if(action.equalsIgnoreCase("opened")) {

                    tickets = data.getOpenedBy(user.getUuid(), cursor, plugin.ticketsPerPage);

                }

                // Tickets should only be null if the player does not exist.
                if(tickets == null) {
                    sender.sendMessage(Message.errorUserNotExists(args[1]));
                    return true;
                }

                SimpleDateFormat sdf  = new SimpleDateFormat("MMM.dd kk:mm z");

                // Send header.
                sender.sendMessage(ChatColor.AQUA + "------ Page " + pageNumber + " - " + ChatColor.YELLOW + " " +
                        (action.equalsIgnoreCase("closed") ? "Handled" : "Opened") + " by " + user.getUsername() + ChatColor.AQUA + " ------");

                for(Map.Entry<Integer, Ticket> entry : tickets.entrySet()) {

                    Ticket ticket = entry.getValue();

                    // Is the player online?
                    ChatColor online = RTSFunctions.isUserOnline(user.getUuid()) ? ChatColor.GREEN : ChatColor.RED;
                    // Send body.
                    sender.sendMessage(ChatColor.GOLD + "#" + ticket.getId() + " " + sdf.format(new java.util.Date(ticket.getTimestamp() * 1000)) +
                    " by " + online + ticket.getName() + ChatColor.GOLD + " - " + ChatColor.GRAY + RTSFunctions.shortenMessage(ticket.getMessage()));

                }

                break;

            case "HELP":
                if(!RTSPermissions.canSeeHelpPage(sender)) return true;
                sender.sendMessage(ChatColor.GREEN + "====[ " + ChatColor.GOLD + "ReportRTS Help " + ChatColor.GREEN + "]====");
                sender.sendMessage(ChatColor.RED + (plugin.legacyCommands ? "/" + plugin.commandMap.get("readTicket") : "/ticket " + plugin.commandMap.get("readTicket")) + ChatColor.GOLD + ChatColor.BOLD + " [STATUS] [ID/PAGE]" + ChatColor.RESET + ChatColor.YELLOW + " - See ticket details");
                sender.sendMessage(ChatColor.RED + (plugin.legacyCommands ? "/" + plugin.commandMap.get("claimTicket") : "/ticket " + plugin.commandMap.get("claimTicket")) + ChatColor.GOLD + ChatColor.BOLD + " [ID]" + ChatColor.RESET + ChatColor.YELLOW + " - Claim ticket, stops toe stepping");
                sender.sendMessage(ChatColor.RED + (plugin.legacyCommands ? "/" + plugin.commandMap.get("closeTicket") : "/ticket " + plugin.commandMap.get("closeTicket")) + ChatColor.GOLD + ChatColor.BOLD + " [ID] [COMMENT]" + ChatColor.RESET + ChatColor.YELLOW + " - Close ticket");
                sender.sendMessage(ChatColor.RED + (plugin.legacyCommands ? "/" + plugin.commandMap.get("holdTicket") : "/ticket " + plugin.commandMap.get("holdTicket")) + ChatColor.GOLD + ChatColor.BOLD + " [ID] [COMMENT]" + ChatColor.RESET + ChatColor.YELLOW + " - Put ticket on hold");
                sender.sendMessage(ChatColor.RED + (plugin.legacyCommands ? "/" + plugin.commandMap.get("openTicket") : "/ticket " + plugin.commandMap.get("openTicket")) + ChatColor.GOLD + ChatColor.BOLD + " [MESSAGE]" + ChatColor.RESET + ChatColor.YELLOW + " - Opens a ticket");
                sender.sendMessage(ChatColor.RED + (plugin.legacyCommands ? "/" + plugin.commandMap.get("listStaff") : "/ticket " + plugin.commandMap.get("listStaff")) + ChatColor.YELLOW + " - See online staff");
                sender.sendMessage(ChatColor.RED + (plugin.legacyCommands ? "/" + plugin.commandMap.get("broadcastToStaff") : "/ticket " + plugin.commandMap.get("broadcastToStaff")) + ChatColor.GOLD + ChatColor.BOLD + " [MESSAGE]" + ChatColor.RESET + ChatColor.YELLOW + " - Send a message to all online staff");
                sender.sendMessage(ChatColor.RED + "/reportrts " + ChatColor.GOLD + ChatColor.BOLD + " [ACTION]" + ChatColor.RESET + ChatColor.YELLOW + " - General command for ReportRTS");
                sender.sendMessage(ChatColor.RED + (plugin.legacyCommands ? "/" + plugin.commandMap.get("reopenTicket") : "/ticket " + plugin.commandMap.get("reopenTicket")) + ChatColor.GOLD + ChatColor.BOLD + " [ID]" + ChatColor.RESET + ChatColor.YELLOW + " - Reopens a held or closed ticket");
                sender.sendMessage(ChatColor.RED + (plugin.legacyCommands ? "/" + plugin.commandMap.get("teleportToTicket") : "/ticket " + plugin.commandMap.get("teleportToTicket")) + ChatColor.GOLD + ChatColor.BOLD + " [ID]" + ChatColor.RESET + ChatColor.YELLOW + " - Teleport to specified ticket");
                sender.sendMessage(ChatColor.RED + (plugin.legacyCommands ? "/" + plugin.commandMap.get("unclaimTicket") : "/ticket " + plugin.commandMap.get("unclaimTicket")) + ChatColor.GOLD + ChatColor.BOLD + " [ID]" + ChatColor.RESET + ChatColor.YELLOW + " - Unclaim ticket");
                sender.sendMessage(ChatColor.RED + (plugin.legacyCommands ? "/" + plugin.commandMap.get("commentTicket") : "/ticket " + plugin.commandMap.get("commentTicket")) + ChatColor.GOLD + ChatColor.BOLD + " [ID] [COMMENT]" + ChatColor.RESET + ChatColor.YELLOW + " - Comments on a ticket");
                break;

            case "NOTIFICATIONS":
                if(!RTSPermissions.canManageNotifications(sender)) return true;
                if(args.length <= 1){
                    sender.sendMessage(ChatColor.YELLOW + "There are currently " +  plugin.notifications.size() + " players left to notify.");
                    sender.sendMessage("Reset them using /reportrts notifications reset");
                    return true;
                }
                if(!args[1].equalsIgnoreCase("reset")){
                    sender.sendMessage(ChatColor.RED + "Syntax is /reportrts notifications reset");
                    return true;
                }

                if(!data.resetNotifications()) {
                    sender.sendMessage(ChatColor.RED + "Notifications did not reset correctly!");
                    return true;
                }

                plugin.notifications.clear();
                sender.sendMessage(ChatColor.GREEN + "Notifications have been reset.");
                break;

            case "DUTY":
                if(!(sender instanceof Player)){
                    sender.sendMessage("[ReportRTS] You cannot change your duty status from the console.");
                    return true;
                }
                Player player1 = (Player) sender;
                if(!RTSPermissions.isStaff((Player) sender)) return true;
                if(args.length <= 1){
                    if(plugin.staff.contains(player1.getUniqueId()))
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
                    if(!plugin.staff.contains(player1.getUniqueId())) plugin.staff.add(player1.getUniqueId());
                    sender.sendMessage(ChatColor.YELLOW + "You are now on duty.");
                }else{
                    if(plugin.staff.contains(player1.getUniqueId())) plugin.staff.remove(player1.getUniqueId());
                    sender.sendMessage(ChatColor.YELLOW + "You are now off duty.");
                }
                break;

            case "SETUP":
                if(!sender.isOp()) return false;
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
                        if(data.load()){
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
                        if(data.load()){
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
                        if(data.load()){
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
                        if(data.load()){
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
                        if(data.load()){
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
                        if(data.load()){
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
                        if(data.load()){
                            sender.sendMessage(ChatColor.GREEN + "ReportRTS should be set up now! Restart for the plugin to work correctly.");
                            plugin.getServer().getPluginManager().disablePlugin(plugin);
                        }
                    }
                    return true;
                }

                sender.sendMessage(ChatColor.RED + "Wrong argument! Valid arguments: HOSTNAME, PORT, DATABASE, USERNAME, PASSWORD, PREFIX, REFRESH");
                break;

            case "BANLIST":

                if(!RTSPermissions.canBanUser(sender)) return true;

                ArrayList<User> users = data.getUsers(true);

                if(users == null || users.size() == 0) {
                    sender.sendMessage(Message.error("There are no banned users."));
                    return true;
                }

                sender.sendMessage(ChatColor.YELLOW + "Listing banned users (" + users.size() + " total):");

                Iterator it = users.iterator();

                int i = 0;
                while(it.hasNext()) {

                    User user1 = (User) it.next();

                    i++;

                    sender.sendMessage(ChatColor.GREEN + "" + i + ": " + ChatColor.BOLD + ChatColor.AQUA + user1.getUsername() + ChatColor.GRAY + " - " + user1.getUuid().toString());

                }

                break;

            default:
                return false;
        }
        return true;
    }
}