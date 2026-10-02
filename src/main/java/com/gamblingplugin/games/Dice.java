package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.DiceStructure;
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

        playWithBetType(player, bet, "SPECIFIC", choice);
    }

    // Method for GUI-based betting & command betting
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
        plugin.getJackpotManager().contributeBet(bet);
        plugin.getCooldownManager().setCooldown(player, "dice");

        // Show bet info
        player.sendMessage("");
        player.sendMessage("§6§l[VieGambling] Dados 3D");
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

        int result = random.nextInt(6) + 1;

        // Check if near a 3D physical dice table
        DiceStructure diceStruct = plugin.getStructureManager().getDiceStructure(player.getLocation());
        if (diceStruct != null) {
            // Master 3D table roll (Single authoritative animation)
            diceStruct.roll3D(player, result, () -> {
                finalizeGame(player, bet, betType, specificNumber, result);
            });
        } else {
            // Standalone virtual toss fallback
            new DiceAnimation(plugin).play(player, specificNumber, (v) -> {
                finalizeGame(player, bet, betType, specificNumber, result);
            });
        }
    }

    private void finalizeGame(Player player, double bet, String betType, int specificNumber, int result) {
        String unicodeDice = getDiceUnicode(result);

        player.sendMessage("");
        player.sendMessage("§6§l[VieGambling] §7Resultado: §e" + unicodeDice + " " + result);
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

            // Single unified Title & Fanfare
            player.sendTitle("§a§l¡GANASTE! " + unicodeDice, "§e+" + plugin.getEconomyManager().format(payout) + " §7(Dado: " + result + ")", 5, 50, 15);

            Location loc = player.getLocation();
            player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc.add(0, 1.5, 0), 35, 0.5, 0.5, 0.5, 0.1);
            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 25, 0.5, 0.5, 0.5, 0);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.4f);
        } else {
            player.sendMessage("§c§l[VieGambling] Perdiste");
            player.sendMessage("§7Mejor suerte la próxima vez");
            player.sendMessage("");

            // Single unified Loss Title & Sound
            player.sendTitle("§c§lPERDISTE", "§7Dado cayó en §e" + unicodeDice + " " + result + " §7| Suerte la próxima", 5, 40, 10);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.4f, 1f);
        }

        // Try to win jackpot (regardless of game result)
        if (plugin.getJackpotManager().tryWinJackpot(player, bet)) {
            player.sendMessage("");
            player.sendMessage("§6§l¡BONUS! §7¡También ganaste el §6§lJACKPOT§7!");
            player.sendMessage("");
        }
    }

    private String getDiceUnicode(int result) {
        return switch (result) {
            case 1 -> "⚀";
            case 2 -> "⚁";
            case 3 -> "⚂";
            case 4 -> "⚃";
            case 5 -> "⚄";
            case 6 -> "⚅";
            default -> "🎲";
        };
    }
}
