package com.nyancraft.reportrts.command.sub;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.Ticket;

import com.nyancraft.reportrts.persistence.DataProvider;
import com.nyancraft.reportrts.util.BungeeCord;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReadTicket {

    private static ReportRTS plugin = ReportRTS.getPlugin();
    private static DataProvider data = plugin.getDataProvider();
    private static SimpleDateFormat sdf = new SimpleDateFormat("MMM.dd kk:mm z");
    private static String substring;

    /**
     * Initial handling of the Read sub-command.
     * @param sender player that sent the command
     * @param args arguments
     * @return true if command handled correctly
     */
    public static boolean handleCommand(CommandSender sender, String[] args) {

        // Not enough arguments to be anything but "/ticket read".
        if(args.length < 2) return viewPage(sender, 1);

        // We need to figure out what form of action the user has specified.
        switch(args[1].toUpperCase()) {

            case "P":
            case "PAGE":
                if(args.length < 3) return viewPage(sender, 1);
                return viewPage(sender, RTSFunctions.isNumber(args[2]) ? Integer.parseInt(args[2]) : 1);

            case "H":
            case "HELD":
                if(args.length < 3) return viewHeld(sender, 1);
                return viewHeld(sender, RTSFunctions.isNumber(args[2]) ? Integer.parseInt(args[2]) : 1);

            case "C":
            case "CLOSED":
                if(args.length < 3) return viewClosed(sender, 1);
                return viewClosed(sender, RTSFunctions.isNumber(args[2]) ? Integer.parseInt(args[2]) : 1);

            case "S":
            case "SERVER":
                if(args.length < 3) return viewServer(sender, BungeeCord.getServerName(), 1);
                if(args.length == 4) return viewServer(sender, args[3], 1);
                if(args.length >= 5) return viewServer(sender, args[3], RTSFunctions.isNumber(args[4]) ? Integer.parseInt(args[4]) : 1);
                break;

            case "SELF":
                return viewSelf(sender);

            default:
                // Defaults to this if not found. In this case we need to figure out what the command is trying to do.
                if(RTSFunctions.isNumber(args[1])) return viewId(sender, Integer.parseInt(args[1]));
                sender.sendMessage(Message.parse("generalInternalError", "No valid action specified."));
                break;
        }

        return true;
    }

    /**
     * View specified ticket.
     * @param sender commandsender
     * @param id ticket number
     * @return true if command handled correctly
     */
    private static boolean viewId(CommandSender sender, int id) {

        if(!RTSPermissions.canReadAll(sender)) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.check"));
            return true;
        }

        Ticket ticket = plugin.tickets.get(id);

        // Request does not exist in the requestMap and must be retrieved from the database.
        if(ticket == null) {

            ticket = data.getTicket(id);

            if(ticket == null) {
                sender.sendMessage(Message.parse("generalRequestNotFound", Integer.toString(id)));
                return true;
            }
        }

        // Sets the colour of the player's name depending on whether they are online or not.
        ChatColor online = (RTSFunctions.isUserOnline(ticket.getUUID())) ? ChatColor.GREEN : ChatColor.RED;

        String date = sdf.format(new java.util.Date(ticket.getTimestamp() * 1000));
        ChatColor statusColor = null;
        String status = null;
        if(ticket.getStatus() == 0){
            status = "Open";
            statusColor = ChatColor.YELLOW;
        }
        if(ticket.getStatus() == 1){
            status = "Claimed";
            statusColor = ChatColor.RED;
        }
        if(ticket.getStatus() == 2){
            status = "On Hold";
            statusColor = ChatColor.LIGHT_PURPLE;
        }
        if(ticket.getStatus() == 3){
            status = "Closed";
            statusColor = ChatColor.GREEN;
        }

        // Compile a response for the user.
        sender.sendMessage(ChatColor.AQUA + "--------- " + "Ticket #" + ticket.getId() + " - " + statusColor + status + ChatColor.AQUA + " ---------");
        sender.sendMessage(ChatColor.YELLOW + "Opened by" + online + " " + ticket.getName() + ChatColor.YELLOW + " at " +  ChatColor.GREEN + date + ChatColor.YELLOW + " at X:" + ChatColor.GREEN + ticket.getX() + ChatColor.YELLOW + ", Y:" + ChatColor.GREEN + ticket.getY() + ChatColor.YELLOW + ", Z:" + ChatColor.GREEN + ticket.getZ());
        sender.sendMessage(ChatColor.GRAY + ticket.getMessage());
        if(ticket.getStatus() == 1) {
            long Millis = (System.currentTimeMillis() - (ticket.getModTimestamp()) * 1000);
            sender.sendMessage(ChatColor.LIGHT_PURPLE + String.format("Claimed for: %d hours, %d minutes, %d seconds",
                    Millis/(1000*60*60), (Millis%(1000*60*60))/(1000*60), ((Millis%(1000*60*60))%(1000*60))/1000) + " by " + ticket.getModName());
        }
        if(ticket.getModComment() != null && ticket.getStatus() >= 2) {
            sender.sendMessage(ChatColor.YELLOW + "Comment: " + ChatColor.DARK_GREEN + ticket.getModComment());
        }
        return true;
    }

    /**
     * View the specified page. Defaults to 1.
     * @param sender player that sent the command
     * @param page page number
     * @return true if command handled correctly
     */
    private static boolean viewPage(CommandSender sender, int page) {

        if(!RTSPermissions.canReadAll(sender)) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.check"));
            return true;
        }

        if(page < 0) page = 1;
        int a = page * plugin.ticketsPerPage;

        // Compile a response for the user.
        sender.sendMessage(ChatColor.AQUA + "--------- " + plugin.tickets.size() + " Tickets -" + ChatColor.YELLOW + " Open " + ChatColor.AQUA + "---------");
        if(plugin.tickets.size() == 0) sender.sendMessage(Message.parse("checkNoRequests"));

        List<Ticket> tmpList = new ArrayList<>(plugin.tickets.values());

        // Sets the start location of the "cursor".
        for(int i = (page * plugin.ticketsPerPage) - plugin.ticketsPerPage; i < a && i < plugin.tickets.size(); i++) {
            if(i < 0) i = 1;
            Ticket ticket = tmpList.get(i);

            if(plugin.hideWhenOffline && !RTSFunctions.isUserOnline(ticket.getUUID())){
                a++;
                continue;
            }

            substring = RTSFunctions.shortenMessage(ticket.getMessage());

            substring = (ticket.getStatus() == 1) ? ChatColor.LIGHT_PURPLE + "Claimed by " + ticket.getModName() : ChatColor.GRAY + substring;
            String bungeeServer = (ticket.getBungeeCordServer().equals(BungeeCord.getServer()) ? "" : "[" + ChatColor.GREEN + ticket.getBungeeCordServer() + ChatColor.RESET + "] ");
            if(plugin.fancify && (sender instanceof Player) && ticket.getMessage().length() >= 20) {
                PacketContainer chat = new PacketContainer(PacketType.Play.Server.CHAT);
                chat.getChatComponents().write(0, WrappedChatComponent.fromJson("{\"text\":\"" + bungeeServer + ChatColor.GOLD + "#" + ticket.getId() + " "
                        + RTSFunctions.getTimeAgo(ticket.getTimestamp()) + " by " + ((RTSFunctions.isUserOnline(ticket.getUUID())) ? ChatColor.GREEN : ChatColor.RED)
                        + ticket.getName() + ChatColor.GOLD + " - " + "\", \"extra\":[{\"text\":\"" + JSONObject.escape(substring) + "\",\"color\":\"" + (ticket.getStatus() == 1 ? "light_purple" :"gray") + "\",\"hoverEvent\":" +
                        "{\"action\":\"show_text\",\"value\":\"" + JSONObject.escape(RTSFunctions.separateText(ticket.getMessage(), 6)) + "\"}}]}"));
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket((Player) sender, chat);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                sender.sendMessage(bungeeServer + ChatColor.GOLD + "#" + ticket.getId() + " " + RTSFunctions.getTimeAgo(ticket.getTimestamp())
                        + " by " + ((RTSFunctions.isUserOnline(ticket.getUUID())) ? ChatColor.GREEN : ChatColor.RED) + ticket.getName() + ChatColor.GOLD + " - " + substring);
            }
        }

        return true;
    }

    /**
     * View tickets put on hold.
     * @param sender player that sent the command
     * @param page page number
     * @return true if command handled correctly
     */
    private static boolean viewHeld(CommandSender sender, int page) {

        if(!RTSPermissions.canReadAll(sender)) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.check"));
            return true;
        }

        // Set cursor start position.
        int i = (page * plugin.ticketsPerPage) - plugin.ticketsPerPage;

        LinkedHashMap<Integer, Ticket> tickets = data.getTickets(2, i, plugin.ticketsPerPage);

        if(tickets ==  null) {
            sender.sendMessage(Message.parse("generalInternalError", "Can't read held tickets, see console for errors."));
            return true;
        }

        sender.sendMessage(ChatColor.AQUA + "--------- " + tickets.size() + " Tickets -" + ChatColor.YELLOW + " Held " + ChatColor.AQUA + "---------");
        if(tickets.size() == 0) sender.sendMessage(Message.parse("holdNoRequests"));

        for(Map.Entry<Integer, Ticket> entry : tickets.entrySet()) {

            Ticket ticket = entry.getValue();

            substring = RTSFunctions.shortenMessage(ticket.getMessage());

            ChatColor online = (RTSFunctions.isUserOnline(ticket.getUUID()) ? ChatColor.GREEN : ChatColor.RED);
            String bungeeServer = (ticket.getBungeeCordServer().equals(BungeeCord.getServer()) ? "" :  "[" + ChatColor.GREEN + ticket.getBungeeCordServer() + ChatColor.RESET + "] ");

            if(plugin.fancify && (sender instanceof Player) && ticket.getMessage().length() >= 20) {
                PacketContainer chat = new PacketContainer(PacketType.Play.Server.CHAT);
                chat.getChatComponents().write(0, WrappedChatComponent.fromJson("{\"text\":\"" + bungeeServer + ChatColor.GOLD + "#" + ticket.getId() + " "
                        + sdf.format(new java.util.Date(ticket.getTimestamp() * 1000)) + " by " + (RTSFunctions.isUserOnline(ticket.getUUID()) ? ChatColor.GREEN : ChatColor.RED)
                        + ticket.getName() + ChatColor.GOLD + " - " + "\", \"extra\":[{\"text\":\"" + JSONObject.escape(substring) + "\",\"color\":\"gray\",\"hoverEvent\":" +
                        "{\"action\":\"show_text\",\"value\":\"" + JSONObject.escape(RTSFunctions.separateText(ticket.getMessage(), 6)) + "\"}}]}"));
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket((Player) sender, chat);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                sender.sendMessage(bungeeServer + ChatColor.GOLD + "#" + ticket.getId() + " " + sdf.format(new java.util.Date(ticket.getTimestamp() * 1000))
                        + " by " + online + ticket.getName() + ChatColor.GOLD + " - " + ChatColor.GRAY + substring);
            }
        }
        return true;
    }

    /**
     * View tickets that have been resolved.
     * @param sender player that sent the command
     * @param page page number
     * @return true if command handled correctly
     */
    private static boolean viewClosed(CommandSender sender, int page) {

        if(!RTSPermissions.canReadAll(sender)) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.check"));
            return true;
        }

        // Set cursor position.
        int i = (page * plugin.ticketsPerPage) - plugin.ticketsPerPage;

        LinkedHashMap<Integer, Ticket> tickets = data.getTickets(3, i, plugin.ticketsPerPage);

        if(tickets ==  null) {
            sender.sendMessage(Message.parse("generalInternalError", "Can't read closed tickets, see console for errors."));
            return true;
        }

        sender.sendMessage(ChatColor.AQUA + "--------- " + tickets.size() + " Tickets -" + ChatColor.YELLOW + " Closed " + ChatColor.AQUA + "--------- ");
        if(tickets.size() == 0) sender.sendMessage(Message.parse("closedNoRequests"));

        for(Map.Entry<Integer, Ticket> entry : tickets.entrySet()) {

            Ticket ticket = entry.getValue();

            substring = RTSFunctions.shortenMessage(ticket.getMessage());

            ChatColor online = (RTSFunctions.isUserOnline(ticket.getUUID()) ? ChatColor.GREEN : ChatColor.RED);
            String bungeeServer = (ticket.getBungeeCordServer().equals(BungeeCord.getServer()) ? "" :  "[" + ChatColor.GREEN + ticket.getBungeeCordServer() + ChatColor.RESET + "] ");

            if(plugin.fancify && (sender instanceof Player) && ticket.getMessage().length() >= 20) {
                PacketContainer chat = new PacketContainer(PacketType.Play.Server.CHAT);
                chat.getChatComponents().write(0, WrappedChatComponent.fromJson("{\"text\":\"" + bungeeServer + ChatColor.GOLD + "#" + ticket.getId() + " "
                        + sdf.format(new java.util.Date(ticket.getTimestamp() * 1000)) + " by " + (RTSFunctions.isUserOnline(ticket.getUUID()) ? ChatColor.GREEN : ChatColor.RED)
                        + ticket.getName() + ChatColor.GOLD + " - " + "\", \"extra\":[{\"text\":\"" + JSONObject.escape(substring) + "\",\"color\":\"gray\",\"hoverEvent\":" +
                        "{\"action\":\"show_text\",\"value\":\"" + JSONObject.escape(RTSFunctions.separateText(ticket.getMessage(), 6)) + "\"}}]}"));
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket((Player) sender, chat);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                sender.sendMessage(bungeeServer + ChatColor.GOLD + "#" + ticket.getId() + " " + sdf.format(new java.util.Date(ticket.getTimestamp() * 1000))
                        + " by " + online + ticket.getName() + ChatColor.GOLD + " - " + ChatColor.GRAY + substring);
            }
        }
        return true;
    }

    /**
     * View open tickets on a specific server.
     * @param sender player that sent the command
     * @param server name of target server
     * @param page page number
     * @return true if command handled correctly.
     */
    private static boolean viewServer(CommandSender sender, String server, int page) {

        if(!RTSPermissions.canReadAll(sender)) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.check"));
            return true;
        }

        if(page < 0) page = 0;

        // Set cursor position.
        int a = (page * plugin.ticketsPerPage) - plugin.ticketsPerPage;

        // Compile a response for the user.
        sender.sendMessage(ChatColor.AQUA + "--------- " + plugin.tickets.size() + " Tickets From Server " + server + " -" + ChatColor.YELLOW + " Open " + ChatColor.AQUA + "---------");
        if(plugin.tickets.size() == 0) sender.sendMessage(Message.parse("checkNoRequests"));

        List<Ticket> tmpList = new ArrayList<>(plugin.tickets.values());

        for(int i = (page * plugin.ticketsPerPage) - plugin.ticketsPerPage; i < a && i < plugin.tickets.size(); i++){
            Ticket ticket = tmpList.get(i);
            if(plugin.hideWhenOffline && !RTSFunctions.isUserOnline(ticket.getUUID()) || !ticket.getBungeeCordServer().equals(server)){
                a++;
                continue;
            }
            substring = RTSFunctions.shortenMessage(ticket.getMessage());
            substring = (ticket.getStatus() == 1) ? ChatColor.LIGHT_PURPLE + "Claimed by " + ticket.getModName() : ChatColor.GRAY + substring;

            if(plugin.fancify && (sender instanceof Player) && ticket.getMessage().length() >= 20) {
                PacketContainer chat = new PacketContainer(PacketType.Play.Server.CHAT);
                chat.getChatComponents().write(0, WrappedChatComponent.fromJson("{\"text\":\"" + ChatColor.GOLD + "#" + ticket.getId() + " "
                        + sdf.format(new java.util.Date(ticket.getTimestamp() * 1000)) + " by " + ((RTSFunctions.isUserOnline(ticket.getUUID())) ? ChatColor.GREEN : ChatColor.RED)
                        + ticket.getName() + ChatColor.GOLD + " - " + "\", \"extra\":[{\"text\":\"" +JSONObject.escape(substring) + "\",\"color\":\"gray\",\"hoverEvent\":" +
                        "{\"action\":\"show_text\",\"value\":\"" + JSONObject.escape(RTSFunctions.separateText(ticket.getMessage(), 6)) + "\"}}]}"));
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket((Player) sender, chat);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.GOLD + "#" + ticket.getId() + " " + sdf.format(new java.util.Date(ticket.getTimestamp() * 1000))
                        + " by " + (RTSFunctions.isUserOnline(ticket.getUUID()) ? ChatColor.GREEN : ChatColor.RED) + ticket.getName() + ChatColor.GOLD +  " - " + substring);
            }

        }
        return true;
    }

    /**
     * View tickets opened by yourself.
     * @param sender player that sent the command
     * @return true if command handled correctly
     */
    private static boolean viewSelf(CommandSender sender) {

        if(!RTSPermissions.canReadOwn(sender)) {
            sender.sendMessage(Message.parse("generalPermissionError", "reportrts.command.check.self"));
            return true;
        }

        int openRequests = 0;
        for(Map.Entry<Integer, Ticket> entry : plugin.tickets.entrySet()) if(entry.getValue().getName().equals(sender.getName())) openRequests++;
        int i = 0;
        sender.sendMessage(ChatColor.AQUA + "--------- " + ChatColor.YELLOW + " You have " + openRequests + " unresolved tickets " + ChatColor.AQUA + "----------");
        if(openRequests == 0) sender.sendMessage(ChatColor.GOLD + "You have no open tickets at this time.");
        for(Map.Entry<Integer, Ticket> entry : plugin.tickets.entrySet()) {
            if (entry.getValue().getName().equals(sender.getName())) {
                i++;
                if (i > 5) break;
            }
            Ticket ticket = entry.getValue();
            String substring = RTSFunctions.shortenMessage(ticket.getMessage());

            substring = (ticket.getStatus() == 1) ? ChatColor.LIGHT_PURPLE + "Claimed by " + ticket.getModName() : ChatColor.GRAY + substring;
            String bungeeServer = (ticket.getBungeeCordServer().equals(BungeeCord.getServer()) ? "" : "[" + ChatColor.GREEN + ticket.getBungeeCordServer() + ChatColor.RESET + "] ");

            if(plugin.fancify && (sender instanceof Player) && ticket.getMessage().length() >= 20) {
                PacketContainer chat = new PacketContainer(PacketType.Play.Server.CHAT);
                chat.getChatComponents().write(0, WrappedChatComponent.fromJson("{\"text\":\"" + bungeeServer + ChatColor.GOLD + "#" + ticket.getId() + " "
                        + sdf.format(new java.util.Date(ticket.getTimestamp() * 1000)) + " by " + ((RTSFunctions.isUserOnline(ticket.getUUID())) ? ChatColor.GREEN : ChatColor.RED)
                        + ticket.getName() + ChatColor.GOLD + " - " + "\", \"extra\":[{\"text\":\"" + JSONObject.escape(substring) + "\",\"color\":\"gray\",\"hoverEvent\":" +
                        "{\"action\":\"show_text\",\"value\":\"" + JSONObject.escape(RTSFunctions.separateText(ticket.getMessage(), 6)) + "\"}}]}"));
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket((Player) sender, chat);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.GOLD + "#" + ticket.getId() + " " + sdf.format(new java.util.Date(ticket.getTimestamp() * 1000))
                        + " by " + (RTSFunctions.isUserOnline(ticket.getUUID()) ? ChatColor.GREEN : ChatColor.RED) + ticket.getName() + ChatColor.GOLD +  " - " + substring);
            }
        }
        return true;
    }
}