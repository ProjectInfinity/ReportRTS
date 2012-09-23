package com.nyancraft.reportrts.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.util.Message;

public class ModBroadcastCommand implements CommandExecutor{

    private ReportRTS plugin;

    public ModBroadcastCommand(ReportRTS plugin){
        this.plugin = plugin;
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(!RTSPermissions.canBroadcast(sender)) return true;
        if(args.length == 0) return false;
        double start = 0;
        if(plugin.debugMode) start = System.nanoTime();
        String message = RTSFunctions.implode(args, " ");
        RTSFunctions.messageMods(Message.parse("broadcastMessage", sender.getName(), message));
        if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
        return true;
    }
}
