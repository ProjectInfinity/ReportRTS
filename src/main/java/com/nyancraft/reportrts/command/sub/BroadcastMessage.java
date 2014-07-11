package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.NotificationType;
import com.nyancraft.reportrts.event.ReportModBroadcastEvent;
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
        args[0] = "";
        String message = RTSFunctions.implode(args, " ");
        try {
            BungeeCord.globalNotify(Message.parse("broadcastMessage", sender.getName(), message), -1, NotificationType.NOTIFYONLY);
        } catch(IOException e) {
            e.printStackTrace();
        }
        RTSFunctions.messageMods(Message.parse("broadcastMessage", sender.getName(), message), false);
        // Let other plugins know about the broadcast
        plugin.getServer().getPluginManager().callEvent(new ReportModBroadcastEvent(sender, message));
        return true;
    }
}