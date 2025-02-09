package me.fullidle.keepfight.KeepFight;

import lombok.SneakyThrows;
import me.fullidle.ficore.ficore.common.SomeMethod;
import me.fullidle.keepfight.KeepFight.common.CommonUtil;
import me.fullidle.keepfight.KeepFight.common.SomeData;
import me.fullidle.keepfight.KeepFight.common.actions.*;
import me.fullidle.keepfight.KeepFight.common.commands.KFReload;
import me.fullidle.keepfight.KeepFight.v12.V12;
import me.fullidle.keepfight.KeepFight.v16.V16;
import me.fullidle.keepfight.KeepFight.v20.V20;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin {
    @SneakyThrows
    @Override
    public void onEnable() {
        SomeData.main = this;

        String version = SomeMethod.getMinecraftVersion();
        switch (version) {
            case "1.12.2":
                SomeData.kfPlugin = new V12();
                break;
            case "1.16.5":
                SomeData.kfPlugin = new V16();
                break;
            case "1.20.2":
                SomeData.kfPlugin = new V20();
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

        this.reloadConfig();

        SomeData.help = getConfig().getStringList("msg.help").toArray(new String[0]);
        SomeData.actions = getConfig().getStringList("battleTitleTips.actions").stream().map(CommonUtil::parseAction).toArray(IAction[]::new);

        getServer().getPluginManager().registerEvents(new ForgeListener(), this);
        PluginCommand command = getCommand("keepfight");
        command.setExecutor(SomeData.kfPlugin);
        getCommand("kfreload").setExecutor(KFReload.INSTANCE);
        getLogger().info("Plugin is enabled!");
    }

    @Override
    public void reloadConfig() {
        this.saveDefaultConfig();
        super.reloadConfig();

        Bukkit.getScheduler().cancelTasks(this);
        SomeData.titleTipsDelay.clear();

        SomeData.kfPlugin.reload();
    }
}
