package dev.anonhunt;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AnonymousHunt extends JavaPlugin implements Listener {

    private final Map<UUID, String> realNames = new HashMap<>();
    private final Set<UUID> eliminated = new HashSet<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("starthunt").setExecutor(new StartHuntCommand());
	// Bukkit.getPluginManager().registerEvents(new TabCompleteBlocker(), this);
        this.getCommand("stophunt").setExecutor(new StopHuntCommand());
	Bukkit.getPluginManager().registerEvents(new CommandBlocker(), this);
	Bukkit.getPluginManager().registerEvents(new CommandHider(this), this);

        getLogger().info("AnonymousHunt enabled.");
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        realNames.put(p.getUniqueId(), p.getName());

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "skin set " + p.getName() + " root");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "skin update -all");
        p.setDisplayName("Herobrine");
        p.setPlayerListName("Herobrine");
        event.setJoinMessage(ChatColor.YELLOW + "" + ChatColor.MAGIC + "Herobrine" + ChatColor.RESET + "" + ChatColor.YELLOW + " joined the server");

        for (Player playergoo : Bukkit.getOnlinePlayers()) {
            if (playergoo.hasPermission("anonhunt.see")) {
                // This player has the permission
                // Do something, e.g. send a message
                playergoo.sendMessage("[TERMINATOR] Their real name was " + p.getName());
            }
        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        event.setQuitMessage(p.hasPermission("anonhunt.see") ? p.getName() + " left the server" : ChatColor.YELLOW + "" + ChatColor.MAGIC + "Herobrine" + ChatColor.RESET + "" + ChatColor.YELLOW + " left the server");
        for (Player playergoo : Bukkit.getOnlinePlayers()) {
            if (playergoo.hasPermission("anonhunt.see")) {
	        playergoo.sendMessage("[TERMINATOR] Their real name was " + p.getName());
	    }
        }
    }
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        event.setFormat("<" + ChatColor.MAGIC + "Herobrine" + ChatColor.RESET + "> " + event.getMessage());
        for (Player playergoo : Bukkit.getOnlinePlayers()) {
            if (playergoo.hasPermission("anonhunt.see")) {
                playergoo.sendMessage("[TERMINATOR] <" + p.getName() + "> " + event.getMessage());
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        String victimReal = realNames.getOrDefault(victim.getUniqueId(), victim.getName());

        if (killer != null) {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            if (weapon.hasItemMeta() && weapon.getItemMeta().hasDisplayName()) {
                String weaponName = ChatColor.stripColor(weapon.getItemMeta().getDisplayName());

                if (weaponName.equalsIgnoreCase(victimReal)) {
                    Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.MAGIC + "Herobrine" + ChatColor.RESET + ChatColor.RED + " was murdered in cold blood!");
                    event.setDeathMessage("They watched too much brainrot and died.");
                    eliminated.add(victim.getUniqueId());
                    Bukkit.getBanList(BanList.Type.NAME).addBan(victim.getName(), "[TERMINATOR 9000] You have been eliminated. Contact @KGanta1215 on Discord for a chance at redemption.", null, null);
                    victim.kickPlayer("[TERMINATOR 9000] Erased player " + victim.getName() + " from reality. You have been eliminated. Contact @KGanta1215 on Discord for a chance at redemption.");
                    // return;
                }
            }
            for (Player playergoo : Bukkit.getOnlinePlayers()) {
                if (playergoo.hasPermission("anonhunt.see")) {
                    playergoo.sendMessage("[TERMINATOR] " + victim.getName() + " was murdered by " + killer.getName());
                }
            }
        }
	for (Player playergoo : Bukkit.getOnlinePlayers()) {
            if (playergoo.hasPermission("anonhunt.see")) {
                playergoo.sendMessage("[TERMINATOR] " + victim.getName() + " died naturally");
            }
        }
        event.setDeathMessage(ChatColor.MAGIC + "Herobrine" + ChatColor.RESET + " died naturally.");
    }
    public static AnonymousHunt getPlugin() {
        return JavaPlugin.getPlugin(AnonymousHunt.class);
    }
    public static String getRealName(Player p) {
        return JavaPlugin.getPlugin(AnonymousHunt.class).realNames.getOrDefault(p.getUniqueId(), p.getName());
    }
}

class CommandBlocker implements Listener {

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        // If player does NOT have admin permission, block command
        if (!player.hasPermission("anonhunt.admin")) {
            event.setCancelled(true);
            player.sendMessage("Commands are not enabled on this server");
        }
    }
}
