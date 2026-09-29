package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.engine.DisplayEntityEngine;
import org.bukkit.*;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Vector3f;

import java.util.concurrent.ThreadLocalRandom;

public class PlinkoPhysicalMachine {

    private final GamblingPlugin plugin;
    private final Location machineBase;

    public PlinkoPhysicalMachine(GamblingPlugin plugin, Location machineBase) {
        this.plugin = plugin;
        this.machineBase = machineBase;
    }

    public void dropBall(Player player, double betAmount, int riskLevel) {
        if (!plugin.getEconomyManager().has(player, betAmount)) {
            player.sendMessage(ChatColor.RED + "✖ Fondos insuficientes para jugar Plinko 3D (" +
                    plugin.getEconomyManager().format(betAmount) + ")");
            return;
        }

        plugin.getEconomyManager().withdraw(player, betAmount);
        player.sendMessage(ChatColor.GOLD + "🎰 [Plinko 3D] ¡Esfera lanzada por " + plugin.getEconomyManager().format(betAmount) + "!");

        Location dropLocation = machineBase.clone().add(0, 8, 0);
        ItemDisplay ball = DisplayEntityEngine.spawnItemDisplay(dropLocation, new ItemStack(Material.SLIME_BALL), new Vector3f(0.5f, 0.5f, 0.5f));

        new BukkitRunnable() {
            int step = 0;
            double xOffset = 0;
            double currentY = 8.0;

            @Override
            public void run() {
                if (ball.isDead() || !ball.isValid()) {
                    cancel();
                    return;
                }

                step++;
                currentY -= 0.6;
                double bounce = (ThreadLocalRandom.current().nextBoolean() ? 0.35 : -0.35);
                xOffset += bounce;

                Location newLoc = machineBase.clone().add(xOffset, currentY, 0);
                ball.teleport(newLoc);

                // Sound & particle effects on each peg hit
                newLoc.getWorld().playSound(newLoc, Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.2f + (step * 0.08f));
                newLoc.getWorld().spawnParticle(Particle.CRIT, newLoc, 5, 0.1, 0.1, 0.1, 0.05);

                if (currentY <= 0) {
                    // Hit the bottom slot
                    cancel();
                    ball.remove();
                    evaluateMultiplier(player, betAmount, xOffset);
                }
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }

    private void evaluateMultiplier(Player player, double bet, double finalOffset) {
        double dist = Math.abs(finalOffset);
        double multiplier;

        if (dist >= 2.0) {
            multiplier = 10.0; // Edge jackpot!
        } else if (dist >= 1.2) {
            multiplier = 3.5;
        } else if (dist >= 0.6) {
            multiplier = 1.2;
        } else {
            multiplier = 0.4; // Center
        }

        double payout = bet * multiplier;
        Location winLoc = machineBase.clone().add(finalOffset, 0, 0);

        if (multiplier >= 1.0) {
            plugin.getEconomyManager().deposit(player, payout);
            player.sendMessage(ChatColor.GREEN + "🎉 [Plinko 3D] ¡Multiplicador x" + multiplier + "! Has ganado " +
                    plugin.getEconomyManager().format(payout) + "!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.6f);
            winLoc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, winLoc, 30, 0.5, 0.5, 0.5, 0.1);
        } else {
            player.sendMessage(ChatColor.RED + "✖ [Plinko 3D] Multiplicador x" + multiplier + ". Has recuperado " +
                    plugin.getEconomyManager().format(payout) + ".");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.8f);
        }
    }
}
