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

    // TODO: REALLY needs testing with the new command system.
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        //System.out.println("Command: " + cmd + ", Label: " + label + ", Args: " + RTSFunctions.implode(args, "") + ", Length: " + args.length);
        if(args.length > 1 || !RTSFunctions.isNumber(args[1]) || args[1].equalsIgnoreCase(sender.getName()) || plugin.requestMap.size() < 1) {

            if(args[1].isEmpty()) {
                List<String> response = new ArrayList<>();
                response.add(plugin.requestMap.keySet().toArray()[1].toString());
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