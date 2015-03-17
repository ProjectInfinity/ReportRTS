package com.nyancraft.reportrts.util;

import java.text.MessageFormat;

import org.bukkit.ChatColor;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.ReportRTS;

public class Message {

    private static String parse(String key, Object ... params ){
        Object prop = ReportRTS.getMessageHandler().messageMap.get(key);
        if(prop == null) {
            if(!ReportRTS.getMessageHandler().getMessageConfig().getDefaults().contains(key))
                return "Missing message <" + key + "> in ReportRTS/messages.yml, no default found.";
            ReportRTS.getMessageHandler().messageMap.put(key, ReportRTS.getMessageHandler().getMessageConfig().getDefaults().getString(key));
            ReportRTS.getMessageHandler().getMessageConfig().set(key, ReportRTS.getMessageHandler().getMessageConfig().getDefaults().getString(key));
            prop = ReportRTS.getMessageHandler().getMessageConfig().getDefaults().getString(key);
            ReportRTS.getMessageHandler().saveMessageConfig();
        }
        return MessageFormat.format(parseColors((String) prop), params);
    }

    public static String parseColors(String msg){
        String message = msg;
        for(ChatColor color : ChatColor.values()){
            String colorKey = "%" + color.name().toLowerCase() + "%";
            if(message.contains(colorKey)){
                message = message.replaceAll(colorKey, color.toString());
            }
        }
        return message;
    }

    public static void debug(String name, String className, double start, String cmd, String[] args){
        String arguments = RTSFunctions.implode(args, " ");
        ReportRTS.getPlugin().getLogger().info(name + " " + className + " took " + RTSFunctions.getTimeSpent(start) + "ms: " + cmd + " " + arguments);
    }

    /** Easy access messages below. **/

    public static String broadcast(Object ... params) {
        return parse("broadcast", params);
    }

    public static String banUser(String sender, String player) {
        return parse("ban-user", sender, player);
    }

    public static String banRemove(String sender, String player) {
        return parse("ban-remove", sender, player);
    }

    public static String error(Object ... params) {
        return parse("error", params);
    }

    public static String errorBanned() {
        return parse("error-banned");
    }

    public static String errorBanUser(String player) {
        return parse("error-ban-user", player);
    }

    public static String errorUnbanUser(String player) {
        return parse("error-unban-user", player);
    }

    public static String errorPermission(String ... params) {
        return parse("error-permission", params);
    }

    public static String errorTicketStatus() {
        return parse("error-ticket-status");
    }

    public static String errorTicketNotClosed(String ticketId) {
        return parse("error-ticket-not-closed",ticketId);
    }

    public static String errorTicketNaN(String param) {
        return parse("error-ticket-nan", param);
    }

    public static String errorTicketClaim(int ticketId, String player) {
        return parse("error-ticket-claim", ticketId, player);
    }

    public static String errorTicketOwner() {
        return parse("error-ticket-owner");
    }

    public static String errorUserNotExists(String player) {
        return parse("error-user-not-exists", player);
    }

    public static String errorUserNotSpecified() {
        return parse("error-user-not-specified");
    }

    public static String teleport(String ticketId) {
        return parse("teleport", ticketId);
    }

    public static String teleportXServer(String cmd) {
        return parse("teleport-x-server", cmd);
    }

    public static String ticketAssign(String player, int ticketId) {
        return parse("ticket-assign", player, ticketId);
    }

    public static String ticketAssignUser(String player) {
        return parse("ticket-assign-user", player);
    }

    public static String ticketUnresolved(Object ... params) {
        return parse("ticket-unresolved", params);
    }

    public static String ticketUnresolvedHeld(Object ... params) {
        return parse("ticket-unresolved-held", params);
    }

    public static String ticketUnclaim(String player, String ticketId) {
        return parse("ticket-unclaim", player, ticketId);
    }

    public static String ticketUnclaimUser(String player, int ticketId) {
        return parse("ticket-unclaim-user", player, ticketId);
    }

    public static String ticketComment(String ticketId, String player) {
        return parse("ticket-comment", ticketId, player);
    }

    public static String ticketCommentUser(String ticketId) {
        return parse("ticket-comment-user", ticketId);
    }

    public static String ticketClaim(String player, String ticketId) {
        return parse("ticket-claim", player, ticketId);
    }

    public static String ticketClaimUser(String player) {
        return parse("ticket-claim-user", player);
    }

    public static String ticketHold(String ticketId, String player) {
        return parse("ticket-hold", ticketId, player);
    }

    public static String ticketHoldText(String ... params) {
        return parse("ticket-hold-text", params);
    }

    public static String ticketHoldUser(String player, int ticketId) {
        return parse("ticket-hold-user", player, ticketId);
    }

    public static String ticketClose(String ticketId, String player) {
        return parse("ticket-close", ticketId, player);
    }

    public static String ticketCloseUser(String ticketId, String player) {
        return parse("ticket-close-user", ticketId, player);
    }

    public static String ticketCloseOffline() {
        return parse("ticket-close-offline");
    }

    public static String ticketCloseOfflineMulti(int amount, String cmd) {
        return parse("ticket-close-offline-multi", amount, cmd);
    }

    public static String ticketCloseText(String ... params) {
        return parse("ticket-close-text", params);
    }

    public static String ticketDuplicate() {
        return parse("ticket-duplicate");
    }

    public static String ticketNotExists(int ticketId) {
        return parse("ticket-not-exists", ticketId);
    }

    public static String ticketNotClaimed(int ticketId) {
        return parse("ticket-not-claimed", ticketId);
    }

    public static String ticketNotOpen(int ticketId) {
        return parse("ticket-not-open", ticketId);
    }

    public static String ticketText(String message) {
        return parse("ticket-text", message);
    }

    public static String ticketOpen(String player, String ticketId) {
        return parse("ticket-open", player, ticketId);
    }

    public static String ticketOpenUser(String ticketId) {
        return parse("ticket-open-user", ticketId);
    }

    public static String ticketReopen(String player, String ticketId) {
        return parse("ticket-reopen", player, ticketId);
    }

    public static String ticketReadNone() {
        return parse("ticket-read-none");
    }
    public static String ticketReadNoneSelf() {
        return parse("ticket-read-none-self");
    }

    public static String ticketReadNoneHeld() {
        return parse("ticket-read-none-held");
    }

    public static String ticketReadNoneClosed() {
        return parse("ticket-read-none-closed");
    }

    public static String ticketTooShort(int words) {
        return parse("ticket-too-short", words);
    }

    public static String ticketTooMany() {
        return parse("ticket-too-many");
    }

    public static String ticketTooFast(long ticketId) {
        return parse("ticket-too-fast", ticketId);
    }

    public static String staffListSeparator(String ... params) {
        return parse("staff-list-separator", params);
    }

    public static String staffListEmpty(String ... params) {
        return parse("staff-list-empty", params);
    }

    public static String staffListOnline(String ... params) {
        return parse("staff-list-online", params);
    }

    public static String outdated(String version) {
        return parse("plugin-outdated", version);
    }

    public static String setup() {
        return parse("plugin-not-setup");
    }
}
