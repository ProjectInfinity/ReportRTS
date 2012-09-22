package com.nyancraft.reportrts.util;

import com.nyancraft.reportrts.ReportRTS;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class MessageHandler {
    private FileConfiguration messageConfig = null;
    private File messageFile = null;

    public static Map<String, String> messageMap = new HashMap<String, String>();

    public void reloadMessageConfig() {
        if(!configExists()){
            if(messageFile == null) messageFile = new File(ReportRTS.getPlugin().getDataFolder(), "messages.yml");
            messageConfig = YamlConfiguration.loadConfiguration(ReportRTS.getPlugin().getResource("messages.yml"));
            saveMessageConfig();
        }
        if(messageFile == null) messageFile = new File(ReportRTS.getPlugin().getDataFolder(), "messages.yml");
        messageConfig = YamlConfiguration.loadConfiguration(messageFile);
        InputStream defaultMessageStream = ReportRTS.getPlugin().getResource("messages.yml");
        if(defaultMessageStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultMessageStream);
            messageConfig.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getMessageConfig() {
        if(messageConfig == null) this.reloadMessageConfig();
        return messageConfig;
    }

    public void saveMessageConfig() {
        if(messageConfig == null || messageFile == null) return;
        try {
            getMessageConfig().save(messageFile);
        } catch(IOException e) {
            ReportRTS.getPlugin().getLogger().log(Level.SEVERE, "Could not save messages to " + messageConfig, e);
        }
    }

    public void reloadMessageMap() {
        Set<String> Messages = messageConfig.getKeys(false);
        for(String message : Messages){
            messageMap.put(message, messageConfig.getString(message));
        }
    }

    private boolean configExists() {
        return new File(ReportRTS.getPlugin().getDataFolder(), "messages.yml").exists();
    }
}
