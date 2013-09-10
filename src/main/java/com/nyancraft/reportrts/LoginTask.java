package com.nyancraft.reportrts;

import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class LoginTask extends BukkitRunnable {

    private final ReportRTS plugin;
    private final String player;
    private final Map.Entry<Integer, String> entry;

    public LoginTask(ReportRTS plugin, String player, Map.Entry<Integer, String> entry) {
        this.plugin = plugin;
        this.player = player;
        this.entry = entry;
    }

    public void run() {
        ResultSet rs = DatabaseManager.getDatabase().getTicketById(entry.getKey());
        try{
            plugin.getServer().getPlayer(player).sendMessage(Message.parse("completedUserOffline"));
            rs.first();
            String comment = rs.getString("mod_comment");
            if(comment == null) comment = "";
            plugin.getServer().getPlayer(player).sendMessage(Message.parse("completedText", rs.getString("text"), comment));
            rs.close();
            if(!DatabaseManager.getDatabase().setNotificationStatus(entry.getKey(), 1)) plugin.getLogger().warning("Unable to set notification status to 1.");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

}