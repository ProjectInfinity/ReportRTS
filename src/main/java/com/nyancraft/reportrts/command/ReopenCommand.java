package com.nyancraft.reportrts.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;

public class ReopenCommand implements CommandExecutor{

    private ReportRTS plugin;
    public ReopenCommand(ReportRTS plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!RTSPermissions.canCompleteRequests(sender)) return true;
        if(args.length == 0) return false;
        if(!RTSFunctions.isParsableToInt(args[0])) return false;
        double start = 0;
        if(plugin.debugMode) start = System.nanoTime();
        if(!DatabaseManager.getDatabase().setRequestStatus(Integer.parseInt(args[0]), sender.getName(), 0, "", 0)){
            sender.sendMessage(Message.parse("generalInternalError", "Unable to reopen request #" + args[0]));
            return true;
        }

        // For lack of a better way of doing it. SHOULD DO SOMETHING ABOUT THIS!
        plugin.requestMap.clear();
        DatabaseManager.getDatabase().populateRequestMap();

        RTSFunctions.messageMods(Message.parse("reopenedRequest", sender.getName(), args[0]));
        sender.sendMessage(Message.parse("reopenedRequestSelf", args[0]));
        if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
        return true;
    }
}
