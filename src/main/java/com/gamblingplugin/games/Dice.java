package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.games.DiceAnimation;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Random;

public class Dice extends Game {

    private final Random random = new Random();

    public Dice(GamblingPlugin plugin) {
        super(plugin, "dice");
    }

    @Override
    public void play(Player player, double bet, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUso: /gamble dice <cantidad> <1-6>");
            return;
        }

        int choice;
        try {
            choice = Integer.parseInt(args[0]);
            if (choice < 1 || choice > 6) {
                player.sendMessage("§cPor favor elige un número entre 1 y 6.");
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cNúmero inválido.");
            return;
        }

        if (!plugin.getEconomyManager().has(player, bet)) {
            player.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds"));
            return;
        }

        plugin.getEconomyManager().withdraw(player, bet);

        // Contribute to jackpot
        plugin.getJackpotManager().contributeBet(bet);

        // Set cooldown (10 seconds)
        plugin.getCooldownManager().setCooldown(player, "dice");

        player.sendMessage(plugin.getConfigManager().getMessage("bet-placed")
                .replace("%amount%", String.valueOf(bet))
                .replace("%selection%", String.valueOf(choice)));

        startAnimation(player, bet, "SPECIFIC", choice);
    }

    // New method for GUI-based betting
    public void playWithBetType(Player player, double bet, String betType, int specificNumber) {
        // Check cooldown
        if (plugin.getCooldownManager().hasCooldown(player, "dice")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player, "dice");
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

        // Set cooldown
        plugin.getCooldownManager().setCooldown(player, "dice");

        // Show bet info
        player.sendMessage("");
        player.sendMessage("§6§l[VieGambling] Dados");
        String betDesc = "";
        if (betType.equals("SPECIFIC")) {
            betDesc = "Número " + specificNumber + " (6x)";
        } else if (betType.equals("LOW_RANGE")) {
            betDesc = "Rango Bajo 1-3 (1.5x)";
        } else if (betType.equals("HIGH_RANGE")) {
            betDesc = "Rango Alto 4-6 (1.5x)";
        }
        player.sendMessage("§7Tu apuesta: §e" + betDesc);
        player.sendMessage("§7Monto: §a$" + String.format("%.2f", bet));
        player.sendMessage("");

        // Find and animate nearest dice structure
        com.gamblingplugin.structures.DiceStructure diceStruct = plugin.getStructureManager()
                .getDiceStructure(player.getLocation());
        if (diceStruct != null) {
            diceStruct.roll(() -> {
                // Animation done
            });
        }

        startAnimation(player, bet, betType, specificNumber);
    }

    private void startAnimation(Player player, double bet, String betType, int specificNumber) {
        new DiceAnimation(plugin).play(player, specificNumber, (v) -> {
            int result = random.nextInt(6) + 1;
            finalizeGame(player, bet, betType, specificNumber, result);
        });
    }

    private void finalizeGame(Player player, double bet, String betType, int specificNumber, int result) {
        player.sendMessage("");
        player.sendMessage("§6§l[VieGambling] §7Resultado:");
        player.sendMessage("§e  Dado: " + result);
        player.sendMessage("");

        boolean won = false;
        double multiplier = 0;

        // Determine win condition and payout
        if (betType.equals("SPECIFIC")) {
            won = (specificNumber == result);
            multiplier = 6.0;
        } else if (betType.equals("LOW_RANGE")) {
            won = (result >= 1 && result <= 3);
            multiplier = 1.5;
        } else if (betType.equals("HIGH_RANGE")) {
            won = (result >= 4 && result <= 6);
            multiplier = 1.5;
        }

        if (won) {
            double payout = bet * multiplier;
            plugin.getEconomyManager().deposit(player, payout);
            player.sendMessage("§a§l[VieGambling] ¡GANASTE!");
            player.sendMessage("§7Premio: §a$" + String.format("%.2f", payout) + " §7(" + multiplier + "x)");
            player.sendMessage("");

            // Visual effects
            com.gamblingplugin.effects.ParticleEffects.playWinEffect(player, payout);
            com.gamblingplugin.effects.ParticleEffects.playDiceEffect(player.getLocation());
            com.gamblingplugin.effects.DisplayManager.showWinTitle(player, payout);
            com.gamblingplugin.effects.DisplayManager.showDiceResult(player, result);

            // Win celebration
            Location loc = player.getLocation();
            player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc.add(0, 2, 0), 30, 0.5, 0.5, 0.5, 0.1);
            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 20, 0.5, 0.5, 0.5, 0);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.3f);
        } else {
            player.sendMessage("§c§l[VieGambling] Perdiste");
            player.sendMessage("§7Mejor suerte la próxima vez");
            player.sendMessage("");

            // Visual effects
            com.gamblingplugin.effects.ParticleEffects.playLossEffect(player);
            com.gamblingplugin.effects.DisplayManager.showLossTitle(player);
            com.gamblingplugin.effects.DisplayManager.showDiceResult(player, result);

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
