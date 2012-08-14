package com.nyancraft.reportrts.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nyancraft.reportrts.RTSPermissions;
import com.nyancraft.reportrts.util.Message;

public class ModlistCommand implements CommandExecutor{

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!RTSPermissions.canListStaff(sender)) return true;

        Player[] players = sender.getServer().getOnlinePlayers();
        String staff = "";
        String separator = Message.parse("modlistMessageSeparator");

        for(Player player : players){
            if(RTSPermissions.isModerator(player)) staff = staff + player.getDisplayName() + separator;
        }
        if(staff.length() == 0){
            sender.sendMessage(Message.parse("modlistNoMods"));
            return true;
        }
        staff = staff.substring(0, staff.length() - separator.length());

        
        
        sender.sendMessage(Message.parse("modlistMessage", staff));
        return true;
    }
}
