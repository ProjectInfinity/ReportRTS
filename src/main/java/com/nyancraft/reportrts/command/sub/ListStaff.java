package com.nyancraft.reportrts.command.sub;

import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ListStaff {

    private static ReportRTS plugin = ReportRTS.getPlugin();

    /**
     * Initial handling of the Staff sub-command.
     * @param sender player that sent the command
     * @return true if command handled correctly
     */
    public static boolean handleCommand(CommandSender sender) {

        // TODO: Possible to-do. No cross server functionality!
        if(!RTSPermissions.canListStaff(sender)) return true;
        StringBuilder staff = new StringBuilder();
        String separator = Message.staffListSeparator();

        for(UUID uuid : plugin.staff) {
            Player player = plugin.getServer().getPlayer(uuid);
            if(player == null) return false;
            if(plugin.vanishSupport && sender instanceof Player) {
                if(!((Player) sender).canSee(player)) continue;
            }
            staff.append(player.getDisplayName());
            staff.append(separator);
        }
        if(staff.isEmpty()) {
            sender.sendMessage(Message.staffListEmpty());
            return true;
        }
        String staffString = staff.substring(0, staff.length() - separator.length());

        sender.sendMessage(Message.staffListOnline(staffString));
        return true;
    }
}
