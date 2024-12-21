package me.fullidle.keepfight.KeepFight.common.actions;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
public class CommandAction implements IAction {
    private final String value;

    public CommandAction(String value) {
        this.value = value;
    }

    @Override
    public void execute(Player player) {
        Bukkit.dispatchCommand(player,value);
    }
}
