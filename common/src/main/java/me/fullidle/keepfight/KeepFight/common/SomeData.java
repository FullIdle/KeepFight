package me.fullidle.keepfight.KeepFight.common;

import org.bukkit.plugin.Plugin;

import java.util.*;

public class SomeData {
    public static Plugin main;

    public static List<String> subCmd = Arrays.asList(
            "help",
            "rs","eb",
            "reselect","emptyBlood"
    );
    public static String[] help = new String[]{
            "rs,reselect 卡稍等时用!(抓精灵除外)","eb,emptyBlood 空血卡时用"
    };
}
