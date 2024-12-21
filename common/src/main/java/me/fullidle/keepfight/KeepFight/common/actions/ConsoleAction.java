package me.fullidle.keepfight.KeepFight.common.actions;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
public class ConsoleAction implements IAction{
    private final String value;

    public ConsoleAction(String value){
        this.value = value;
    }

    @Override
    public void execute(Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),value);
    }
}
