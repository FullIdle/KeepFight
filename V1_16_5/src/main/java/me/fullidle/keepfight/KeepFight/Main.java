package me.fullidle.keepfight.KeepFight;

import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.BackToMainMenuPacket;
import lombok.SneakyThrows;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends JavaPlugin {
    @SneakyThrows
    @Override
    public void onEnable() {
        getCommand("keepfight").setExecutor(this);
        getLogger().info("插件已经载入!");
    }

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage("非玩家不可以用");
            return false;
        }
        Player player = (Player) sender;
        ServerPlayerEntity spe = StorageProxy.getParty(player.getUniqueId()).getPlayer();
        BattleController battle = BattleRegistry.getBattle(spe);
        if (battle == null){
            sender.sendMessage("§c你不在对局中!");
            return false;
        }
        for (PlayerParticipant battlePlayer : battle.getPlayers()) {
            List<PixelmonWrapper> list = Arrays.asList(battlePlayer.allPokemon);
            battlePlayer.sendMessage(new BackToMainMenuPacket(list.stream().map(r-> (r.isAlive()&&r.entity == null)).collect(Collectors.toList()), true, list.stream().map(PixelmonWrapper::getPokemonUUID).collect(Collectors.toList())));
        }
        return false;
    }
}
