package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.engine.DisplayEntityEngine;
import org.bukkit.*;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PhysicalSlotsCabinet {

    private final GamblingPlugin plugin;
    private final Location cabinetLocation;
    private final Material[] slotSymbols = {
            Material.DIAMOND,
            Material.EMERALD,
            Material.GOLD_INGOT,
            Material.NETHER_STAR,
            Material.REDSTONE,
            Material.LAPIS_LAZULI
    };

    public PhysicalSlotsCabinet(GamblingPlugin plugin, Location cabinetLocation) {
        this.plugin = plugin;
        this.cabinetLocation = cabinetLocation;
    }

    public void spin(Player player, double bet) {
        if (!plugin.getEconomyManager().has(player, bet)) {
            player.sendMessage(ChatColor.RED + "✖ Fondos insuficientes para la Máquina Tragamonedas 3D.");
            return;
        }

        plugin.getEconomyManager().withdraw(player, bet);
        player.sendMessage(ChatColor.GOLD + "🎰 [Tragamonedas 3D] ¡Palanca accionada por " +
                plugin.getEconomyManager().format(bet) + "!");
        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1f, 1f);

        // Spawn 3 spinning display reels
        List<ItemDisplay> reels = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Location reelLoc = cabinetLocation.clone().add((i - 1) * 0.45, 1.2, 0);
            ItemDisplay reel = DisplayEntityEngine.spawnItemDisplay(reelLoc, new ItemStack(Material.GOLD_INGOT), new Vector3f(0.35f, 0.35f, 0.35f));
            reels.add(reel);
        }

        new BukkitRunnable() {
            int ticks = 0;
            Material s1, s2, s3;

            @Override
            public void run() {
                ticks++;

                for (int i = 0; i < 3; i++) {
                    Material randomSymbol = slotSymbols[ThreadLocalRandom.current().nextInt(slotSymbols.length)];
                    reels.get(i).setItemStack(new ItemStack(randomSymbol));
                }

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.6f, 1.4f);

                if (ticks >= 20) {
                    cancel();
                    // Final outcome
                    s1 = slotSymbols[ThreadLocalRandom.current().nextInt(slotSymbols.length)];
                    s2 = slotSymbols[ThreadLocalRandom.current().nextInt(slotSymbols.length)];
                    s3 = slotSymbols[ThreadLocalRandom.current().nextInt(slotSymbols.length)];

                    reels.get(0).setItemStack(new ItemStack(s1));
                    reels.get(1).setItemStack(new ItemStack(s2));
                    reels.get(2).setItemStack(new ItemStack(s3));

                    // Schedule cleanup of displays after 4 seconds
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        for (ItemDisplay d : reels) {
                            if (d.isValid()) d.remove();
                        }
                    }, 80L);

                    evaluateResult(player, bet, s1, s2, s3);
                }
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }

    private void evaluateResult(Player player, double bet, Material s1, Material s2, Material s3) {
        if (s1 == s2 && s2 == s3) {
            double multiplier = (s1 == Material.NETHER_STAR) ? 50.0 : ((s1 == Material.DIAMOND) ? 20.0 : 10.0);
            double prize = bet * multiplier;
            plugin.getEconomyManager().deposit(player, prize);

            Bukkit.broadcastMessage(ChatColor.GOLD + "🎰💥 ¡JACKPOT EN TRAGAMONEDAS 3D! ¡" +
                    ChatColor.YELLOW + player.getName() + ChatColor.GOLD + " ha sacado 3x " + s1.name() +
                    " y ganó " + plugin.getEconomyManager().format(prize) + "!");

            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 60, 0.5, 0.5, 0.5, 0.2);
        } else if (s1 == s2 || s2 == s3 || s1 == s3) {
            double prize = bet * 1.5;
            plugin.getEconomyManager().deposit(player, prize);
            player.sendMessage(ChatColor.GREEN + "✨ ¡Doble coincidencia! Has ganado " + plugin.getEconomyManager().format(prize));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
        } else {
            player.sendMessage(ChatColor.RED + "✖ No hubo suerte esta vez. ¡Inténtalo de nuevo!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.8f);
        }
    }
}
