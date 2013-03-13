package com.nyancraft.reportrts;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.util.Message;

public class RTSPermissions {

    public static boolean hasPermission(CommandSender sender, String permission){
        if(!sender.hasPermission(permission)){
            sender.sendMessage(Message.parse("generalPermissionError", permission));
        }
        return sender.hasPermission(permission);
    }

    public static boolean isModerator(Player player){
        return hasPermission(player, "reportrts.mod");
    }

    public static boolean canFileRequest(CommandSender sender){
        return hasPermission(sender, "reportrts.command.modreq");
    }

    public static boolean canCheckAllRequests(CommandSender sender){
        return hasPermission(sender, "reportrts.command.check");
    }

    public static boolean canCheckOwnRequests(CommandSender sender){
        return hasPermission(sender, "reportrts.command.check.self");
    }

    public static boolean canCompleteRequests(CommandSender sender){
        return hasPermission(sender, "reportrts.command.complete");
    }

    public static boolean canCompleteOwnRequests(CommandSender sender){
        return hasPermission(sender, "reportrts.command.complete.self");
    }

    public static boolean canTeleport(CommandSender sender){
        return hasPermission(sender, "reportrts.command.teleport");
    }

    public static boolean canReloadPlugin(CommandSender sender){
        return hasPermission(sender, "reportrts.command.reload");
    }

    public static boolean canBanUser(CommandSender sender){
        return hasPermission(sender, "reportrts.command.ban");
    }

    public static boolean canResetPlugin(CommandSender sender){
        return hasPermission(sender, "reportrts.command.reset");
    }

    public static boolean canPutTicketOnHold(CommandSender sender){
        return hasPermission(sender, "reportrts.command.hold");
    }

    public static boolean canClaimTicket(CommandSender sender){
        return hasPermission(sender, "reportrts.command.claim");
    }

    public static boolean canListStaff(CommandSender sender){
        return hasPermission(sender, "reportrts.command.modlist");
    }

    public static boolean canBroadcast(CommandSender sender){
        return hasPermission(sender, "reportrts.command.broadcast");
    }

    public static boolean canCheckStats(CommandSender sender){
        return hasPermission(sender, "reportrts.command.stats");
    }

    public static boolean canOverride(CommandSender sender){
        return hasPermission(sender, "reportrts.override");
    }

    public static boolean canSeeHelpPage(CommandSender sender){
        return hasPermission(sender, "reportrts.command.help");
    }

    public static boolean canManageNotifications(CommandSender sender){
        return hasPermission(sender, "reportrts.command.notifications");
    }

    public static boolean canAssignRequests(CommandSender sender){
        return hasPermission(sender, "reportrts.command.assign");
    }
}
