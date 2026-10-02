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
        plugin.getJackpotManager().contributeBet(bet);
        plugin.getCooldownManager().setCooldown(player, "coinflip");

        player.sendMessage("");
        player.sendMessage("§6§l[VieGambling] Coinflip 3D");
        player.sendMessage("§7Tu elección: §e" + (chosenSide == Side.HEADS ? "Cara (HEADS)" : "Cruz (TAILS)"));
        player.sendMessage("§7Apuesta: §a$" + String.format("%.2f", bet));
        player.sendMessage("");

        // Determine result
        Side result = random.nextBoolean() ? Side.HEADS : Side.TAILS;

        // Check if near a 3D physical coinflip structure
        CoinflipStructure structure = plugin.getStructureManager().getCoinflipStructure(player.getLocation());
        if (structure != null) {
            // Master 3D table flip (Single authoritative animation)
            structure.flip3D(player, result, () -> {
                finalizeGame(player, bet, chosenSide, result);
            });
        } else {
            // Standalone virtual chest GUI fallback
            com.gamblingplugin.gui.CoinflipSpinGUI spinGUI = new com.gamblingplugin.gui.CoinflipSpinGUI(plugin);
            spinGUI.open(player, chosenSide, result, bet);

            new BukkitRunnable() {
                @Override
                public void run() {
                    finalizeGame(player, bet, chosenSide, result);
                }
            }.runTaskLater(plugin, 70L);
        }
    }

    private void finalizeGame(Player player, double bet, Side chosenSide, Side result) {
        String sideName = (result == Side.HEADS ? "§6Cara (HEADS)" : "§7Cruz (TAILS)");

        player.sendMessage("");
        player.sendMessage("§6§l[VieGambling] §7Resultado: " + sideName);
        player.sendMessage("");

        if (chosenSide == result) {
            // WIN
            double payout = bet * 2;
            plugin.getEconomyManager().deposit(player, payout);
            player.sendMessage("§a§l[VieGambling] ¡GANASTE!");
            player.sendMessage("§7Premio: §a$" + String.format("%.2f", payout) + " §7(2.0x)");
            player.sendMessage("");

            // Single unified Title & Fanfare
            player.sendTitle("§a§l¡GANASTE!", sideName + " §a+" + plugin.getEconomyManager().format(payout), 5, 50, 15);

            Location loc = player.getLocation();
            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.add(0, 1.5, 0), 30, 0.5, 0.5, 0.5, 0);
            player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 25, 0.5, 0.5, 0.5, 0.1);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.4f);
        } else {
            // LOSE
            player.sendMessage("§c§l[VieGambling] Perdiste");
            player.sendMessage("§7Mejor suerte la próxima vez");
            player.sendMessage("");

            // Single unified Loss Title & Sound
            player.sendTitle("§c§lPERDISTE", "§7Cayó en " + sideName + " §7| Suerte la próxima", 5, 40, 10);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.4f, 1f);
        }

        // Try to win jackpot
        if (plugin.getJackpotManager().tryWinJackpot(player, bet)) {
            player.sendMessage("");
            player.sendMessage("§6§l¡BONUS! §7¡También ganaste el §6§lJACKPOT§7!");
            player.sendMessage("");
        }
    }
}
