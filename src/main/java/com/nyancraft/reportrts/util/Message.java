package com.nyancraft.reportrts.util;

import java.text.MessageFormat;

import org.bukkit.ChatColor;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.ReportRTS;

public class Message {

    public static String parse(String key, Object ... params ){
        Object prop = ReportRTS.getMessageHandler().messageMap.get(key);
        if(prop == null || !(prop instanceof String)) {
            if(!ReportRTS.getMessageHandler().getMessageConfig().getDefaults().contains(key))
                return "Missing message <" + key + "> in ReportRTS/messages.yml, no default found.";
            ReportRTS.getMessageHandler().messageMap.put(key, ReportRTS.getMessageHandler().getMessageConfig().getDefaults().getString(key));
            ReportRTS.getMessageHandler().getMessageConfig().set(key, ReportRTS.getMessageHandler().getMessageConfig().getDefaults().getString(key));
            prop = ReportRTS.getMessageHandler().getMessageConfig().getDefaults().getString(key);
            ReportRTS.getMessageHandler().saveMessageConfig();
        }
        String message = (String) prop;
        for (ChatColor color : ChatColor.values()) {
            String colorKey = "%" + color.name().toLowerCase() + "%";
            if (message.contains(colorKey)) {
                message = message.replaceAll(colorKey, color.toString());
            }
        }
        return MessageFormat.format(message, params);
    }

    public static void debug(String name, String className, double start, String cmd, String[] args){
        String arguments = RTSFunctions.implode(args, " ");
        ReportRTS.getPlugin().getLogger().info(name + " " + className + " took " + RTSFunctions.getTimeSpent(start) + "ms: " + cmd + " " + arguments);
    }
}
