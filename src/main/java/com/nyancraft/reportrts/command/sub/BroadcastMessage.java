package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.event.TicketBroadcastEvent;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class BroadcastMessage {

    private static ReportRTS plugin = ReportRTS.getPlugin();

    /**
     * Initial handling of the Broadcast sub-command.
     * @param sender player that sent the command
     * @param args arguments
     * @return true if command handled correctly
     */
    public static boolean handleCommand(CommandSender sender, String[] args) {

        if(!RTSPermissions.canBroadcast(sender)) return true;
        if(args.length < 2) return false;
        args[0] = null;
        String message = RTSFunctions.implode(args, " ");
        try {
            BungeeCord.globalNotify(Message.broadcast(sender.getName(), message), -1, NotificationType.NOTIFYONLY);
        } catch(IOException e) {
            e.printStackTrace();
        }
        RTSFunctions.messageStaff(Message.broadcast(sender.getName(), message), false);
        // Let other plugins know about the broadcast
        plugin.getServer().getPluginManager().callEvent(new TicketBroadcastEvent(sender, message));
        return true;
    }
}