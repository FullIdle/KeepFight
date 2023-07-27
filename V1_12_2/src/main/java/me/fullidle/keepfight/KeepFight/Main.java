package me.fullidle.keepfight.KeepFight;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.battles.controller.log.BattleActionBase;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.BackToMainMenu;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleEndCause;
import com.pixelmonmod.pixelmon.util.network.BetterNetworkWrapper;
import net.minecraft.entity.player.EntityPlayerMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
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
            EntityPlayerMP player = Pixelmon.storageManager.getParty(p.getUniqueId()).getPlayer();
            BattleControllerBase battle = BattleRegistry.getBattle(player);
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
                        BattleActionBase[] base = battle.battleLog.getAllActions().toArray(new BattleActionBase[0]);
                        battle.battleLog.getAllActions().clear();
                        for (BattleActionBase action : base) {
                            battle.battleLog.addEvent(action);
                        }
                        battle.updatePokemonHealth();
                        battle.update();
                        break;
                    }
                    case "INTERFACE":{
                        ArrayList<PixelmonWrapper> allPoke = battle.getTeamPokemon(battle.getParticipantForEntity(player));
                        List<PixelmonWrapper> collect = allPoke.stream().filter(s -> s.getHealth() >= 1).collect(Collectors.toList());
                        Pixelmon.network.sendTo(new BackToMainMenu(true,true, (ArrayList<PixelmonWrapper>) collect),player);
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