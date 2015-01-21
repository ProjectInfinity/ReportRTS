package com.nyancraft.reportrts;

import com.nyancraft.reportrts.data.Ticket;
import com.nyancraft.reportrts.persistence.DataProvider;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class LoginTask extends BukkitRunnable {

    private final ReportRTS plugin;
    private final UUID uuid;
    private final Map.Entry<Integer, UUID> entry;
    private final DataProvider data;

    public LoginTask(ReportRTS plugin, UUID uuid, Map.Entry<Integer, UUID> entry) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.entry = entry;
        this.data = plugin.getDataProvider();
    }

    public void run() {

        Ticket ticket = data.getTicket(entry.getKey());

        boolean online = plugin.getServer().getPlayer(uuid) != null;

        if(online) plugin.getServer().getPlayer(uuid).sendMessage(Message.parse("completedUserOffline"));

        // Prevent duplicate notifications (especially across multiple servers)
        if(ticket.isNotified()) return;

        String comment = ticket.getModComment();
        if(comment == null) comment = "";
        if(online) plugin.getServer().getPlayer(uuid).sendMessage(Message.parse("completedText", ticket.getMessage(), comment));

        if(data.setNotificationStatus(entry.getKey(), true) < 1) plugin.getLogger().warning("Unable to set notification status to 1 for ticket " + entry.getKey() + ".");
    }
}