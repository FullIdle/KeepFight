package me.fullidle.keepfight.KeepFight;

import me.fullidle.ficore.ficore.common.api.event.ForgeEvent;
import me.fullidle.keepfight.KeepFight.common.SomeData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ForgeListener implements Listener {
    @EventHandler
    public void onForge(ForgeEvent event){
        SomeData.kfPlugin.forgeEvent(event);
    }
}
