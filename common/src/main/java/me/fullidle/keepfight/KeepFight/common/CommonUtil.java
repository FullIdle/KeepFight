package me.fullidle.keepfight.KeepFight.common;

import me.fullidle.keepfight.KeepFight.common.actions.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CommonUtil {
    public static void resetTitleTipsTick(Player player) {
        FileConfiguration config = SomeData.main.getConfig();
        BukkitRunnable br = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !SomeData.kfPlugin.playerInBattle(player)) {
                    removeTitleTips(player);
                    try {
                        cancel();
                    } catch (IllegalStateException ignored) {
                    }
                    return;
                }
                for (IAction action : SomeData.actions) {
                    action.execute(player);
                }
            }
        };
        long a = config.getLong("battleTitleTips.time");
        cancel(SomeData.titleTipsDelay.put(player, br));
        player.sendTitle(" ", "", 0, 1, 0);
        br.runTaskTimer(SomeData.main, a, a);
    }

    public static void removeTitleTips(Player player) {
        cancel(SomeData.titleTipsDelay.remove(player));
        player.sendTitle(" ", "", 0, 1, 0);
    }

    public static void cancel(BukkitRunnable runnable) {
        if (runnable == null) return;
        try {
            runnable.cancel();
        } catch (IllegalStateException ignored) {
        }
    }

    public static IAction parseAction(String actionText) {
        actionText = actionText.replace('&', '§');
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
