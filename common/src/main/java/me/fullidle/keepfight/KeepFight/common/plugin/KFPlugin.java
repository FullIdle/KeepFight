package me.fullidle.keepfight.KeepFight.common.plugin;

import me.fullidle.ficore.ficore.common.api.event.ForgeEvent;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public abstract class KFPlugin implements TabExecutor {
    public abstract boolean playerInBattle(Player player);

    public abstract void forgeEvent(ForgeEvent event);

    public abstract void reload();
}
