package com.nyancraft.reportrts.util;

import java.text.MessageFormat;

import org.bukkit.ChatColor;

import com.nyancraft.reportrts.ReportRTS;

public class Message {
	
    public static String parse( String key, Object ... params ) {
        
        Object prop = ReportRTS.getPlugin().messageMap.get(key);
        if( prop == null || !(prop instanceof String) ) {
            return "Missing message <" + key + "> in ReportRTS/config.yml";
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
}
