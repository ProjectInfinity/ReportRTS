package com.nyancraft.reportrts.util;

import com.nyancraft.reportrts.ReportRTS;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class VersionChecker {

    public boolean upToDate(){
        if(!ReportRTS.getPlugin().getConfig().getBoolean("versionCheck")) return true;
        try {
            final String currentVersion = ReportRTS.getPlugin().getDescription().getVersion().substring(0,ReportRTS.getPlugin().getDescription().getVersion().lastIndexOf("-"));
            final URLConnection connection = new URL("https://api.curseforge.com/servermods/files?projectIds=36853").openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-agent", "ReportRTS version " + currentVersion + " (By ProjectInfinity)");
            final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();
            reader.close();
            connection.getInputStream().close();
            JSONArray array = (JSONArray) JSONValue.parse(response);
            if(array.size() > 0){
                JSONObject latest = (JSONObject) array.get(array.size() - 1);
                String version = (String) latest.get("name");
                String download = (String) latest.get("downloadUrl");
                if(Integer.parseInt(currentVersion.replace("v", "").replaceAll("[^A-Za-z0-9]", "")) < Integer.parseInt(version.replace("v", "").replaceAll("[^A-Za-z0-9]", ""))){
                    ReportRTS.getPlugin().getLogger().info("Version " + version + " is available for download.");
                    ReportRTS.getPlugin().getLogger().info("Download it at " + download);
                    ReportRTS.getPlugin().versionString = version;
                    return false;
                }else{
                    return true;
                }
            }else{
                return true;
            }
        } catch (final Exception e) {
            return true;
        }
    }
}
