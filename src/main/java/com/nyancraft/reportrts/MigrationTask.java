package com.nyancraft.reportrts;

import com.nyancraft.reportrts.persistence.MySQLDataProvider;
import com.nyancraft.reportrts.util.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MigrationTask extends BukkitRunnable {

    /** This MigrationTask is for MySQL ONLY where UUID field does not exist. **/

    private ReportRTS plugin;
    private MySQLDataProvider data;

    public MigrationTask(ReportRTS plugin) {
        this.plugin = plugin;
        this.data = (MySQLDataProvider) plugin.getDataProvider();
    }

    @Override
    public void run() {
        if(!data.isLoaded()){
            this.cancel();
            return;
        }
        ArrayList<String> players = new ArrayList<>();
        ArrayList<String> delete = new ArrayList<>();
        Map<String, UUID> response = new HashMap<>();
        Map<String, Integer> tracking = new HashMap<>();
        UUIDFetcher fetcher;
        try {
            ResultSet preLoopRS = data.query("SELECT COUNT(`name`) FROM `" + plugin.storagePrefix + "reportrts_user` WHERE `uuid` IS NULL OR `uuid` = ''");
            preLoopRS.first();
            int total = preLoopRS.getInt(1);
            preLoopRS.close();
            int count = 0;
            int tempCount = 0;
            while (true) {
                players.clear();
                response.clear();
                tempCount = 0;

                ResultSet rs = data.query("SELECT `name` FROM `" + plugin.storagePrefix + "reportrts_user` WHERE `uuid` IS NULL OR `uuid` = '' LIMIT 40");
                while(rs.next()) players.add(rs.getString("name"));
                rs.close();
                if (players.size() < 1) break;
                fetcher = new UUIDFetcher(players);
                response = fetcher.call();

                if (players.contains("CONSOLE")) response.put("CONSOLE", UUID.randomUUID());

                for(Map.Entry<String, UUID> entry : response.entrySet()){
                    data.query("UPDATE `" + plugin.storagePrefix + "reportrts_user` SET `uuid` = '" + entry.getValue().toString() + "' WHERE `name` = '" + entry.getKey() + "'");
                    tempCount++;
                    //System.out.println("[ReportRTS] Updated player entry " + entry.getKey() + " with UUID " + entry.getValue()); // Too spammy?
                }

                count = count + tempCount;

                System.out.println("[ReportRTS] Updated " + tempCount + " player entries.");
                System.out.println("[ReportRTS] Progress: " + count + "/" + total + " " + String.format("%.2f", (float)(count * 100) / total) + "%");
                System.out.println("[ReportRTS] ----------------------------");

                for(String player: players) {
                    if(!tracking.containsKey(player)) {
                        tracking.put(player, 1);
                        continue;
                    }
                    tracking.put(player, tracking.get(player) + 1);
                    if(tracking.get(player) >= 10) delete.add(player);
                }

                if(delete.size() > 0) {
                    for(String ghost : delete) {
                        data.query("DELETE FROM `" + plugin.storagePrefix + "reportrts_user` WHERE `name` = '" + ghost +"'");
                        System.out.println("[ReportRTS] User " + ghost + " does not exist and was deleted.");
                    }
                    delete.clear();
                }
            }

            System.out.println("[ReportRTS] Finished migrating data. Please reload the plugin.");
            Bukkit.getScheduler().scheduleSyncDelayedTask(ReportRTS.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    ReportRTS.getPlugin().reloadPlugin();
                }
            });

            this.cancel();

            } catch (Exception e) {
            e.printStackTrace();
            this.cancel();
        }
    }
}
