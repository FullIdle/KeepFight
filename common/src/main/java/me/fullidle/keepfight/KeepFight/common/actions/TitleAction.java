package me.fullidle.keepfight.KeepFight.common.actions;

import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class TitleAction implements IAction {
    private final String value;

    public TitleAction(String value) {
        this.value = value;
    }

    @Override
    public void execute(Player player) {
        player.sendTitle(value, "", 7, 300, 7);
    }
}
