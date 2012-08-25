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
        long start = 0;
        if(plugin.debugMode) start = System.currentTimeMillis();
        if(!plugin.requestMap.containsKey(Integer.parseInt(args[0]))){
            sender.sendMessage(Message.parse("claimNotOpen"));
            return true;
        }
        String name = sender.getName();
        if(name == null){
            sender.sendMessage(Message.parse("generalInternalError", "Name is null! Try again."));
            return true;
        }
        if(!DatabaseManager.getDatabase().setRequestStatus(Integer.parseInt(args[0]), name, 1, "", 0)){
            sender.sendMessage(Message.parse("generalInternalError", "Unable to claim request #" + args[0]));
            return true;
        }
        Player player = sender.getServer().getPlayer(plugin.requestMap.get(Integer.parseInt(args[0])).getName());
        if(player != null){
            player.sendMessage(Message.parse("claimUser", name));
            player.sendMessage(Message.parse("claimText", plugin.requestMap.get(Integer.parseInt(args[0])).getMessage()));
        }
        plugin.requestMap.get(Integer.parseInt(args[0])).setStatus(1);
        plugin.requestMap.get(Integer.parseInt(args[0])).setModName(name);
        plugin.requestMap.get(Integer.parseInt(args[0])).setModTimestamp(System.currentTimeMillis()/1000);
        RTSFunctions.messageMods(Message.parse("claimRequest", name, args[0]), sender.getServer().getOnlinePlayers());
        if(plugin.debugMode) Message.debug(name, this.getClass().getSimpleName(), start, cmd.getName(), args);
        return true;
    }

}
