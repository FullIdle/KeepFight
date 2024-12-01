package me.fullidle.keepfight.KeepFight.v12;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.events.battles.AttackEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleMessageEvent;
import com.pixelmonmod.pixelmon.api.events.battles.UseBattleItemEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.BackToMainMenu;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.BattleSwitch;
import lombok.SneakyThrows;
import me.fullidle.ficore.ficore.common.api.event.ForgeEvent;
import me.fullidle.keepfight.KeepFight.common.CommonUtil;
import me.fullidle.keepfight.KeepFight.common.SomeData;
import net.minecraft.entity.player.EntityPlayerMP;
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

public class V12 implements Listener, CommandExecutor {
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
        //
        BackToMainMenu message = new BackToMainMenu(true,true,new ArrayList<>(Arrays.asList(pp.allPokemon)));
        pp.sendMessage(message);
        pp.bc.sendToPlayer(pp.player,SomeData.main.getConfig().
                getString("msg.KeepFightCmdMsg").replace('&','§'));

        PixelmonWrapper wrapper = pp.controlledPokemon.get(0);
        if (wrapper.getHealth() != 0){
            return false;
        }
        wrapper.pokemon.setHealth(1);
        waitDiedPoke.put(pp.player.func_110124_au(),wrapper.pokemon);
        pp.sendMessage(new BattleSwitch());
        return false;
    }

    @EventHandler
    public void onForge(ForgeEvent event){
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
                if (e.user.getParticipant() instanceof PlayerParticipant)
                    CommonUtil.resetTitleTipsTick(Bukkit.getPlayer(((PlayerParticipant) e.user.getParticipant()).player.func_110124_au()));
            }
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
}
