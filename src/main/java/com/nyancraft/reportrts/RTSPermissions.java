package com.nyancraft.reportrts;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.util.Message;

public class RTSPermissions {

    public static boolean isStaff(Player player) {
        if(ReportRTS.permission != null) return ReportRTS.permission.playerHas(player, "reportrts.staff");
        return player.hasPermission("reportrts.staff");
    }

    public static boolean canOpenTicket(CommandSender sender) {
        if(ReportRTS.permission != null) {
            if(!ReportRTS.permission.has(sender, "reportrts.command.open")) {
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.open"));
                return false;
            }
            return true;
        }
        if(!sender.hasPermission("reportrts.command.open")) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.open"));
            return false;
        }
        return true;
    }

    public static boolean canReadAll(CommandSender sender) {
        if(ReportRTS.permission != null) return ReportRTS.permission.has(sender, "reportrts.command.read");
        return sender.hasPermission("reportrts.command.read");
    }

    public static boolean canReadOwn(CommandSender sender) {
        if(ReportRTS.permission != null) return ReportRTS.permission.has(sender, "reportrts.command.read.self");
        return sender.hasPermission("reportrts.command.read.self");
    }

    public static boolean canCloseTicket(CommandSender sender) {
        if(ReportRTS.permission != null) return ReportRTS.permission.has(sender, "reportrts.command.close");
        return sender.hasPermission("reportrts.command.close");
    }

    public static boolean canCloseOwnTicket(CommandSender sender) {
        if(!(sender instanceof Player)) return false;
        if(ReportRTS.permission != null) return ReportRTS.permission.has(sender, "reportrts.command.close.self");
        return sender.hasPermission("reportrts.command.close.self");
    }

    public static boolean canTeleport(CommandSender sender) {
        if(ReportRTS.permission != null) {
            if(!ReportRTS.permission.has(sender, "reportrts.command.teleport")) {
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.teleport"));
                return false;
            }
            return true;
        }
        if(!sender.hasPermission("reportrts.command.teleport")) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.teleport"));
            return false;
        }
        return true;
    }

    public static boolean canReloadPlugin(CommandSender sender) {
        if(ReportRTS.permission != null) {
            if(!ReportRTS.permission.has(sender, "reportrts.command.reload")) {
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.reload"));
                return false;
            }
            return true;
        }
        if(!sender.hasPermission("reportrts.command.reload")) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.reload"));
            return false;
        }
        return true;
    }

    public static boolean canBanUser(CommandSender sender) {
        if(ReportRTS.permission != null){
            if(!ReportRTS.permission.has(sender, "reportrts.command.ban")) {
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.ban"));
                return false;
            }
            return true;
        }
        if(!sender.hasPermission("reportrts.command.ban")) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.ban"));
            return false;
        }
        return true;
    }

    public static boolean canResetPlugin(CommandSender sender) {
        if(ReportRTS.permission != null){
            if(!ReportRTS.permission.has(sender, "reportrts.command.reset")) {
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.reset"));
                return false;
            }
            return true;
        }
        if(!sender.hasPermission("reportrts.command.reset")) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.reset"));
            return false;
        }
        return true;
    }

    public static boolean canPutTicketOnHold(CommandSender sender) {
        if(ReportRTS.permission != null) {
            if(!ReportRTS.permission.has(sender, "reportrts.command.hold")) {
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.hold"));
                return false;
            }
            return true;
        }
        if(!sender.hasPermission("reportrts.command.hold")) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.hold"));
            return false;
        }
        return true;
    }

    public static boolean canClaimTicket(CommandSender sender) {
        if(ReportRTS.permission != null){
            if(!ReportRTS.permission.has(sender, "reportrts.command.claim")) {
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.claim"));
                return false;
            }
            return true;
        }
        if(!sender.hasPermission("reportrts.command.claim")) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.claim"));
            return false;
        }
        return true;
    }

    public static boolean canListStaff(CommandSender sender) {
        if(ReportRTS.permission != null) {
            if(!ReportRTS.permission.has(sender, "reportrts.command.list")){
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.list"));
                return false;
            }
            return true;
        }
        if(!sender.hasPermission("reportrts.command.list")) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.list"));
            return false;
        }
        return true;
    }

    public static boolean canBroadcast(CommandSender sender) {
        if(ReportRTS.permission != null) {
            if(!ReportRTS.permission.has(sender, "reportrts.command.broadcast")) {
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.broadcast"));
                return false;
            }
            return true;
        }
        if(!sender.hasPermission("reportrts.command.broadcast")) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.broadcast"));
            return false;
        }
        return true;
    }

    public static boolean canCheckStats(CommandSender sender) {
        if(ReportRTS.permission != null) {
            if(!ReportRTS.permission.has(sender, "reportrts.command.stats")) {
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.stats"));
                return false;
            }
            return true;
        }
        if(!sender.hasPermission("reportrts.command.stats")) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.stats"));
            return false;
        }
        return true;
    }

    public static boolean canOverride(CommandSender sender) {
        if(ReportRTS.permission != null) {
            if(!ReportRTS.permission.has(sender, "reportrts.override")) {
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.override"));
                return false;
            }
            return true;
        }
        if(!sender.hasPermission("reportrts.override")) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.override"));
            return false;
        }
        return true;
    }

    public static boolean canSeeHelpPage(CommandSender sender) {
        if(ReportRTS.permission != null) {
            if(!ReportRTS.permission.has(sender, "reportrts.command.help")) {
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.help"));
                return false;
            }
            return true;
        }
        if(!sender.hasPermission("reportrts.command.help")) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.help"));
            return false;
        }
        return true;
    }

    public static boolean canManageNotifications(CommandSender sender) {
        if(ReportRTS.permission != null) {
            if(!ReportRTS.permission.has(sender, "reportrts.command.notifications")) {
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.notifications"));
                return false;
            }
            return true;
        }
        if(!sender.hasPermission("reportrts.command.notifications")) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.notifications"));
            return false;
        }
        return true;
    }

    public static boolean canAssignTickets(CommandSender sender) {
        if(ReportRTS.permission != null) {
            if(!ReportRTS.permission.has(sender, "reportrts.command.assign")) {
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.assign"));
                return false;
            }
            return true;
        }
        if(!sender.hasPermission("reportrts.command.assign")) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.assign"));
            return false;
        }
        return true;
    }

    public static boolean canReopenTicket(CommandSender sender) {
        if(ReportRTS.permission != null) {
            if(!ReportRTS.permission.has(sender, "reportrts.command.reopen")) {
                sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.reopen"));
                return false;
            }
            return true;
        }
        if(!sender.hasPermission("reportrts.command.reopen")) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.reopen"));
            return false;
        }
        return true;
    }
}
