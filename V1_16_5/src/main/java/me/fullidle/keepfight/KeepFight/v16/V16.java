package me.fullidle.keepfight.KeepFight.v16;

import com.pixelmonmod.pixelmon.api.events.battles.BattleMessageEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.BackToMainMenuPacket;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.BattleSwitchPacket;
import lombok.SneakyThrows;
import me.fullidle.ficore.ficore.common.api.event.ForgeEvent;
import me.fullidle.keepfight.KeepFight.common.SomeData;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.stream.Collectors;

public class V16 implements Listener, CommandExecutor, TabCompleter {
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
        if (args.length < 1){
            rs(pp);
            return false;
        }
        if (pp.bc.hasSpectator(pp.player)) {
            pp.bc.sendToPlayer(pp.player,"§c你非参与者");
            return false;
        }
        switch (args[0]){
            case "rs":
            case "reselect":{
                rs(pp);
                break;
            }
            case "eb":
            case "emptyBlood":{
                PixelmonWrapper wrapper = pp.controlledPokemon.get(0);
                if (wrapper.getHealth() != 0){
                    battle.sendToPlayer(fp,"§cYour Pokémon is not dead!");
                    break;
                }
                wrapper.pokemon.setHealth(1);
                waitDiedPoke.put(player.getUniqueId(),wrapper.pokemon);
                pp.sendMessage(new BattleSwitchPacket());
                break;
            }
        }
        return false;
    }

    public static void rs(PlayerParticipant pp){
        BackToMainMenuPacket message = new BackToMainMenuPacket(
                Arrays.stream(pp.allPokemon).map(pw->(pw.isAlive() && pw.entity == null)).collect(Collectors.toList()), true,
                Arrays.stream(pp.allPokemon).map(PixelmonWrapper::getPokemonUUID).collect(Collectors.toList()));
        pp.sendMessage(message);
        pp.bc.sendToPlayer(pp.player,"§a对战续命成功,有没有效果得自己试");
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length < 1) return SomeData.subCmd;
        if (args.length == 1) return SomeData.subCmd.stream().filter(s->s.startsWith(args[0])).collect(Collectors.toList());
        return null;
    }

    @EventHandler
    public void onForge(ForgeEvent event){
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
        }
    }
}
