package com.nyancraft.reportrts.util;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.NotificationType;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class BungeeCord {

    private static List<byte[]> pendingRequests = new CopyOnWriteArrayList<>();

    private static boolean noPlayersOnline;

    private static String serverName;

    public static void processPendingRequests(){
        if(!pendingRequests.isEmpty()){
            for(byte[] toSend : pendingRequests){
                Player player = Bukkit.getOnlinePlayers().iterator().next();
                if(player != null){
                    player.sendPluginMessage(ReportRTS.getPlugin(), "BungeeCord", toSend);
                    pendingRequests.remove(toSend);
                }else{
                    break;
                }
            }
        }
    }

    public static void triggerAutoSync(){
        noPlayersOnline = Bukkit.getOnlinePlayers().size() == 0;
    }

    public static boolean isServerEmpty(){
        return noPlayersOnline;
    }

    public static String getServerName() {
        return serverName;
    }

    public static String getServer() {
        if(!ReportRTS.getPlugin().bungeeCordSupport) { return ""; }
        if(serverName != null) {
            return serverName;
        } else {
            Player player = Bukkit.getOnlinePlayers().iterator().next(); // This will error if no player is online.
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            try {
                out.writeUTF("GetServer");
            } catch(IOException e) {
                e.printStackTrace();
            }
            if(player != null) {
                player.sendPluginMessage(ReportRTS.getPlugin(), "BungeeCord", b.toByteArray());
            } else {
                pendingRequests.add(b.toByteArray());
            }
        }
        return "";
    }

    public static void setServer(String server){
        if(!ReportRTS.getPlugin().bungeeCordSupport || server == null) return;
        if(serverName == null){
            serverName = server;
            ReportRTS.getPlugin().getConfig().set("bungeecord.serverName", server);
            ReportRTS.getPlugin().saveConfig();
        }
    }

    public static void teleportUser(Player player, String targetServer, int ticketId) throws IOException{
        if(!ReportRTS.getPlugin().bungeeCordSupport || player == null || serverName == null) return;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        out.writeUTF("Forward");
        out.writeUTF(targetServer);
        out.writeUTF("ReportRTS");

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        msgout.writeUTF("TeleportNotify");
        msgout.writeInt(ticketId);
        msgout.writeUTF(player.getUniqueId().toString());
        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());

        player.sendPluginMessage(ReportRTS.getPlugin(), "BungeeCord", b.toByteArray());

        // Teleport
        ByteArrayOutputStream b1 = new ByteArrayOutputStream();
        DataOutputStream out1 = new DataOutputStream(b1);
        out1.writeUTF("Connect");
        out1.writeUTF(targetServer);

        out1.writeShort(msgbytes.toByteArray().length);
        out1.write(msgbytes.toByteArray());

        player.sendPluginMessage(ReportRTS.getPlugin(), "BungeeCord", b1.toByteArray());
    }

    public static void globalNotify(String message, int ticketId, NotificationType notifyType) throws IOException{
        if(!ReportRTS.getPlugin().bungeeCordSupport || serverName == null) return;

        String serverPrefix = (ReportRTS.getPlugin().bungeeCordServerPrefix == null || ReportRTS.getPlugin().bungeeCordServerPrefix.equals("") ? "[" + serverName + "]" : Message.parseColors(ReportRTS.getPlugin().bungeeCordServerPrefix));
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("ReportRTS");

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        msgout.writeUTF("NotifyAndSync");
        msgout.writeInt(ticketId);
        msgout.writeInt(notifyType.getCode());
        msgout.writeUTF(serverPrefix + " " + message);

        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());

        Player player = (Bukkit.getOnlinePlayers().size() == 0 ? null : Bukkit.getOnlinePlayers().iterator().next());
        if(player != null){
            player.sendPluginMessage(ReportRTS.getPlugin(), "BungeeCord", b.toByteArray());
        }else{
            pendingRequests.add(b.toByteArray());
        }
    }

    public static void notifyUser(UUID uuid, String message, int ticketId) throws IOException{
        if(!ReportRTS.getPlugin().bungeeCordSupport || serverName == null) return;

        String serverPrefix = (ReportRTS.getPlugin().bungeeCordServerPrefix == null || ReportRTS.getPlugin().bungeeCordServerPrefix.equals("") ? "[" + serverName + "]" : Message.parseColors(ReportRTS.getPlugin().bungeeCordServerPrefix));
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("ReportRTS");

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        msgout.writeUTF("NotifyUserAndSync");
        msgout.writeInt(ticketId);
        msgout.writeUTF(uuid.toString());
        msgout.writeUTF(serverPrefix + " " + message);

        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());

        Player player = (Bukkit.getOnlinePlayers().size() == 0 ? null : Bukkit.getOnlinePlayers().iterator().next());
        if(player != null){
            player.sendPluginMessage(ReportRTS.getPlugin(), "BungeeCord", b.toByteArray());
        }else{
            pendingRequests.add(b.toByteArray());
        }
    }

    public static void handleNotify(byte[] bytes){
        try{
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
            String subChannel = in.readUTF();
            if(subChannel.equals("ReportRTS")) {
                short len = in.readShort();
                byte[] msgbytes = new byte[len];
                in.readFully(msgbytes);
                DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
                String function = msgin.readUTF();
                if(function.equals("NotifyAndSync")) {
                    int ticketId = msgin.readInt();
                    NotificationType notifType = NotificationType.getTypeByCode(msgin.readInt());
                    String msg = msgin.readUTF();
                    if(notifType.getCode() == 0 || notifType.getCode() == 1) {
                        if(RTSFunctions.syncTicket(ticketId)){
                            RTSFunctions.messageStaff(msg, (notifType.getCode() == 0));
                        }
                    } else if(notifType.getCode() == 3 || notifType.getCode() == 4) {
                        RTSFunctions.messageStaff(msg, false);
                    } else if(notifType.getCode() == 2 || notifType.getCode() == 5) {
                        if(RTSFunctions.syncTicket(ticketId)) {
                            if(notifType.getCode() == 2) ReportRTS.getPlugin().notifications.put(ticketId, ReportRTS.getPlugin().tickets.get(ticketId).getUUID());
                            ReportRTS.getPlugin().tickets.remove(ticketId);
                            RTSFunctions.messageStaff(msg, (notifType.getCode() == 0));
                        }
                    } else if(notifType.getCode() == 6) {
                        ReportRTS.getPlugin().tickets.remove(ticketId);
                        RTSFunctions.messageStaff(msg, (notifType.getCode() == 0));
                    }
                } else if(function.equals("NotifyUserAndSync")) {
                    int ticketId = msgin.readInt();
                    UUID uuid = UUID.fromString(msgin.readUTF());
                    String msg = msgin.readUTF();
                    if(RTSFunctions.syncTicket(ticketId)){
                        Player player = Bukkit.getPlayer(uuid);
                        if(player != null) {
                            player.sendMessage(msg);
                            if(ReportRTS.getPlugin().getDataProvider().setNotificationStatus(ticketId, true) < 1) ReportRTS.getPlugin().getLogger().warning("Unable to set notification status to 1.");
                        }
                    }
                }else if(function.equals("TeleportNotify")) {
                    int ticketId = msgin.readInt();
                    UUID uuid = UUID.fromString(msgin.readUTF());
                    if(RTSFunctions.isUserOnline(uuid)) {
                        Player player = Bukkit.getPlayer(uuid);
                        if(player != null) {
                            player.sendMessage(Message.parse("teleportedUser",
                                    (ReportRTS.getPlugin().legacyCommands ? "/" + ReportRTS.getPlugin().commandMap.get("teleportToTicket") : "/ticket " + ReportRTS.getPlugin().commandMap.get("teleportToTicket") + " " + Integer.toString(ticketId))));
                            Bukkit.dispatchCommand(player, "ticket " + ReportRTS.getPlugin().commandMap.get("teleportToTicket") + " " + Integer.toString(ticketId));
                        } else {
                            ReportRTS.getPlugin().teleportMap.put(uuid, ticketId);
                        }
                    } else {
                        ReportRTS.getPlugin().teleportMap.put(uuid, ticketId);
                    }
                }
            } else if(subChannel.equals("GetServer")) {
                String serverName = in.readUTF();
                setServer(serverName);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
