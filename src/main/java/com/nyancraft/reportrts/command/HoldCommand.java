package com.nyancraft.reportrts.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.event.ReportHoldEvent;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.util.Message;
import com.nyancraft.reportrts.util.BungeeCord;

import java.io.IOException;

public class HoldCommand implements CommandExecutor {

    private ReportRTS plugin;

    public HoldCommand(ReportRTS plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!RTSPermissions.canPutTicketOnHold(sender)) return true;
        if(args.length == 0) return false;
        if(!RTSFunctions.isParsableToInt(args[0])) return false;
        double start = 0;
        if(plugin.debugMode) start = System.nanoTime();
        String reason = RTSFunctions.implode(args, " ");
        int ticketId = Integer.parseInt(args[0]);
        if(reason.length() <= args[0].length()){
            reason = "None specified.";
        }else{
            reason = reason.substring(args[0].length());
        }
        long timestamp = System.currentTimeMillis() / 1000;
        if(!DatabaseManager.getDatabase().setRequestStatus(ticketId, sender.getName(), 2, reason, 0, timestamp, true)) {
            sender.sendMessage(Message.parse("generalInternalError", "Unable to put request #" + args[0] + " on hold."));
            return true;
        }
        
        if(plugin.requestMap.containsKey(ticketId)){
        	
            Player player = sender.getServer().getPlayer(plugin.requestMap.get(ticketId).getUUID());
            if(player != null){
                player.sendMessage(Message.parse("holdUser", sender.getName()));
                player.sendMessage(Message.parse("holdText", plugin.requestMap.get(ticketId).getMessage(), reason.trim()));
            }
            
            plugin.getServer().getPluginManager().callEvent(new ReportHoldEvent(plugin.requestMap.get(ticketId), reason, sender));
            
            plugin.requestMap.remove(ticketId);
        }

        try{
            BungeeCord.globalNotify(Message.parse("holdRequest", args[0], sender.getName()), ticketId, NotificationType.HOLD);
        }catch(IOException e){
            e.printStackTrace();
        }
        RTSFunctions.messageMods(Message.parse("holdRequest", args[0], sender.getName()), false);
        if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
        return true;
    }

}
