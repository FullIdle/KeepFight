package me.fullidle.keepfight.KeepFight;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import net.minecraft.entity.player.EntityPlayerMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements CommandExecutor {
    @Override
    public void onEnable() {
        getLogger().info("§3插件已载入");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            EntityPlayerMP player = Pixelmon.storageManager.getParty(p.getUniqueId()).getPlayer();
            BattleControllerBase battle = BattleRegistry.getBattle(player);
            if (battle != null) {
                battle.pauseBattle();
                battle.endPause();
            }else{
                sender.sendMessage("§a你不在对战中");
            }
            sender.sendMessage("§a对战续命成功,有没有效果得自己试");
            return false;
        }
        sender.sendMessage("§c你不是玩家!");
        return false;
    }
}