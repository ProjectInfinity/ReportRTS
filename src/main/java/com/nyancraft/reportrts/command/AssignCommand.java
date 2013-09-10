package com.nyancraft.reportrts.command;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.event.ReportAssignEvent;
import com.nyancraft.reportrts.persistence.Database;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AssignCommand implements CommandExecutor {

    private ReportRTS plugin;
    private Database dbManager;

    public AssignCommand(ReportRTS plugin){
        this.plugin = plugin;
        this.dbManager = DatabaseManager.getDatabase();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!RTSPermissions.canAssignRequests(sender)) return true;
        if(args.length <= 1 || !RTSFunctions.isParsableToInt(args[0])) return false;

        double start = 0;
        if(plugin.debugMode) start = System.nanoTime();

        String name = sender.getName();
        int ticketId = Integer.parseInt(args[0]);
        if(!plugin.requestMap.containsKey(ticketId)){
            sender.sendMessage(Message.parse("assignNotOpen"));
            return true;
        }
        String assignee = args[1];
        if(name == null || assignee == null){
            sender.sendMessage(Message.parse("generalInternalError", "Your name or assignee is null! Try again."));
            return true;
        }
        if(DatabaseManager.getDatabase().getUserId(assignee, false) == 0){
            sender.sendMessage(Message.parse("generalInternalError", "That user does not exist!"));
            return true;
        }
        if(!DatabaseManager.getDatabase().setRequestStatus(ticketId, assignee, 1, "", 0)){
            sender.sendMessage(Message.parse("generalInternalError", "Unable to assign request #" + ticketId + " to " + assignee));
            return true;
        }
        Player player = sender.getServer().getPlayer(plugin.requestMap.get(ticketId).getName());
        if(player != null){
            player.sendMessage(Message.parse("assignUser", assignee));
            player.sendMessage(Message.parse("assignText", plugin.requestMap.get(ticketId).getMessage()));
        }
        plugin.requestMap.get(ticketId).setStatus(1);
        plugin.requestMap.get(ticketId).setModName(assignee);
        plugin.requestMap.get(ticketId).setModTimestamp(System.currentTimeMillis()/1000);
        RTSFunctions.messageMods(Message.parse("assignRequest", assignee, ticketId), false);
        
        // Let other plugins know the request was assigned.
        plugin.getServer().getPluginManager().callEvent(new ReportAssignEvent(plugin.requestMap.get(ticketId), sender));
        
        if(plugin.debugMode) Message.debug(name, this.getClass().getSimpleName(), start, cmd.getName(), args);
        return true;
    }
}
