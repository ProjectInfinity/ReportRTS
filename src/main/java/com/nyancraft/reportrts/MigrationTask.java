package com.nyancraft.reportrts;

import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MigrationTask extends BukkitRunnable {

    private ReportRTS plugin;

    public MigrationTask(ReportRTS plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if(!DatabaseManager.getDatabase().isLoaded()){
            this.cancel();
            return;
        }
        ArrayList<String> players = new ArrayList<>();
        Map<String, UUID> response = new HashMap<String, UUID>();
        UUIDFetcher fetcher = null;
        try {
            // TODO: Show progression?
            ResultSet preLoopRS = DatabaseManager.getDatabase().query("SELECT COUNT(`name`) FROM `" + plugin.storagePrefix + "reportrts_user` WHERE `uuid` IS NULL OR `uuid` = ''");
            preLoopRS.first();
            int total = preLoopRS.getInt(1);
            preLoopRS.close();
            int count = 0;
            int tempCount = 0;
            while (true) {
                players.clear();
                response.clear();
                tempCount = 0;

                ResultSet rs = DatabaseManager.getDatabase().query("SELECT `name` FROM `" + plugin.storagePrefix + "reportrts_user` WHERE `uuid` IS NULL OR `uuid` = '' LIMIT 40");
                while(rs.next()) players.add(rs.getString("name"));
                rs.close();
                if (players.size() < 1) break;

                fetcher = new UUIDFetcher(players);
                response = fetcher.call();

                if (players.contains("CONSOLE")) response.put("CONSOLE", UUID.randomUUID());

                for(Map.Entry<String, UUID> entry : response.entrySet()){
                    DatabaseManager.getDatabase().query("UPDATE `" + plugin.storagePrefix + "reportrts_user` SET `uuid` = '" + entry.getValue().toString() + "' WHERE `name` = '" + entry.getKey() + "'");
                    tempCount++;
                    //System.out.println("[ReportRTS] Updated player entry " + entry.getKey() + " with UUID " + entry.getValue()); // Too spammy?
                }

                count = count + tempCount;

                System.out.println("[ReportRTS] Updated " + tempCount + " player entries.");
                System.out.println("[ReportRTS] Progress: " + count + "/" + total + " " + String.format("%.2f", (float)(count * 100) / total) + "%");
                System.out.println("[ReportRTS] ----------------------------");
            }

            System.out.println("[ReportRTS] Finished migrating data. Please reload the plugin.");
            Bukkit.getScheduler().scheduleSyncDelayedTask(ReportRTS.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    ReportRTS.getPlugin().reloadPlugin();
                }
            });

            this.cancel();
            return;

            } catch (Exception e) {
            e.printStackTrace();
            this.cancel();
            return;
        }
    }
}
