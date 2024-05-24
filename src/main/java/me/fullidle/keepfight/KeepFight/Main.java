package me.fullidle.keepfight.KeepFight;

import lombok.SneakyThrows;
import me.fullidle.ficore.ficore.common.SomeMethod;
import me.fullidle.keepfight.KeepFight.common.SomeData;
import me.fullidle.keepfight.KeepFight.v12.V12;
import me.fullidle.keepfight.KeepFight.v16.V16;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
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

        Listener versionO;
        String version = SomeMethod.getMinecraftVersion();
        if (version.equals("1.12.2")){
            versionO = new V12();
        } else if (version.equals("1.16.5")) {
            versionO = new V16();
        }else {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    Main.this.getLogger().info("§cThis plugin does not support servers with version §6"+version);
                    getServer().getPluginManager().disablePlugin(Main.this);
                }
            };
            runnable.run();
            return;
        }

        getServer().getPluginManager().registerEvents(versionO,this);
        PluginCommand command = getCommand("keepfight");
        command.setExecutor((CommandExecutor) versionO);
        command.setTabCompleter((TabCompleter) versionO);
        getLogger().info("Plugin is enabled!");
    }
}
