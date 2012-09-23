package com.nyancraft.reportrts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.data.HelpRequest;
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
        if(plugin.notificationMap.size() > 0){
            ArrayList<Integer> keys = new ArrayList();
            for(Map.Entry<Integer, String> entry : plugin.notificationMap.entrySet()){

                if(entry.getValue().equals(event.getPlayer().getName())){
                    ResultSet rs = DatabaseManager.getDatabase().getTicketById(entry.getKey());
                    try{
                        if(plugin.useMySQL) rs.first();
                        event.getPlayer().sendMessage(Message.parse("completedUserOffline"));
                        String comment = rs.getString("mod_comment");
                        if(comment == null) comment = "";
                        event.getPlayer().sendMessage(Message.parse("completedText", rs.getString("text"), comment));
                        rs.close();
                        if(!DatabaseManager.getDatabase().setNotificationStatus(entry.getKey(), 1)) plugin.getLogger().warning("Unable to set notification status to 1.");
                        keys.add(entry.getKey());
                    }catch(SQLException e){
                        e.printStackTrace();
                    }
                }
            }
            for(int key : keys) plugin.notificationMap.remove(key);
        }

        if(!RTSPermissions.isModerator(event.getPlayer())) return;

        if(!plugin.moderatorMap.contains(event.getPlayer().getName())) plugin.moderatorMap.add(event.getPlayer().getName());

        int openRequests = plugin.requestMap.size();

        if(openRequests < 1 && !plugin.hideNotification)
            event.getPlayer().sendMessage(Message.parse("generalNoRequests"));

        if(openRequests > 0)
            event.getPlayer().sendMessage(Message.parse("generalOpenRequests", openRequests));

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
        if(RTSFunctions.getOpenRequestsByUser(event.getPlayer()) >= plugin.maxRequests && !(ReportRTS.permission != null ? ReportRTS.permission.has(event.getPlayer(), "reportrts.command.modreq.unlimited") : event.getPlayer().hasPermission("reportrts.command.modreq.unlimited"))){
            event.getPlayer().sendMessage(Message.parse("modreqTooManyOpen"));
            block.breakNaturally();
            return;
        }
        int userId = DatabaseManager.getDatabase().getUserId(event.getPlayer().getName());
        if(DatabaseManager.getDatabase().fileRequest(event.getPlayer().getName(), event.getPlayer().getWorld().getName(), event.getPlayer().getLocation(), message, userId)){
            int ticketId = DatabaseManager.getDatabase().getLatestTicketIdByUser(userId);
            plugin.requestMap.put(ticketId, new HelpRequest(event.getPlayer().getName(), ticketId, System.currentTimeMillis()/1000, message, 0, event.getPlayer().getLocation().getBlockX(), event.getPlayer().getLocation().getBlockY(), event.getPlayer().getLocation().getBlockZ(), event.getPlayer().getLocation().getYaw(), event.getPlayer().getLocation().getPitch(), event.getPlayer().getWorld().getName()));
            event.getPlayer().sendMessage(Message.parse("modreqFiledUser"));
            RTSFunctions.messageMods(Message.parse("modreqFiledMod", event.getPlayer().getName(), ticketId));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event){
        if(plugin.moderatorMap.contains(event.getPlayer().getName())) plugin.moderatorMap.remove(event.getPlayer().getName());
    }
}
