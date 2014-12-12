package com.nyancraft.reportrts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.nyancraft.reportrts.data.Ticket;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.event.player.PlayerQuitEvent;

public class RTSListener implements Listener{
    private final ReportRTS plugin;

    public RTSListener(ReportRTS plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event){
        BungeeCord.triggerAutoSync();
        BungeeCord.processPendingRequests();
        if(BungeeCord.getServer().equals("")){
            Bukkit.getScheduler().runTaskLaterAsynchronously(ReportRTS.getPlugin(), new Runnable(){
                public void run(){
                    BungeeCord.getServer();
                }
            }, 60L);
        }

        if(plugin.notificationMap.size() > 0){
            Map<Integer, UUID> keys = new HashMap<>();
            for(Map.Entry<Integer, UUID> entry : plugin.notificationMap.entrySet()){
                if(entry.getValue().equals(event.getPlayer().getUniqueId())){
                    new LoginTask(plugin, event.getPlayer().getUniqueId(), entry).runTaskLater(plugin, 100L);
                    keys.put(entry.getKey(), entry.getValue());
                }
            }
            if(keys.size() >= 2){
                event.getPlayer().sendMessage(Message.parse("completedReqMulti", keys.size(), (plugin.legacyCommands ? plugin.commandMap.get("readTicket") + " self" : "ticket " + plugin.commandMap.get("readTicket") + " self")));
                for(Map.Entry<Integer, UUID> entry : keys.entrySet()){
                    new LoginTask(plugin, event.getPlayer().getUniqueId(), entry).runTaskLater(plugin, 100L);
                }
            }else{
                for(Map.Entry<Integer, UUID> entry : keys.entrySet()){
                    new LoginTask(plugin, event.getPlayer().getUniqueId(), entry).runTaskLater(plugin, 100L);
                }
            }

            for(int key : keys.keySet()) plugin.notificationMap.remove(key);

            if(!plugin.teleportMap.isEmpty()){
                Integer g = plugin.teleportMap.get(event.getPlayer().getUniqueId());
                if(g != null){
                    event.getPlayer().sendMessage(Message.parse("teleportedUser", "/" + (plugin.legacyCommands ? plugin.commandMap.get("teleportToTicket") : "ticket " + plugin.commandMap.get("teleportToTicket")) + " " + Integer.toString(g)));
                    Bukkit.dispatchCommand(event.getPlayer(), "ticket " + plugin.commandMap.get("teleportToTicket") + " " + Integer.toString(g));
                    plugin.teleportMap.remove(event.getPlayer().getUniqueId());
                }
            }
        }

        if(!RTSPermissions.isStaff(event.getPlayer())) return;

        if(!plugin.moderatorMap.contains(event.getPlayer().getUniqueId())) plugin.moderatorMap.add(event.getPlayer().getUniqueId());

        int openRequests = plugin.requestMap.size();

        if(openRequests < 1 && !plugin.hideNotification)
            event.getPlayer().sendMessage(Message.parse("generalNoRequests"));

        if(openRequests > 0)
            event.getPlayer().sendMessage(Message.parse("generalOpenRequests", openRequests, (plugin.legacyCommands ? plugin.commandMap.get("readTicket") : "ticket " + plugin.commandMap.get("readTicket"))));

        if(event.getPlayer().isOp()){
            if(plugin.outdated) event.getPlayer().sendMessage(Message.parse("outdatedPlugin", plugin.versionString));
            if(!plugin.setupDone) event.getPlayer().sendMessage(Message.parse("generalSetupNotDone"));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event){

        Block block = event.getBlock();
        if(!(block.getState() instanceof Sign)) return;
        if(!event.getLine(0).equalsIgnoreCase("[help]")) return;

        if(!RTSPermissions.canFileRequest(event.getPlayer())) {
            block.breakNaturally();
            return;
        }
        String[] text = new String[3]; System.arraycopy(event.getLines(), 1, text, 0, 3);
        String message = RTSFunctions.cleanUpSign(text);
        if(message.length() == 0) {
            event.getPlayer().sendMessage(Message.parse("generalInternalError", "Help signs can't be empty."));
            block.breakNaturally();
            return;
        }
        if(RTSFunctions.getOpenRequestsByUser(event.getPlayer().getUniqueId()) >= plugin.maxRequests && !(ReportRTS.permission != null ? ReportRTS.permission.has(event.getPlayer(), "reportrts.command.modreq.unlimited") : event.getPlayer().hasPermission("reportrts.command.modreq.unlimited"))){
            event.getPlayer().sendMessage(Message.parse("modreqTooManyOpen"));
            block.breakNaturally();
            return;
        }
        int userId = DatabaseManager.getDatabase().getUserId(event.getPlayer().getName(), event.getPlayer().getUniqueId(), true);
        if(DatabaseManager.getDatabase().fileRequest(event.getPlayer().getName(), event.getPlayer().getWorld().getName(), event.getPlayer().getLocation(), message, userId)){
            int ticketId = DatabaseManager.getDatabase().getLatestTicketIdByUser(userId);
            plugin.requestMap.put(ticketId, new Ticket(event.getPlayer().getName(), event.getPlayer().getUniqueId(), ticketId, System.currentTimeMillis()/1000, message, 0, event.getPlayer().getLocation().getBlockX(), event.getPlayer().getLocation().getBlockY(), event.getPlayer().getLocation().getBlockZ(), event.getPlayer().getLocation().getYaw(), event.getPlayer().getLocation().getPitch(), event.getPlayer().getWorld().getName(), BungeeCord.getServer(), ""));
            event.getPlayer().sendMessage(Message.parse("modreqFiledUser"));
            try{
                BungeeCord.globalNotify(Message.parse("modreqFiledMod", event.getPlayer().getName(), ticketId), ticketId, NotificationType.NEW);
            }catch(IOException e){
                e.printStackTrace();
            }
            RTSFunctions.messageMods(Message.parse("modreqFiledMod", event.getPlayer().getName(), String.valueOf(ticketId)), true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event){
        BungeeCord.triggerAutoSync();
        if(plugin.moderatorMap.contains(event.getPlayer().getUniqueId())) plugin.moderatorMap.remove(event.getPlayer().getUniqueId());
    }
}