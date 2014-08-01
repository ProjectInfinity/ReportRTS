package com.nyancraft.reportrts.command;

import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.command.sub.*;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TicketCommand implements CommandExecutor {

    private ReportRTS plugin;

    public TicketCommand(ReportRTS plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        /** Argument checker, DO NOT LEAVE THIS UNCOMMENTED IN PRODUCTION *
        int i = -1;
        for(String arg : args) {
            i++;
            System.out.println("Position: " + i + " | Actual Position: " + (i + 1) + " | Argument: " + arg);
        }
        /** LOOK ABOVE **/

        if(args.length < 1) return false;

        double start = 0;
        if(plugin.debugMode) start = System.nanoTime();
        boolean result;

        // For lack of a better way. Please enlighten me if you have a suggestion to improve anything below.

        /** Read a ticket. **/
        if(args[0].equalsIgnoreCase(plugin.commandMap.get("readTicket"))) {
            result = ReadTicket.handleCommand(sender, args);
            if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
            return result;
        }
        /** Open a ticket. **/
        if(args[0].equalsIgnoreCase(plugin.commandMap.get("openTicket"))) {
            result = OpenTicket.handleCommand(sender, args);
            if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
            return result;
        }
        /** Close a ticket. **/
        if(args[0].equalsIgnoreCase(plugin.commandMap.get("closeTicket"))) {
            result = CloseTicket.handleCommand(sender, args);
            if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
            return result;
        }
        /** Reopen a ticket. **/
        if(args[0].equalsIgnoreCase(plugin.commandMap.get("reopenTicket"))) {
            result = ReopenTicket.handleCommand(sender, args);
            if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
            return result;
        }
        /** Claim a ticket. **/
        if(args[0].equalsIgnoreCase(plugin.commandMap.get("claimTicket"))) {
            result = ClaimTicket.handleCommand(sender, args);
            if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
            return result;
        }
        /** Unclaim a ticket. **/
        if(args[0].equalsIgnoreCase(plugin.commandMap.get("unclaimTicket"))) {
            result = UnclaimTicket.handleCommand(sender, args);
            if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
            return result;
        }
        /** Hold a ticket. **/
        if(args[0].equalsIgnoreCase(plugin.commandMap.get("holdTicket"))) {
            result = HoldTicket.handleCommand(sender, args);
            if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
            return result;
        }
        /** Teleport to a ticket. **/
        if(args[0].equalsIgnoreCase(plugin.commandMap.get("teleportToTicket"))) {
            result = TeleportTicket.handleCommand(sender, args);
            if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
            return result;
        }
        /** Broadcast to staff. **/
        if(args[0].equalsIgnoreCase(plugin.commandMap.get("broadcastToStaff"))) {
            result = BroadcastMessage.handleCommand(sender, args);
            if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
            return result;
        }
        /** List staff. **/
        if(args[0].equalsIgnoreCase(plugin.commandMap.get("listStaff"))) {
            result = ListStaff.handleCommand(sender);
            if(plugin.debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
            return result;
        }
        return true;
    }
}