package com.nyancraft.reportrts;

import com.nyancraft.reportrts.util.BungeeCord;
import org.bukkit.scheduler.BukkitRunnable;

public class BungeeNameTask extends BukkitRunnable {

    private ReportRTS plugin;

    public BungeeNameTask(ReportRTS plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if(plugin.getServer().getOnlinePlayers().size() > 0) {
            plugin.getLogger().info("Server name updated, task is shutting down.");
            BungeeCord.getServer();
            this.cancel();
        }
    }
}