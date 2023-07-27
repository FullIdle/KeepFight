package me.fullidle.keepfight.KeepFight;

import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.util.helpers.NetworkHelper;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.log.action.BattleAction;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.BackToMainMenuPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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
                battle.sendToAll(getMsg(getConfig().getString("Msg.KeepFightCmdMsg")));
                switch (getConfig().getString("模式")){
                    case "SIMPLE":{
                        break;
                    }
                    case "UPDATE":{
                        for (BattleParticipant participant : battle.participants) {
                            participant.updateOtherPokemon();
                            for (PixelmonWrapper wrapper : participant.allPokemon) {
                                wrapper.update();
                                wrapper.updateHPIncrease();
                            }
                        }
                        BattleAction[] base = battle.battleLog.getAllActions().toArray(new BattleAction[0]);
                        battle.battleLog.getAllActions().clear();
                        for (BattleAction action : base) {
                            battle.battleLog.logEvent(action);
                        }
                        battle.updatePokemonHealth();
                        battle.update();
                        break;
                    }
                    case "INTERFACE":{
                        ArrayList<PixelmonWrapper> allPoke = battle.getTeamPokemon(battle.getParticipantForEntity(player));
                        List<UUID> collect = allPoke.stream().map(s->{
                            if (s.getHealth() >= 1) {
                                return s.getPokemonUUID();
                            }
                            return null;
                        }).filter(Objects::nonNull).collect(Collectors.toList());
                        List<Boolean> booleans = collect.stream().map(s-> true).collect(Collectors.toList());
                        NetworkHelper.sendPacket(player,new BackToMainMenuPacket(booleans,true,collect));
                        break;
                    }
                }
                battle.pauseBattle();
                battle.endPause();
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