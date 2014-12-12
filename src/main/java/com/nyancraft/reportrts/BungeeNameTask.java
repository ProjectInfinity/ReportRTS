package com.nyancraft.reportrts;

import com.nyancraft.reportrts.util.BungeeCord;

public class BungeeNameTask implements Runnable {

    private ReportRTS plugin;

    public BungeeNameTask(ReportRTS plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // TODO: Verify that this works.
        if(plugin.getServer().getOnlinePlayers().size() > 0 && (BungeeCord.getServerName() == null || BungeeCord.getServerName().isEmpty())) {
            plugin.getLogger().info("Server name updated, task is shutting down.");
            BungeeCord.getServer();
            Thread.currentThread().interrupt();
        }
    }
}