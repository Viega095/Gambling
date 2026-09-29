package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.entity.Player;

import java.util.Random;

public class CaseOpening extends Game {

    private final Random random = new Random();

    public enum CaseType {
        COMMON(50.0),
        RARE(200.0),
        LEGENDARY(1000.0);

        private final double price;

        CaseType(double price) {
            this.price = price;
        }

        public double getPrice() {
            return price;
        }
    }

    public enum Rarity {
        COMMON(0.5, 1.0, 60.0, "§7"),
        UNCOMMON(1.0, 2.0, 25.0, "§a"),
        RARE(2.0, 4.0, 10.0, "§9"),
        EPIC(4.0, 8.0, 4.0, "§5"),
        LEGENDARY(10.0, 20.0, 1.0, "§6§l");

        private final double minMultiplier;
        private final double maxMultiplier;
        private final double probability; // percentage
        private final String color;

        Rarity(double minMultiplier, double maxMultiplier, double probability, String color) {
            this.minMultiplier = minMultiplier;
            this.maxMultiplier = maxMultiplier;
            this.probability = probability;
            this.color = color;
        }

        public double getMinMultiplier() {
            return minMultiplier;
        }

        public double getMaxMultiplier() {
            return maxMultiplier;
        }

        public double getProbability() {
            return probability;
        }

        public String getColor() {
            return color;
        }
    }

    public CaseOpening(GamblingPlugin plugin) {
        super(plugin, "case");
    }

    @Override
    public void play(Player player, double bet, String[] args) {
        // This method is not used for case opening
        // Cases are opened through the GUI system
        player.sendMessage("§c[VieGambling] Usa /gamble para abrir cajas");
    }

    public void openCase(Player player, CaseType caseType) {
        double price = caseType.getPrice();

        // Check balance
        if (!plugin.getEconomyManager().has(player, price)) {
            player.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds"));
            return;
        }

        // Withdraw payment
        plugin.getEconomyManager().withdraw(player, price);

        // Roll rarity
        Rarity rarity = rollRarity();

        // Calculate winnings
        double multiplier = calculateMultiplier(rarity);
        double winnings = price * multiplier;

        // Display result through animation GUI
        com.gamblingplugin.gui.CaseAnimationGUI animGUI = new com.gamblingplugin.gui.CaseAnimationGUI(plugin);
        animGUI.open(player, caseType, rarity, price, winnings);

        // Play structure animation
        com.gamblingplugin.structures.CaseStructure structure = plugin.getStructureManager()
                .getCaseStructure(player.getLocation());
        if (structure != null) {
            structure.playOpenAnimation();
        }

        // Process winnings after animation (5 seconds)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            finalizeOpening(player, price, rarity, winnings);
        }, 100L); // 5 seconds
    }

    private void finalizeOpening(Player player, double bet, Rarity rarity, double winnings) {
        // Deposit winnings
        plugin.getEconomyManager().deposit(player, winnings);

        // Stats
        if (winnings > bet) {
            plugin.getStatsManager().addWin(player, winnings - bet);
        } else {
            plugin.getStatsManager().addLoss(player, bet - winnings);
        }

        // Contribute to jackpot
        plugin.getJackpotManager().contributeBet(bet);

        // Try to win jackpot
        if (plugin.getJackpotManager().tryWinJackpot(player, bet)) {
            player.sendMessage("");
            player.sendMessage("§6§l¡BONUS! §7¡También ganaste el §6§lJACKPOT§7!");
            player.sendMessage("");
        }

        // Send result message
        player.sendMessage("");
        player.sendMessage("§6§l[CASE OPENING] §7Resultado:");
        player.sendMessage("§7Rareza: " + rarity.getColor() + rarity.name());
        player.sendMessage("§7Ganancia: §a$" + String.format("%.2f", winnings));

        if (winnings > bet) {
            player.sendMessage("§a§l¡GANASTE $" + String.format("%.2f", winnings - bet) + "!");
        } else if (winnings < bet) {
            player.sendMessage("§c§lPerdiste $" + String.format("%.2f", bet - winnings));
        } else {
            player.sendMessage("§e§lEmpate");
        }
        player.sendMessage("");
    }

    private Rarity rollRarity() {
        double roll = random.nextDouble() * 100.0;
        double cumulative = 0.0;

        for (Rarity rarity : Rarity.values()) {
            cumulative += rarity.getProbability();
            if (roll < cumulative) {
                return rarity;
            }
        }

        return Rarity.COMMON; // Fallback
    }

    private double calculateMultiplier(Rarity rarity) {
        double min = rarity.getMinMultiplier();
        double max = rarity.getMaxMultiplier();
        return min + (random.nextDouble() * (max - min));
    }
}
