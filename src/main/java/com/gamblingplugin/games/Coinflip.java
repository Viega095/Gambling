package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.CoinflipStructure;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class Coinflip {

    private final GamblingPlugin plugin;
    private final Random random = new Random();

    public enum Side {
        HEADS, TAILS
    }

    public Coinflip(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void play(Player player, double bet, String selection) {
        Side chosenSide;
        try {
            chosenSide = Side.valueOf(selection.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c[VieGambling] Lado inválido. Usa HEADS o TAILS");
            return;
        }

        // Check cooldown
        if (plugin.getCooldownManager().hasCooldown(player, "coinflip")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player, "coinflip");
            player.sendMessage("§c[VieGambling] Espera " + remaining + " segundos");
            return;
        }

        // Check balance
        if (!plugin.getEconomyManager().has(player, bet)) {
            player.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds"));
            return;
        }

        plugin.getEconomyManager().withdraw(player, bet);

        // Contribute to jackpot
        plugin.getJackpotManager().contributeBet(bet);

        // Set cooldown (10 seconds)
        plugin.getCooldownManager().setCooldown(player, "coinflip");

        player.sendMessage("");
        player.sendMessage("§6§l[VieGambling] Coinflip");
        player.sendMessage("§7Tu elección: §e" + (chosenSide == Side.HEADS ? "Cara" : "Cruz"));
        player.sendMessage("§7Apuesta: §a$" + String.format("%.2f", bet));
        player.sendMessage("");

        // Determine result immediately
        Side result = random.nextBoolean() ? Side.HEADS : Side.TAILS;

        // Find and animate nearest coinflip structure
        CoinflipStructure structure = plugin.getStructureManager().getCoinflipStructure(player.getLocation());
        if (structure != null) {
            structure.flip3D(player, result, null);
        }

        // Show spinning GUI
        com.gamblingplugin.gui.CoinflipSpinGUI spinGUI = new com.gamblingplugin.gui.CoinflipSpinGUI(plugin);
        spinGUI.open(player, chosenSide, result, bet);

        // Schedule result processing after GUI animation
        new BukkitRunnable() {
            @Override
            public void run() {
                finalizeGame(player, bet, chosenSide, result);
            }
        }.runTaskLater(plugin, 100L); // 5 seconds (3s spin + 2s result display)
    }

    private void finalizeGame(Player player, double bet, Side chosenSide, Side result) {
        player.sendMessage("");
        player.sendMessage("§6§l[VieGambling] §7Resultado:");
        player.sendMessage("§e  Moneda cayó: " + (result == Side.HEADS ? "§6Cara" : "§7Cruz"));
        player.sendMessage("");

        if (chosenSide == result) {
            // WIN
            double payout = bet * 2;
            plugin.getEconomyManager().deposit(player, payout);
            player.sendMessage("§a§l[VieGambling] ¡GANASTE!");
            player.sendMessage("§7Premio: §a$" + String.format("%.2f", payout));
            player.sendMessage("");

            // Visual effects
            com.gamblingplugin.effects.ParticleEffects.playWinEffect(player, payout);
            com.gamblingplugin.effects.ParticleEffects.playCoinflipEffect(player.getLocation());
            com.gamblingplugin.effects.DisplayManager.showWinTitle(player, payout);
            com.gamblingplugin.effects.DisplayManager.showCoinflipResult(player,
                    result == Side.HEADS ? "HEADS" : "TAILS");

            // Win celebration
            Location loc = player.getLocation();
            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.add(0, 2, 0), 30, 0.5, 0.5, 0.5, 0);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.3f);

        } else {
            // LOSE
            player.sendMessage("§c§l[VieGambling] Perdiste");
            player.sendMessage("§7Mejor suerte la próxima vez");
            player.sendMessage("");

            // Visual effects
            com.gamblingplugin.effects.ParticleEffects.playLossEffect(player);
            com.gamblingplugin.effects.DisplayManager.showLossTitle(player);
            com.gamblingplugin.effects.DisplayManager.showCoinflipResult(player,
                    result == Side.HEADS ? "HEADS" : "TAILS");

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.4f, 1f);
        }

        // Try to win jackpot (regardless of game result)
        if (plugin.getJackpotManager().tryWinJackpot(player, bet)) {
            player.sendMessage("");
            player.sendMessage("§6§l¡BONUS! §7¡También ganaste el §6§lJACKPOT§7!");
            player.sendMessage("");
        }
    }
}
