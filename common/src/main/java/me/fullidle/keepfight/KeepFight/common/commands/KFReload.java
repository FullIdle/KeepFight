package me.fullidle.keepfight.KeepFight.common.commands;

import me.fullidle.keepfight.KeepFight.common.SomeData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class KFReload implements CommandExecutor {
    private KFReload(){}
    public static final KFReload INSTANCE = new KFReload();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!commandSender.isOp()) {
            commandSender.sendMessage("§cYou don't have permission!");
            return false;
        }
        SomeData.main.reloadConfig();
        commandSender.sendMessage("§aReload Success!");
        return false;
    }
}
