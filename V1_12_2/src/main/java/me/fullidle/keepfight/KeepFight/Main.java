package me.fullidle.keepfight.KeepFight;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.battles.BattleMessageEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.BackToMainMenu;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.BattleSwitch;
import lombok.SneakyThrows;
import me.fullidle.ficore.ficore.common.api.event.ForgeEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class Main extends JavaPlugin implements Listener {
    @SneakyThrows
    @Override
    public void onEnable() {
        PluginCommand command = getCommand("keepfight");
        command.setExecutor(this);
        command.setTabCompleter(this);
        getLogger().info("插件已经载入!");
        getServer().getPluginManager().registerEvents(this,this);
    }


    public static List<String> subCmd = Arrays.asList(
            "help",
            "rs","eb",
            "reselect","emptyBlood"
            );
    public static String[] help = new String[]{
            "rs,reselect 卡稍等时用!(抓精灵除外)","eb,emptyBlood 空血卡时用"
    };
    public static Map<UUID,Pokemon> waitDiedPoke = new HashMap<>();

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou cannot use this command if you are not a player!");
            return false;
        }
        if (args.length < 1){
            sender.sendMessage(help);
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
        switch (args[0]){
            case "rs":
            case "reselect":{
                BackToMainMenu message = new BackToMainMenu(true,true,new ArrayList<>(Arrays.asList(pp.allPokemon)));
                pp.sendMessage(message);
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
                pp.sendMessage(new BattleSwitch());
                break;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length < 1) return subCmd;
        if (args.length == 1) return subCmd.stream().filter(s->s.startsWith(args[0])).collect(Collectors.toList());
        return null;
    }

    @EventHandler
    public void onForge(ForgeEvent event){
        if (event.getForgeEvent() instanceof BattleMessageEvent) {
            BattleMessageEvent e = (BattleMessageEvent) event.getForgeEvent();
            String text = e.textComponent.getUnformattedComponentText();
            if (!text.contains("sent out")) {
                return;
            }
            UUID uniqueID = e.target.getUniqueID();
            if (Bukkit.getPlayer(uniqueID) == null) {
                return;
            }
            Bukkit.getScheduler().runTaskLater(this,()->{
                Pokemon pokemon = waitDiedPoke.get(uniqueID);
                if (pokemon != null) {
                    pokemon.setHealth(0);
                }
            },1);
        }
    }
}
