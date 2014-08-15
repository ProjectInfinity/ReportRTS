package com.nyancraft.reportrts.util;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.ReportRTS;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TabCompleteHelper implements TabCompleter{

    private ReportRTS plugin;

    public TabCompleteHelper(ReportRTS plugin){
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        /** Argument checker, DO NOT LEAVE THIS UNCOMMENTED IN PRODUCTION
        int tempI = -1;
        for(String arg : args) {
            tempI++;
            System.out.println("Position: " + tempI + " | Actual Position: " + (tempI + 1) + " | Argument: " + arg);
        }
        /** LOOK ABOVE **/
        if(args.length == 0 || (!args[0].equalsIgnoreCase(plugin.commandMap.get("readTicket")) && !args[0].equalsIgnoreCase(plugin.commandMap.get("closeTicket")) &&
            !args[0].equalsIgnoreCase(plugin.commandMap.get("teleportToTicket")) && !args[0].equalsIgnoreCase(plugin.commandMap.get("holdTicket")) &&
            !args[0].equalsIgnoreCase(plugin.commandMap.get("claimTicket")) && !args[0].equalsIgnoreCase(plugin.commandMap.get("unclaimTicket")) &&
            !args[0].equalsIgnoreCase(plugin.commandMap.get("assignTicket")))) {
                // If you got here then the sub-command you tried to tab-complete does not support it.
                List<String> response = new ArrayList<>();
                response.add("");
                return response;
        }
        if(args.length < 2 || args.length >= 2 && (!RTSFunctions.isNumber(args[1]) || args[1].equalsIgnoreCase(sender.getName())) || plugin.requestMap.size() < 1) {

            if(args.length < 2 || args[1].isEmpty()) {
                List<String> response = new ArrayList<>();
                if(args.length >= 2) {
                    response.add((args[1].equalsIgnoreCase(" ") ? " " : "") + plugin.requestMap.keySet().toArray()[0].toString());
                } else {
                    response.add(args[0] + " " + plugin.requestMap.keySet().toArray()[0].toString());
                }
                return response;
            }
            List<String> response = new ArrayList<>();
            response.add("");
            return response;
        }
        final Set<Integer> keys = plugin.requestMap.keySet();
        int initialKey = Integer.parseInt(args[1]);
        if(initialKey <= 0) return null;
        int prevKey = 0;
        for(int key : keys) {
            if(!Integer.toString(key).startsWith(args[1]) && !(key == prevKey) || initialKey > key) {
                continue;
            }
            if(initialKey == key) {
                for(int i : keys) {
                    if(i <= initialKey) continue;
                    prevKey = i;
                    break;
                }
                break;
            }
            prevKey = key;
            break;
        }
        if(prevKey == 0) return null;
        List<String> response = new ArrayList<>();
        response.add(Integer.toString(prevKey));
        return response;
    }
}