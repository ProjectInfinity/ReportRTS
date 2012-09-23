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

public class UnclaimCommand implements CommandExecutor{

    private ReportRTS plugin;
    public UnclaimCommand(ReportRTS plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0) return false;
        if(!RTSPermissions.canClaimTicket(sender)) return true;
        if(!RTSFunctions.isParsableToInt(args[0])) return false;
        double start = 0;
        if(plugin.debugMode) start = System.nanoTime();
        int ticketId = Integer.parseInt(args[0]);
        if(!plugin.requestMap.containsKey(ticketId) || plugin.requestMap.get(ticketId).getStatus() != 1){
            sender.sendMessage(Message.parse("unclaimNotClaimed"));
            return true;
        }
        if(!sender.getName().equals(plugin.requestMap.get(ticketId).getModName()) && !RTSPermissions.canOverride(sender)) return true;
        if(!DatabaseManager.getDatabase().setRequestStatus(ticketId, sender.getName(), 0, "", 0)){
            sender.sendMessage(Message.parse("generalInternalError", "Unable to unclaim request #" + args[0]));
            return true;
        }
        Player player = sender.getServer().getPlayer(plugin.requestMap.get(ticketId).getName());
        if(player != null){
            player.sendMessage(Message.parse("unclaimUser", plugin.requestMap.get(ticketId).getModName()));
            player.sendMessage(Message.parse("unclaimText", plugin.requestMap.get(ticketId).getMessage()));
        }
        plugin.requestMap.get(ticketId).setStatus(0);
        RTSFunctions.messageMods(Message.parse("unclaimReqMod", plugin.requestMap.get(ticketId).getModName(), args[0]));
        plugin.requestMap.get(ticketId).setModName(null);
        sender.sendMessage(Message.parse("unclaimReqSelf", args[0]));

        if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
        return true;
    }
}
