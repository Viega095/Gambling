package com.gamblingplugin.events;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CasinoHeistEvent {

    private final GamblingPlugin plugin;
    private final Set<UUID> activeHeisters = new HashSet<>();
    private boolean heistActive = false;

    public CasinoHeistEvent(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean startHeist(Player leader) {
        if (heistActive) {
            leader.sendMessage(ChatColor.RED + "✖ Ya hay un Golpe al Casino en progreso.");
            return false;
        }

        heistActive = true;
        activeHeisters.add(leader.getUniqueId());

        Bukkit.broadcastMessage(ChatColor.RED + "🚨 [ALERTA CASINO] ¡Las alarmas de la Bóveda Central se han activado!");
        leader.sendMessage(ChatColor.DARK_RED + "☠ [Golpe al Casino] Tienes 60 segundos para hackear la caja fuerte y escapar.");

        Location vaultLoc = leader.getLocation();
        spawnSecurityGuards(vaultLoc);

        new BukkitRunnable() {
            int secondsRemaining = 60;

            @Override
            public void run() {
                if (!leader.isOnline() || leader.isDead()) {
                    leader.sendMessage(ChatColor.RED + "☠ [Golpe Fallido] No pudiste escapar con vida.");
                    endHeist(false, leader);
                    cancel();
                    return;
                }

                secondsRemaining -= 5;
                if (secondsRemaining % 15 == 0) {
                    leader.sendMessage(ChatColor.YELLOW + "⏳ Tiempo para escapar con el botín: " + secondsRemaining + "s");
                    leader.playSound(leader.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
                }

                if (secondsRemaining <= 0) {
                    endHeist(true, leader);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 100L, 100L);

        return true;
    }

    private void spawnSecurityGuards(Location loc) {
        for (int i = 0; i < 3; i++) {
            Zombie guard = (Zombie) loc.getWorld().spawnEntity(loc.clone().add(i * 2 - 2, 0, 3), EntityType.ZOMBIE);
            guard.setCustomName(ChatColor.RED + "Guardaespaldas del Casino VIP");
            guard.setCustomNameVisible(true);
            guard.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
            guard.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            guard.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
            guard.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1200, 1));
        }
    }

    private void endHeist(boolean success, Player leader) {
        heistActive = false;
        activeHeisters.clear();

        if (success) {
            double loot = 250000.0;
            plugin.getEconomyManager().deposit(leader, loot);
            Bukkit.broadcastMessage(ChatColor.GOLD + "💰 ¡" + ChatColor.YELLOW + leader.getName() +
                    ChatColor.GOLD + " ha asaltado la Bóveda del Casino y escapó con " +
                    plugin.getEconomyManager().format(loot) + "!");
            leader.playSound(leader.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }
    }
}
