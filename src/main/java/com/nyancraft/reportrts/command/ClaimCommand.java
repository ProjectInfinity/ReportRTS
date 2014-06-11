package com.nyancraft.reportrts.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.event.ReportClaimEvent;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.data.NotificationType;

import java.io.IOException;

public class ClaimCommand implements CommandExecutor{

    private ReportRTS plugin;

    public ClaimCommand(ReportRTS plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(!(sender instanceof Player)){
            sender.sendMessage("As of the UUID update, console may not claim requests.");
            return true;
        }
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
        long timestamp = System.currentTimeMillis() / 1000;
        if(!DatabaseManager.getDatabase().setRequestStatus(ticketId, name, 1, "", 0, timestamp)){
            sender.sendMessage(Message.parse("generalInternalError", "Unable to claim request #" + args[0]));
            return true;
        }
        Player player = sender.getServer().getPlayer(plugin.requestMap.get(ticketId).getUUID());
        if(player != null){
            player.sendMessage(Message.parse("claimUser", name));
            player.sendMessage(Message.parse("claimText", plugin.requestMap.get(ticketId).getMessage()));
        }
        plugin.requestMap.get(ticketId).setStatus(1);
        plugin.requestMap.get(ticketId).setModUUID(((Player) sender).getUniqueId());
        plugin.requestMap.get(ticketId).setModTimestamp(timestamp);
        plugin.requestMap.get(ticketId).setModName(name);
        try{
            BungeeCord.globalNotify(Message.parse("claimRequest", name, args[0]), ticketId, NotificationType.MODIFICATION);
        }catch(IOException e){
            e.printStackTrace();
        }
        RTSFunctions.messageMods(Message.parse("claimRequest", name, args[0]), false);
        
        // Let other plugins know the request was claimed
        plugin.getServer().getPluginManager().callEvent(new ReportClaimEvent(plugin.requestMap.get(ticketId)));
        
        if(plugin.debugMode) Message.debug(name, this.getClass().getSimpleName(), start, cmd.getName(), args);
        return true;
    }

}
