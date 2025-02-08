package me.fullidle.keepfight.KeepFight.common;

import me.fullidle.keepfight.KeepFight.common.actions.IAction;
import me.fullidle.keepfight.KeepFight.common.plugin.KFPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SomeData {
    public static Plugin main;
    public static KFPlugin kfPlugin;
    public static String[] help;
    public static IAction[] actions;
    public static final Map<Player, BukkitRunnable> titleTipsDelay = new HashMap<>();
}
