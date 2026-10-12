package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.RouletteWheel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Random;

public class Roulette extends Game {

    private final Random random = new Random();
    // American roulette numbers: 0, 1-36, and 00 (represented as -1)
    private static final int[] NUMBERS = { 0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10, 5, 24,
            16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26, -1 }; // Added -1 for 00

    public Roulette(GamblingPlugin plugin) {
        super(plugin, "roulette");
    }

    @Override
    public void play(Player player, double bet, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§c[VieGambling] §7Usage: /gamble roulette <amount> <red/black/green/number>");
            return;
        }

        String choice = args[0].toLowerCase();

        // Check cooldown
        if (plugin.getCooldownManager().hasCooldown(player, "roulette")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player, "roulette");
            player.sendMessage("§c[VieGambling] Debes esperar " + (int) (remaining / 1000) + " segundos.");
            return;
        }

        // Validate choice
        if (!isValidChoice(choice)) {
            player.sendMessage(
                    "§c[VieGambling] §7¡Opción inválida! Elige rojo, negro, verde, par, impar, bajo, alto, o un número 0-36.");
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
        plugin.getCooldownManager().setCooldown(player, "roulette");

        player.sendMessage(plugin.getConfigManager().getMessage("bet-placed")
                .replace("%amount%", String.valueOf(bet))
                .replace("%selection%", choice));

        startAnimation(player, bet, choice);
    }

    private boolean isValidChoice(String choice) {
        if (choice.equals("red") || choice.equals("black") || choice.equals("green") ||
                choice.equals("rojo") || choice.equals("negro") || choice.equals("verde") ||
                choice.equals("even") || choice.equals("odd") || choice.equals("par") || choice.equals("impar") ||
                choice.equals("low") || choice.equals("high") || choice.equals("bajo") || choice.equals("alto"))
            return true;
        // Dozens: 1st12, 2nd12, 3rd12, docena1, docena2, docena3
        if (choice.equals("1st12") || choice.equals("2nd12") || choice.equals("3rd12") ||
                choice.equals("docena1") || choice.equals("docena2") || choice.equals("docena3") ||
                choice.equals("1-12") || choice.equals("13-24") || choice.equals("25-36"))
            return true;
        // Columns: col1, col2, col3, columna1, columna2, columna3
        if (choice.equals("col1") || choice.equals("col2") || choice.equals("col3") ||
                choice.equals("columna1") || choice.equals("columna2") || choice.equals("columna3"))
            return true;
        // Allow 00 as valid choice
        if (choice.equals("00"))
            return true;
        try {
            int num = Integer.parseInt(choice);
            return num >= 0 && num <= 36;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void startAnimation(Player player, double bet, String choice) {
        RouletteWheel wheel = plugin.getStructureManager().getNearestRouletteWheel(player.getLocation());
        if (wheel == null) {
            player.sendMessage("§c[VieGambling] §7¡No hay ninguna mesa de ruleta 3D cercana!");
            return;
        }

        if (wheel.isSpinning()) {
            player.sendMessage("§c[VieGambling] §7¡La ruleta está girando en este momento!");
            return;
        }

        int result = random.nextInt(NUMBERS.length);
        int resultNumber = NUMBERS[result];
        String resultColor = getResultColor(resultNumber);

        wheel.spinTo(resultNumber, () -> {
            finalizeGame(player, bet, choice, resultNumber, resultColor);
        });
    }

    private String getResultColor(int number) {
        if (number == 0 || number == -1)
            return "green";
        int[] redNumbers = { 1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36 };
        for (int n : redNumbers) {
            if (n == number)
                return "red";
        }
        return "black";
    }

    private void finalizeGame(Player player, double bet, String choice, int resultNumber, String resultColor) {
        boolean won = false;
        double payout = 0;

        // Normalize choice
        if (choice.equals("rojo")) choice = "red";
        if (choice.equals("negro")) choice = "black";
        if (choice.equals("verde")) choice = "green";
        if (choice.equals("par")) choice = "even";
        if (choice.equals("impar")) choice = "odd";
        if (choice.equals("bajo") || choice.equals("1-18")) choice = "low";
        if (choice.equals("alto") || choice.equals("19-36")) choice = "high";
        if (choice.equals("docena1") || choice.equals("1-12")) choice = "1st12";
        if (choice.equals("docena2") || choice.equals("13-24")) choice = "2nd12";
        if (choice.equals("docena3") || choice.equals("25-36")) choice = "3rd12";
        if (choice.equals("columna1")) choice = "col1";
        if (choice.equals("columna2")) choice = "col2";
        if (choice.equals("columna3")) choice = "col3";

        // Determine color code for message
        String colorCode = resultColor.equals("red") ? "§c" : (resultColor.equals("green") ? "§a" : "§8");
        String colorName = resultColor.equals("red") ? "ROJO" : (resultColor.equals("green") ? "VERDE" : "NEGRO");
        String numberDisplay = resultNumber == -1 ? "00" : String.valueOf(resultNumber);

        if (choice.equals("red") || choice.equals("black") || choice.equals("green")) {
            if (choice.equals(resultColor)) {
                won = true;
                payout = choice.equals("green") ? bet * 36 : bet * 2;
            }
        } else if (choice.equals("even")) {
            if (resultNumber != 0 && resultNumber != -1 && resultNumber % 2 == 0) {
                won = true;
                payout = bet * 2;
            }
        } else if (choice.equals("odd")) {
            if (resultNumber != 0 && resultNumber != -1 && resultNumber % 2 != 0) {
                won = true;
                payout = bet * 2;
            }
        } else if (choice.equals("low")) {
            if (resultNumber >= 1 && resultNumber <= 18) {
                won = true;
                payout = bet * 2;
            }
        } else if (choice.equals("high")) {
            if (resultNumber >= 19 && resultNumber <= 36) {
                won = true;
                payout = bet * 2;
            }
        } else if (choice.equals("1st12")) {
            if (resultNumber >= 1 && resultNumber <= 12) {
                won = true;
                payout = bet * 3; // 2:1 net payout
            }
        } else if (choice.equals("2nd12")) {
            if (resultNumber >= 13 && resultNumber <= 24) {
                won = true;
                payout = bet * 3;
            }
        } else if (choice.equals("3rd12")) {
            if (resultNumber >= 25 && resultNumber <= 36) {
                won = true;
                payout = bet * 3;
            }
        } else if (choice.equals("col1")) {
            if (resultNumber >= 1 && resultNumber <= 36 && (resultNumber - 1) % 3 == 0) {
                won = true;
                payout = bet * 3;
            }
        } else if (choice.equals("col2")) {
            if (resultNumber >= 1 && resultNumber <= 36 && (resultNumber - 2) % 3 == 0) {
                won = true;
                payout = bet * 3;
            }
        } else if (choice.equals("col3")) {
            if (resultNumber >= 1 && resultNumber <= 36 && resultNumber % 3 == 0) {
                won = true;
                payout = bet * 3;
            }
        } else if (choice.equals("00")) {
            if (resultNumber == -1) {
                won = true;
                payout = bet * 36;
            }
        } else {
            int chosenNum = Integer.parseInt(choice);
            if (chosenNum == resultNumber) {
                won = true;
                payout = bet * 36;
            }
        }

        // Show result to player
        player.sendMessage("");
        player.sendMessage("§6§l[VieGambling] §7Resultado:");
        player.sendMessage(colorCode + "§l  " + colorName + " " + numberDisplay);
        player.sendMessage("");

        if (won) {
            plugin.getEconomyManager().deposit(player, payout);
            player.sendMessage("§a§l[VieGambling] ¡GANASTE!");
            player.sendMessage("§7Premio: §a$" + String.format("%.2f", payout));
            player.sendMessage("");

            // Global broadcast for green wins (rare)
            if (resultColor.equals("green")) {
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage("§a§l[VieGambling] ¡VERDE!");
                Bukkit.broadcastMessage(
                        "§e" + player.getName() + " §7ganó apostando al §a" + colorName + " " + numberDisplay);
                Bukkit.broadcastMessage("§7Premio: §a$" + String.format("%.2f", payout));
                Bukkit.broadcastMessage("");
            }
        } else {
            player.sendMessage("§c§l[VieGambling] Perdiste");
            player.sendMessage("§7Mejor suerte la próxima vez");
            player.sendMessage("");
        }

        // Try to win jackpot (regardless of game result)
        if (plugin.getJackpotManager().tryWinJackpot(player, bet)) {
            player.sendMessage("");
            player.sendMessage("§6§l¡BONUS! §7¡También ganaste el §6§lJACKPOT§7!");
            player.sendMessage("");
        }
    }
}
