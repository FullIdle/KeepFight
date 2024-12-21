package me.fullidle.keepfight.KeepFight.common.actions;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
public class OpAction implements IAction {
    private final String value;

    public OpAction(String value) {
        this.value = value;
    }

    /*还是不安全的*/
    @Override
    public void execute(Player player) {
        boolean op = player.isOp();
        try {
            player.setOp(true);
            Bukkit.dispatchCommand(player,value);
            player.setOp(op);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            player.setOp(op);
        }
    }
}
