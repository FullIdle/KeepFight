package me.fullidle.keepfight.KeepFight.v12;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.command.PixelmonCommand;
import com.pixelmonmod.pixelmon.api.events.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.events.battles.*;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.BackToMainMenu;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.BattleSwitch;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleEndCause;
import lombok.SneakyThrows;
import me.fullidle.ficore.ficore.common.api.event.ForgeEvent;
import me.fullidle.keepfight.KeepFight.common.CommonUtil;
import me.fullidle.keepfight.KeepFight.common.SomeData;
import me.fullidle.keepfight.KeepFight.common.plugin.KFPlugin;
import net.minecraft.entity.player.EntityPlayerMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class V12 extends KFPlugin {
    public static Map<UUID,Pokemon> waitDiedPoke = new HashMap<>();

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou cannot use this command if you are not a player!");
            return false;
        }
        Player player = (Player) sender;
        EntityPlayerMP fp = Pixelmon.storageManager.getParty(player.getUniqueId()).getPlayer();
        BattleControllerBase battle = BattleRegistry.getBattle(fp);
        if (battle == null){
            sender.sendMessage("§cNot battle!");
            return false;
        }
        PlayerParticipant pp = battle.getPlayer(fp);
        if (pp.bc.hasSpectator(pp.player)) {
            pp.bc.sendToPlayer(pp.player,"§cYou are not a participant");
            return false;
        }
        ArrayList<BattleParticipant> our;
        for (BattleParticipant bp : pp.bc.participants) {
            for (PixelmonWrapper wrapper : bp.allPokemon) {
                if (wrapper.getHealth() <= 0) {
                    //我方不只我一人
                    if ((our = pp.getOpponents().get(0).getOpponents()).size() > 1) {
                        our.removeIf(p->p instanceof TrainerParticipant);
                        //我方只有我或者除了我都是npc的时候
                        if (our.size() <= 1) {
                            pp.bc.endBattle();
                        }
                    }
                    break;
                }
            }
        }

        //
        BackToMainMenu message = new BackToMainMenu(true,true,new ArrayList<>(Arrays.asList(pp.allPokemon)));
        pp.sendMessage(message);
        pp.bc.sendToPlayer(pp.player,SomeData.main.getConfig().
                getString("msg.KeepFightCmdMsg").replace('&','§'));

        PixelmonWrapper wrapper = pp.controlledPokemon.get(0);
        if (wrapper.getHealth() != 0){
            return false;
        }
        wrapper.setHealth(1);
        Bukkit.getScheduler().runTask(SomeData.main,()-> wrapper.setHealth(0));
        waitDiedPoke.put(pp.player.func_110124_au(),wrapper.pokemon);
        pp.sendMessage(new BattleSwitch());
        return false;
    }

    @Override
    public boolean playerInBattle(Player player) {
        EntityPlayerMP mp = PixelmonCommand.getEntityPlayer(player.getUniqueId());
        BattleControllerBase battle = BattleRegistry.getBattle(mp);
        if (battle == null) return false;
        return !battle.hasSpectator(mp);
    }

    @Override
    public void forgeEvent(ForgeEvent event) {
        if (event.getForgeEvent() instanceof BattleMessageEvent) {
            BattleMessageEvent e = (BattleMessageEvent) event.getForgeEvent();
            String text = e.textComponent.func_150261_e();
            if (!text.contains("sent out")) {
                return;
            }
            UUID uniqueID = e.target.func_110124_au();
            if (Bukkit.getPlayer(uniqueID) == null) {
                return;
            }
            Bukkit.getScheduler().runTaskLater(SomeData.main,()->{
                Pokemon pokemon = waitDiedPoke.get(uniqueID);
                if (pokemon != null) {
                    pokemon.setHealth(0);
                }
            },1);
            return;
        }

        if (event.getForgeEvent() instanceof BattleStartedEvent) {
            if (SomeData.main.getConfig().getBoolean("battleTitleTips.enable"))
                for (PlayerParticipant player : ((BattleStartedEvent) event.getForgeEvent()).bc.getPlayers())
                    CommonUtil.resetTitleTipsTick(Bukkit.getPlayer(player.player.func_110124_au()));
            return;
        }

        if (event.getForgeEvent() instanceof BattleEndEvent) {
            for (PlayerParticipant player : ((BattleEndEvent) event.getForgeEvent()).bc.getPlayers())
                CommonUtil.removeTitleTips(Bukkit.getPlayer(player.player.func_110124_au()));
            return;
        }

        if (event.getForgeEvent() instanceof AttackEvent.Use) {
            if (SomeData.main.getConfig().getBoolean("battleTitleTips.enable")) {
                AttackEvent.Use e = (AttackEvent.Use) event.getForgeEvent();
                BattleParticipant participant = e.user.getParticipant();
                if (participant instanceof PlayerParticipant)
                    CommonUtil.resetTitleTipsTick(Bukkit.getPlayer(((PlayerParticipant) participant).player.func_110124_au()));
            }
            return;
        }

        if (event.getForgeEvent() instanceof TurnEndEvent) {
            for (PlayerParticipant player : ((TurnEndEvent) event.getForgeEvent()).bcb.getPlayers())
                CommonUtil.resetTitleTipsTick(Bukkit.getPlayer(player.player.func_110124_au()));
            return;
        }

        if (event.getForgeEvent() instanceof UseBattleItemEvent) {
            if (SomeData.main.getConfig().getBoolean("battleTitleTips.enable")) {
                UseBattleItemEvent e = (UseBattleItemEvent) event.getForgeEvent();
                if (e.participant instanceof PlayerParticipant)
                    CommonUtil.resetTitleTipsTick(Bukkit.getPlayer(((PlayerParticipant) e.participant).player.func_110124_au()));
            }
            return;
        }
    }

    @Override
    public void reload() {

    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
