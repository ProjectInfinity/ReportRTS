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
        long start = 0;
        if(plugin.debugMode) start = System.currentTimeMillis();
        if(!plugin.requestMap.containsKey(Integer.parseInt(args[0]))){
            sender.sendMessage(Message.parse("unclaimNotClaimed"));
            return true;
        }
        if(plugin.requestMap.get(Integer.parseInt(args[0])).getStatus() != 1){
            sender.sendMessage(Message.parse("unclaimNotClaimed"));
            return true;
        }
        if(!DatabaseManager.getDatabase().setRequestStatus(Integer.parseInt(args[0]), sender.getName(), 0, "", 0)){
            sender.sendMessage(Message.parse("generalInternalError", "Unable to claim request #" + args[0]));
            return true;
        }
        Player player = sender.getServer().getPlayer(plugin.requestMap.get(Integer.parseInt(args[0])).getName());
        if(player != null){
            player.sendMessage(Message.parse("unclaimUser", plugin.requestMap.get(Integer.parseInt(args[0])).getModName()));
            player.sendMessage(Message.parse("unclaimText", plugin.requestMap.get(Integer.parseInt(args[0])).getMessage()));
        }
        plugin.requestMap.get(Integer.parseInt(args[0])).setStatus(0);
        RTSFunctions.messageMods(Message.parse("unclaimReqMod", plugin.requestMap.get(Integer.parseInt(args[0])).getModName(), args[0]), sender.getServer().getOnlinePlayers());
        plugin.requestMap.get(Integer.parseInt(args[0])).setModName(null);
        sender.sendMessage(Message.parse("unclaimReqSelf", args[0]));

        if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
        return true;
    }
}
