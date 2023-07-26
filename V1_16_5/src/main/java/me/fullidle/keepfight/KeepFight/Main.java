package me.fullidle.keepfight.KeepFight;

import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements CommandExecutor {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("keepfight").setExecutor(this::onCommand);
        getLogger().info("§3插件已载入");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            ServerPlayerEntity player = StorageProxy.getParty(p.getUniqueId()).getPlayer();
            BattleController battle = BattleRegistry.getBattle(player);
            if (battle != null) {
                battle.pauseBattle();
                battle.endPause();
                if (!getConfig().getBoolean("简约模式")){
                    for (BattleParticipant participant : battle.participants) {
                        participant.updateOtherPokemon();
                        for (PixelmonWrapper wrapper : participant.allPokemon) {
                            wrapper.update();
                        }
                    }
                    battle.updatePokemonHealth();
                    battle.update();
                }
                battle.pauseBattle();
                battle.endPause();
                battle.sendToAll(getMsg(getConfig().getString("Msg.KeepFightCmdMsg")));
            }else{
                sender.sendMessage(getMsg(getConfig().getString("Msg.NotInToBattle")));
            }
            return false;
        }
        sender.sendMessage(getMsg(getConfig().getString("Msg.NoPlayer")));
        return false;
    }

    public String getMsg(String str){
        return str.replace("&","§");
    }
}