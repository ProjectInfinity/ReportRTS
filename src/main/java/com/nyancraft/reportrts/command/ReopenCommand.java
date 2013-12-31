package com.nyancraft.reportrts.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.event.ReportReopenEvent;
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
        int ticketId = Integer.parseInt(args[0]);
        if(plugin.debugMode) start = System.nanoTime();
        if(!DatabaseManager.getDatabase().setRequestStatus(Integer.parseInt(args[0]), sender.getName(), 0, "", 0)){
            sender.sendMessage(Message.parse("generalInternalError", "Unable to reopen request #" + args[0]));
            return true;
        }

       if(RTSFunctions.syncTicket(ticketId)){
           // TODO: Implement BungeeCord specific code here.
           RTSFunctions.messageMods(Message.parse("reopenedRequest", sender.getName(), args[0]), true);
           // Let other plugins know the request was assigned.
           plugin.getServer().getPluginManager().callEvent(new ReportReopenEvent(plugin.requestMap.get(ticketId), sender));
           sender.sendMessage(Message.parse("reopenedRequestSelf", args[0]));
           if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
           return true;
       }else{
           sender.sendMessage(Message.parse("generalInternalError", "Unable to reopen request #" + args[0]));
           return true;
       }
    }
}
