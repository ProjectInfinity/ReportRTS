package com.nyancraft.reportrts.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;

public class ClaimCommand implements CommandExecutor{

    private ReportRTS plugin;

    public ClaimCommand(ReportRTS plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0) return false;
        if(!RTSPermissions.canClaimTicket(sender)) return true;
        if(!RTSFunctions.isParsableToInt(args[0])) return false;
        double start = 0;
        if(plugin.debugMode) start = System.nanoTime();
        int ticketId = Integer.parseInt(args[0]);
        if(!plugin.requestMap.containsKey(ticketId)){
            sender.sendMessage(Message.parse("claimNotOpen"));
            return true;
        }
        String name = sender.getName();
        if(name == null){
            sender.sendMessage(Message.parse("generalInternalError", "Name is null! Try again."));
            return true;
        }
        if(!DatabaseManager.getDatabase().setRequestStatus(ticketId, name, 1, "", 0)){
            sender.sendMessage(Message.parse("generalInternalError", "Unable to claim request #" + args[0]));
            return true;
        }
        Player player = sender.getServer().getPlayer(plugin.requestMap.get(ticketId).getName());
        if(player != null){
            player.sendMessage(Message.parse("claimUser", name));
            player.sendMessage(Message.parse("claimText", plugin.requestMap.get(ticketId).getMessage()));
        }
        plugin.requestMap.get(ticketId).setStatus(1);
        plugin.requestMap.get(ticketId).setModName(name);
        plugin.requestMap.get(ticketId).setModTimestamp(System.currentTimeMillis()/1000);
        RTSFunctions.messageMods(Message.parse("claimRequest", name, args[0]));
        if(plugin.debugMode) Message.debug(name, this.getClass().getSimpleName(), start, cmd.getName(), args);
        return true;
    }

}
