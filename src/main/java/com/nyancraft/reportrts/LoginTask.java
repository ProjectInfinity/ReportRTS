package com.nyancraft.reportrts;

import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class LoginTask implements Runnable {

    private final ReportRTS plugin;
    private final UUID uuid;
    private final Map.Entry<Integer, UUID> entry;

    public LoginTask(ReportRTS plugin, UUID uuid, Map.Entry<Integer, UUID> entry) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.entry = entry;
    }

    public void run() {
        ResultSet rs = DatabaseManager.getDatabase().getTicketById(entry.getKey());
        try{
            boolean online = false;
            for(Player player : plugin.getServer().getOnlinePlayers()){
                if(uuid.equals(player.getUniqueId())){
                    online = true;
                    break;
                }
            }
            if(online) plugin.getServer().getPlayer(uuid).sendMessage(Message.parse("completedUserOffline"));
            rs.first();
            // Prevent duplicate notifications (especially across multiple servers)
            int notifStatus = rs.getInt("notified_of_completion");
            if(notifStatus == 1) return;
            String comment = rs.getString("mod_comment");
            if(comment == null) comment = "";
            if(online) plugin.getServer().getPlayer(uuid).sendMessage(Message.parse("completedText", rs.getString("text"), comment));
            rs.close();
            if(!DatabaseManager.getDatabase().setNotificationStatus(entry.getKey(), 1)) plugin.getLogger().warning("Unable to set notification status to 1.");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

}