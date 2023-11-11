package me.fullidle.keepfight.KeepFight;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.BackToMainMenu;
import lombok.SneakyThrows;
import net.minecraft.entity.player.EntityPlayerMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main extends JavaPlugin implements Listener {
    @SneakyThrows
    @Override
    public void onEnable() {
        getCommand("keepfight").setExecutor(this);
        getLogger().info("插件已经载入!");
        getServer().getPluginManager().registerEvents(this,this);
    }

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage("非玩家不可以用");
            return false;
        }
        Player player = (Player) sender;
        EntityPlayerMP spe = Pixelmon.storageManager.getParty(player.getUniqueId()).getPlayer();
        BattleControllerBase battle = BattleRegistry.getBattle(spe);
        if (battle == null){
            sender.sendMessage("§c你不在对局中!");
            return false;
        }
        for (PlayerParticipant battlePlayer : battle.getPlayers()) {
            List<PixelmonWrapper> list = Arrays.asList(battlePlayer.allPokemon);
            battlePlayer.sendMessage(new BackToMainMenu(true, true, new ArrayList<>(list)));
        }
        return false;
    }
}
