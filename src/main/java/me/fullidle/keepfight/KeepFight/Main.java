package me.fullidle.keepfight.KeepFight;

import lombok.SneakyThrows;
import me.fullidle.ficore.ficore.common.SomeMethod;
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
        Listener versionO = null;
        String version = SomeMethod.getMinecraftVersion();
        if (version.equals("1.12.2")){
            versionO = new V12();
        } else if (version.equals("1.16.5")) {
            versionO = new V16();
        }else {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    Main.this.getLogger().info("§c该插件不支持版本为:§3"+version+"§c的伺服器");
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
        getLogger().info("插件已经载入!");
    }
}
