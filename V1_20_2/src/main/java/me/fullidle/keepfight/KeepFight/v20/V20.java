package me.fullidle.keepfight.KeepFight.v20;

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
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.stream.Collectors;

public class V20 implements Listener, CommandExecutor {
    public static Map<UUID,Pokemon> waitDiedPoke = new HashMap<>();

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou cannot use this command if you are not a player!");
            return false;
        }
        Player player = (Player) sender;
        ServerPlayer fp = StorageProxy.getPartyNow(player.getUniqueId()).getPlayer();
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
        waitDiedPoke.put(pp.player.getBukkitEntity().getUniqueId(),wrapper.pokemon);
        pp.sendMessage(new BattleSwitchPacket());
        return false;
    }

    @EventHandler
    public void onForge(ForgeEvent event){
        if (event.getForgeEvent() instanceof BattleMessageEvent) {
            BattleMessageEvent e = (BattleMessageEvent) event.getForgeEvent();
            String text = e.component.getString();
            if (!text.contains("sent out")) {
                return;
            }
            UUID uniqueID = e.target.m_20148_();
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
                    CommonUtil.resetTitleTipsTick(Bukkit.getPlayer(player.player.m_20148_()));
            return;
        }

        if (event.getForgeEvent() instanceof BattleEndEvent) {
            for (PlayerParticipant player : ((BattleEndEvent) event.getForgeEvent()).getBattleController().getPlayers())
                CommonUtil.removeTitleTips(Bukkit.getPlayer(player.player.m_20148_()));
            return;
        }

        if (event.getForgeEvent() instanceof AttackEvent.Use) {
            if (SomeData.main.getConfig().getBoolean("battleTitleTips.enable")) {
                AttackEvent.Use e = (AttackEvent.Use) event.getForgeEvent();
                BattleParticipant participant = e.user.getParticipant();
                if (participant instanceof PlayerParticipant)
                    CommonUtil.resetTitleTipsTick(Bukkit.getPlayer(((PlayerParticipant) participant).player.m_20148_()));
            }
            return;
        }

        if (event.getForgeEvent() instanceof TurnEndEvent) {
            for (PlayerParticipant player : ((TurnEndEvent) event.getForgeEvent()).getBattleController().getPlayers())
                CommonUtil.resetTitleTipsTick(Bukkit.getPlayer(player.player.m_20148_()));
            return;
        }

        if (event.getForgeEvent() instanceof UseBattleItemEvent) {
            if (SomeData.main.getConfig().getBoolean("battleTitleTips.enable")) {
                UseBattleItemEvent e = (UseBattleItemEvent) event.getForgeEvent();
                if (e.getParticipant() instanceof PlayerParticipant)
                    CommonUtil.resetTitleTipsTick(Bukkit.getPlayer(((PlayerParticipant) e.getParticipant()).player.m_20148_()));
            }
            return;
        }
    }
}
