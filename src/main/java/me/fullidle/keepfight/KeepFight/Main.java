package me.fullidle.keepfight.KeepFight;

import lombok.SneakyThrows;
import me.fullidle.ficore.ficore.common.SomeMethod;
import me.fullidle.keepfight.KeepFight.common.SomeData;
import me.fullidle.keepfight.KeepFight.common.actions.*;
import me.fullidle.keepfight.KeepFight.v12.V12;
import me.fullidle.keepfight.KeepFight.v16.V16;
import me.fullidle.keepfight.KeepFight.v20.V20;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin {
    @SneakyThrows
    @Override
    public void onEnable() {
        SomeData.main = this;

        saveDefaultConfig();
        SomeData.help = getConfig().getStringList("msg.help").toArray(new String[0]);
        SomeData.actions = getConfig().getStringList("battleTitleTips.actions").stream().map(Main::parseAction).toArray(IAction[]::new);

        Listener versionO;
        String version = SomeMethod.getMinecraftVersion();
        switch (version) {
            case "1.12.2":
                versionO = new V12();
                break;
            case "1.16.5":
                versionO = new V16();
                break;
            case "1.20.2":
                versionO = new V20();
                break;
            default:
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        Main.this.getLogger().info("§cThis plugin does not support servers with version §6" + version);
                        getServer().getPluginManager().disablePlugin(Main.this);
                    }
                };
                runnable.run();
                return;
        }

        getServer().getPluginManager().registerEvents(versionO, this);
        PluginCommand command = getCommand("keepfight");
        command.setExecutor((CommandExecutor) versionO);
        getLogger().info("Plugin is enabled!");
    }

    public static IAction parseAction(String actionText){
        actionText = actionText.replace('&','§');
        if (actionText.startsWith("op: ")) {
            return new OpAction(actionText.substring(4));
        }
        if (actionText.startsWith("console: ")) {
            return new ConsoleAction(actionText.substring(9));
        }
        if (actionText.startsWith("command: ")) {
            return new CommandAction(actionText.substring(9));
        }
        if (actionText.startsWith("title: ")) {
            return new TitleAction(actionText.substring(7));
        }
        return null;
    }
}
