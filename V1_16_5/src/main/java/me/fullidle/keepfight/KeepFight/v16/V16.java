package me.fullidle.keepfight.KeepFight.v16;

import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.api.events.battles.*;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.BackToMainMenuPacket;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.BattleSwitchPacket;
import lombok.SneakyThrows;
import me.fullidle.ficore.ficore.common.api.event.ForgeEvent;
import me.fullidle.keepfight.KeepFight.common.CommonUtil;
import me.fullidle.keepfight.KeepFight.common.SomeData;
import me.fullidle.keepfight.KeepFight.common.plugin.KFPlugin;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class V16 extends KFPlugin {
    public static Map<UUID,Pokemon> waitDiedPoke = new HashMap<>();

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou cannot use this command if you are not a player!");
            return false;
        }
        Player player = (Player) sender;
        ServerPlayerEntity fp = StorageProxy.getParty(player.getUniqueId()).getPlayer();
        BattleController battle = BattleRegistry.getBattle(fp);
        if (battle == null){
            sender.sendMessage("§cNot battle!");
            return false;
        }
        PlayerParticipant pp = battle.getPlayer(fp);
        if (pp.bc.hasSpectator(pp.player)) {
            pp.bc.sendToPlayer(pp.player,"§cYou are not a participant");
            return false;
        }
        //
        ArrayList<UUID> uuids = new ArrayList<>();
        ArrayList<Boolean> booleans = new ArrayList<>();
        for (PixelmonWrapper wrapper : pp.allPokemon) {
            uuids.add(wrapper.getPokemonUUID());
            booleans.add(wrapper.isAlive());
        }
        BackToMainMenuPacket message = new BackToMainMenuPacket(
                booleans,
                true,
                uuids);
        pp.sendMessage(message);
        pp.bc.sendToPlayer(pp.player,SomeData.main.getConfig().
                getString("msg.KeepFightCmdMsg").replace('&','§'));


        PixelmonWrapper wrapper = pp.controlledPokemon.get(0);
        if (wrapper.getHealth() != 0){
            return false;
        }
        wrapper.pokemon.setHealth(1);
        waitDiedPoke.put(pp.player.func_110124_au(),wrapper.pokemon);
        pp.sendMessage(new BattleSwitchPacket());
        return false;
    }

    @Override
    public boolean playerInBattle(Player player) {
        ServerPlayerEntity spe = PixelmonCommandUtils.getEntityPlayer(player.getUniqueId());
        BattleController battle = BattleRegistry.getBattle(spe);
        if (battle == null) return false;
        return !battle.hasSpectator(spe);
    }

    @Override
    public void forgeEvent(ForgeEvent event) {
        if (event.getForgeEvent() instanceof BattleMessageEvent) {
            BattleMessageEvent e = (BattleMessageEvent) event.getForgeEvent();
            String text = e.textComponent.getString();
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
                for (PlayerParticipant player : ((BattleStartedEvent) event.getForgeEvent()).getBattleController().getPlayers())
                    CommonUtil.resetTitleTipsTick(Bukkit.getPlayer(player.player.func_110124_au()));
            return;
        }

        if (event.getForgeEvent() instanceof BattleEndEvent) {
            for (PlayerParticipant player : ((BattleEndEvent) event.getForgeEvent()).getBattleController().getPlayers())
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
            for (PlayerParticipant player : ((TurnEndEvent) event.getForgeEvent()).getBattleController().getPlayers())
                CommonUtil.resetTitleTipsTick(Bukkit.getPlayer(player.player.func_110124_au()));
            return;
        }

        if (event.getForgeEvent() instanceof UseBattleItemEvent) {
            if (SomeData.main.getConfig().getBoolean("battleTitleTips.enable")) {
                UseBattleItemEvent e = (UseBattleItemEvent) event.getForgeEvent();
                if (e.getParticipant() instanceof PlayerParticipant)
                    CommonUtil.resetTitleTipsTick(Bukkit.getPlayer(((PlayerParticipant) e.getParticipant()).player.func_110124_au()));
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
