package me.fullidle.keepfight.KeepFight.common;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SomeData {
    public static Plugin main;
    public static String[] help;
    public static final Map<Player, BukkitRunnable> titleTipsDelay = new HashMap<>();
}
