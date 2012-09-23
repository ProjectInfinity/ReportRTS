package com.nyancraft.reportrts.command;

import com.nyancraft.reportrts.ReportRTS;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.util.Message;

public class ModlistCommand implements CommandExecutor{

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!RTSPermissions.canListStaff(sender)) return true;

        double start = 0;
        if(ReportRTS.getPlugin().debugMode) start = System.nanoTime();
        String staff = "";
        String separator = Message.parse("modlistMessageSeparator");

        for(String name : ReportRTS.getPlugin().moderatorMap){
            Player player = ReportRTS.getPlugin().getServer().getPlayer(name);
            if(player == null) return false;
            staff = staff + player.getDisplayName() + separator;
        }
        if(staff.length() == 0){
            sender.sendMessage(Message.parse("modlistNoMods"));
            return true;
        }
        staff = staff.substring(0, staff.length() - separator.length());

        sender.sendMessage(Message.parse("modlistMessage", staff));
        if(ReportRTS.getPlugin().debugMode) Message.debug(sender.getName(), this.getClass().getSimpleName(), start, cmd.getName(), args);
        return true;
    }
}
