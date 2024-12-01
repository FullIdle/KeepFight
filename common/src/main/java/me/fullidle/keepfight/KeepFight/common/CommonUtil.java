package me.fullidle.keepfight.KeepFight.common;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CommonUtil {
    public static void resetTitleTipsTick(Player player) {
        FileConfiguration config = SomeData.main.getConfig();
        BukkitRunnable br = new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.sendTitle(config.getString("battleTitleTips.tips").replace('&','§'), "", 7, 20, 7);
                }
            }
        };
        long a = config.getLong("battleTitleTips.time");
        cancel(SomeData.titleTipsDelay.put(player, br));
        br.runTaskTimer(SomeData.main,a,a);
    }

    public static void removeTitleTips(Player player) {
        cancel(SomeData.titleTipsDelay.remove(player));
    }

    public static void cancel(BukkitRunnable runnable){
        if (runnable == null) return;
        try {
            runnable.cancel();
        } catch (IllegalStateException ignored) {
        }
    }
}
