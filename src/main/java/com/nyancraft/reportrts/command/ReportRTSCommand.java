package com.nyancraft.reportrts.command;

import java.sql.ResultSet;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.persistence.Database;
import com.nyancraft.reportrts.persistence.DatabaseManager;
import com.nyancraft.reportrts.persistence.QueryGen;
import com.nyancraft.reportrts.util.Message;

public class ReportRTSCommand implements CommandExecutor{

    private ReportRTS plugin;
    private ResultSet rs;
    private Database dbManager;

    public ReportRTSCommand(ReportRTS plugin) {
        this.plugin = plugin;
        this.dbManager = DatabaseManager.getDatabase();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0) return false;
        try{
            switch(SubCommands.valueOf(args[0].toUpperCase())){

            case RELOAD:
                if(!RTSPermissions.canReloadPlugin(sender)) return true;
                plugin.reloadPlugin();
                sender.sendMessage(ChatColor.YELLOW + "[ReportRTS] Reloaded configuration and requests.");
                break;

            case BAN:
                if(!RTSPermissions.canBanUser(sender)) return true;
                if(!dbManager.setUserStatus(args[1], 1)){
                    sender.sendMessage(Message.parse("generalInternalError", "Cannot ban " + args[1] + " from filing requests."));
                    return true;
                }
                RTSFunctions.messageMods(Message.parse("banUser", sender.getName(), args[1]));
                break;

            case UNBAN:
                if(!RTSPermissions.canBanUser(sender)) return true;
                if(!dbManager.setUserStatus(args[1], 0)){
                    sender.sendMessage(Message.parse("generalInternalError", "Cannot unban " + args[1] + " from filing requests."));
                    return true;
                }
                RTSFunctions.messageMods(Message.parse("unbanUser", sender.getName(), args[1]));
                break;

            case RESET:
                if(!RTSPermissions.canResetPlugin(sender)) return true;
                if(!dbManager.resetDB()){
                    sender.sendMessage(ChatColor.RED + "[ReportRTS] An unexpected error occured when attempting to reset the plugin.");
                    return true;
                }
                plugin.reloadPlugin();
                sender.sendMessage(ChatColor.GOLD + "[ReportRTS] You deleted all users and requests from ReportRTS.");
                plugin.getLogger().log(Level.INFO, sender.getName() + " deleted all users and requests from ReportRTS!");
                break;

            case STATS:
                if(!RTSPermissions.canCheckStats(sender)) return true;
                try{
                    rs = dbManager.getHandledBy(args[1]);
                    int currentHeld = 0;
                    int currentClaimed = 0;
                    int totalCompleted = 0;
                    while(rs.next()){
                        if(rs.getInt("status") == 1) currentClaimed++;
                        if(rs.getInt("status") == 2) currentHeld++;
                        if(rs.getInt("status") == 3) totalCompleted++;
                    }
                    rs.close();
                    sender.sendMessage(ChatColor.YELLOW + "---- Stats for " + args[1] + " ----");
                    sender.sendMessage(ChatColor.YELLOW + "Currently claimed requests: " + currentClaimed);
                    sender.sendMessage(ChatColor.YELLOW + "Currently held requests: " + currentHeld);
                    sender.sendMessage(ChatColor.YELLOW + "Total completed requests: " + totalCompleted);
                }catch(ArrayIndexOutOfBoundsException e){
                    return false;
                }
                break;
            // TODO: Only temporary and for SQLite users. Once I fix the SQLite issue, this will no longer be needed.
            case UPGRADE:
                if(!sender.isOp() || plugin.useMySQL) return true;
                if(RTSFunctions.checkColumns()) return true;
                DatabaseManager.getConnection().createStatement().executeUpdate("ALTER TABLE \"reportrts_request\" RENAME TO \"requests_temp\"");
                DatabaseManager.getConnection().createStatement().executeUpdate(QueryGen.createRequestTable());
                DatabaseManager.getConnection().createStatement().executeUpdate("INSERT INTO \"reportrts_request\" (\"id\", \"user_id\", \"mod_id\", \"mod_timestamp\", \"mod_comment\", \"tstamp\", \"world\", \"x\", \"y\", \"z\", \"text\", \"status\", \"notified_of_completion\") SELECT \"id\", \"user_id\", \"mod_id\", \"mod_timestamp\", \"mod_comment\", \"tstamp\", \"world\", \"x\", \"y\", \"z\", \"text\", \"status\", \"notified_of_completion\" FROM \"requests_temp\"");
                DatabaseManager.getConnection().createStatement().executeUpdate("DROP TABLE requests_temp");
                sender.sendMessage(ChatColor.YELLOW + "Hopefully everything went alright. Please double check it though! Remember to /reportrts reload !!");
                break;

            case HELP:
                if(!RTSPermissions.canSeeHelpPage(sender)) return true;
                sender.sendMessage(ChatColor.GREEN + "====[ " + ChatColor.GOLD + "ReportRTS Help " + ChatColor.GREEN + "]====");
                sender.sendMessage(ChatColor.RED + "/check " + ChatColor.GOLD + ChatColor.BOLD + " [STATUS] [ID/PAGE]" + ChatColor.RESET + ChatColor.YELLOW + " - See request details");
                sender.sendMessage(ChatColor.RED + "/claim " + ChatColor.GOLD + ChatColor.BOLD + " [ID]" + ChatColor.RESET + ChatColor.YELLOW + " - Claim request, stops toe stepping");
                sender.sendMessage(ChatColor.RED + "/complete " + ChatColor.GOLD + ChatColor.BOLD + " [ID] [COMMENT]" + ChatColor.RESET + ChatColor.YELLOW + " - Mark request as complete");
                sender.sendMessage(ChatColor.RED + "/hold " + ChatColor.GOLD + ChatColor.BOLD + " [ID] [COMMENT]" + ChatColor.RESET + ChatColor.YELLOW + " - Put request on hold");
                sender.sendMessage(ChatColor.RED + "/modreq " + ChatColor.GOLD + ChatColor.BOLD + " [MESSAGE]" + ChatColor.RESET + ChatColor.YELLOW + " - File a request");
                sender.sendMessage(ChatColor.RED + "/modlist " + ChatColor.YELLOW + " - See online staff");
                sender.sendMessage(ChatColor.RED + "/mod-broadcast " + ChatColor.GOLD + ChatColor.BOLD + " [MESSAGE]" + ChatColor.RESET + ChatColor.YELLOW + " - Send a message to all online staff");
                sender.sendMessage(ChatColor.RED + "/reportrts " + ChatColor.GOLD + ChatColor.BOLD + " [ACTION]" + ChatColor.RESET + ChatColor.YELLOW + " - General command for ReportRTS");
                sender.sendMessage(ChatColor.RED + "/reopen " + ChatColor.GOLD + ChatColor.BOLD + " [ID]" + ChatColor.RESET + ChatColor.YELLOW + " - Reopen a held or closed request");
                sender.sendMessage(ChatColor.RED + "/tp-id " + ChatColor.GOLD + ChatColor.BOLD + " [ID]" + ChatColor.RESET + ChatColor.YELLOW + " - Teleports to specified request");
                sender.sendMessage(ChatColor.RED + "/unclaim " + ChatColor.GOLD + ChatColor.BOLD + " [ID]" + ChatColor.RESET + ChatColor.YELLOW + " - Unclaim request");
                break;
            }
        }catch(Exception e){
            return false;
        }
        return true;
    }

    private enum SubCommands{
        RELOAD,
        BAN,
        UNBAN,
        RESET,
        STATS,
        UPGRADE,
        HELP
    }
}
