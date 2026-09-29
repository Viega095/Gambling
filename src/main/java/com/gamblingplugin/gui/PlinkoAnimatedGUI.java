package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlinkoAnimatedGUI {

    private final GamblingPlugin plugin;
    private final String mode;
    private static final String TITLE = "§c§lPLINKO - ";
    private final Random random = new Random();

    // Multipliers for each mode
    private static final Map<String, double[]> MODE_MULTIPLIERS = new HashMap<>();

    static {
        MODE_MULTIPLIERS.put("SAFE", new double[] { 1.3, 1.1, 0.9, 0.7, 0.5, 0.7, 0.9, 1.1, 1.3 });
        MODE_MULTIPLIERS.put("NORMAL", new double[] { 10.0, 5.0, 2.0, 1.0, 0.5, 1.0, 2.0, 5.0, 10.0 });
        MODE_MULTIPLIERS.put("EXTREME", new double[] { 50.0, 25.0, 10.0, 2.0, 0.2, 2.0, 10.0, 25.0, 50.0 });
    }

    public PlinkoAnimatedGUI(GamblingPlugin plugin, String mode) {
        this.plugin = plugin;
        this.mode = mode;
    }

    public void playAnimation(Player player, double betPerBall, int totalBalls) {
        String title = TITLE + getModeName();
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Initial setup with pyramid
        setupPyramid(inv);
        player.openInventory(inv);

        // Queue to track ball results
        List<BallResult> ballResults = new ArrayList<>();

        // ADAPTIVE TIMING: More balls = faster animation
        long dropDelay;
        long animSpeed;
        if (totalBalls >= 10) {
            // 10+ balls: SUPER FAST (0.5s per ball)
            dropDelay = 10L; // 0.5s between balls
            animSpeed = 2L; // Ultra fast animation
        } else if (totalBalls >= 5) {
            // 5-9 balls: FAST (0.7s per ball)
            dropDelay = 14L; // 0.7s between balls
            animSpeed = 2L; // Fast animation
        } else {
            // 1-4 balls: NORMAL (0.9s per ball)
            dropDelay = 18L; // 0.9s between balls
            animSpeed = 3L; // Normal animation
        }

        // Animate multiple balls
        new BukkitRunnable() {
            int currentBall = 0;
            double totalWinnings = 0;

            @Override
            public void run() {
                if (currentBall >= totalBalls) {
                    // All balls dropped - show final results
                    this.cancel();
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        player.closeInventory();
                        processResult(player, betPerBall * totalBalls, totalWinnings, ballResults);
                    }, 20L);
                    return;
                }

                // Drop one ball
                int finalSlot = simulateDrop();
                double[] multipliers = MODE_MULTIPLIERS.get(mode);
                double multiplier = multipliers[finalSlot];
                double ballWinnings = betPerBall * multiplier;
                totalWinnings += ballWinnings;

                // Store result
                ballResults.add(new BallResult(finalSlot, multiplier, ballWinnings));

                // Animate ball drop through pyramid
                animateBallDrop(inv, finalSlot, multiplier, currentBall + 1, player, animSpeed);

                currentBall++;
            }
        }.runTaskTimer(plugin, 10L, dropDelay); // Dynamic delay
    }

    private void setupPyramid(Inventory inv) {
        // Create inverted pyramid of pegs
        // Row 0 (top): 1 peg at center
        inv.setItem(4, createPeg());

        // Row 1: 2 pegs
        inv.setItem(12, createPeg());
        inv.setItem(14, createPeg());

        // Row 2: 3 pegs
        inv.setItem(20, createPeg());
        inv.setItem(22, createPeg());
        inv.setItem(24, createPeg());

        // Row 3: 4 pegs
        inv.setItem(28, createPeg());
        inv.setItem(30, createPeg());
        inv.setItem(32, createPeg());
        inv.setItem(34, createPeg());

        // Row 4: 5 pegs
        inv.setItem(36, createPeg());
        inv.setItem(38, createPeg());
        inv.setItem(40, createPeg());
        inv.setItem(42, createPeg());
        inv.setItem(44, createPeg());

        // Bottom row: Multiplier slots
        for (int i = 0; i < 9; i++) {
            inv.setItem(45 + i, createMultiplierSlot(i));
        }

        // Fill empty spaces
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta meta = filler.getItemMeta();
                meta.setDisplayName(" ");
                filler.setItemMeta(meta);
                inv.setItem(i, filler);
            }
        }
    }

    private ItemStack createPeg() {
        ItemStack peg = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = peg.getItemMeta();
        meta.setDisplayName("§8◆");
        peg.setItemMeta(meta);
        return peg;
    }

    private ItemStack createMultiplierSlot(int slot) {
        double[] multipliers = MODE_MULTIPLIERS.get(mode);
        double multiplier = multipliers[slot];
        Material material = getColorForMultiplier(multiplier);

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getColorCodeForMultiplier(multiplier) + multiplier + "x");
        item.setItemMeta(meta);
        return item;
    }

    private Material getColorForMultiplier(double mult) {
        if (mult >= 25.0)
            return Material.PURPLE_STAINED_GLASS;
        if (mult >= 10.0)
            return Material.YELLOW_STAINED_GLASS;
        if (mult >= 5.0)
            return Material.LIME_STAINED_GLASS;
        if (mult >= 2.0)
            return Material.LIGHT_BLUE_STAINED_GLASS;
        if (mult >= 1.0)
            return Material.ORANGE_STAINED_GLASS;
        if (mult >= 0.5)
            return Material.GRAY_STAINED_GLASS;
        return Material.RED_STAINED_GLASS;
    }

    private String getColorCodeForMultiplier(double mult) {
        if (mult >= 25.0)
            return "§d§l";
        if (mult >= 10.0)
            return "§6§l";
        if (mult >= 5.0)
            return "§a§l";
        if (mult >= 2.0)
            return "§b§l";
        if (mult >= 1.0)
            return "§e";
        if (mult >= 0.5)
            return "§7";
        return "§c§l";
    }

    private int simulateDrop() {
        double position = 4.0;
        for (int row = 0; row < 6; row++) {
            position += random.nextBoolean() ? 0.5 : -0.5;
        }
        int slot = (int) Math.round(position);
        return Math.max(0, Math.min(8, slot));
    }

    private void animateBallDrop(Inventory inv, int finalSlot, double multiplier, int ballNumber, Player player,
            long animSpeed) {
        // Create ball item with stack count
        ItemStack ball = new ItemStack(Material.ENDER_PEARL, ballNumber);
        ItemMeta ballMeta = ball.getItemMeta();
        ballMeta.setDisplayName("§e⬇ Bola #" + ballNumber + " cayendo...");
        ball.setItemMeta(ballMeta);

        // Animate through pyramid - ADAPTIVE SPEED
        new BukkitRunnable() {
            int step = 0;
            int[] path = { 4, 12 + (finalSlot >= 4 ? 2 : 0), 20 + Math.min(finalSlot / 2, 2) * 2,
                    28 + Math.min(finalSlot / 2, 3) * 2, 36 + Math.min(finalSlot / 2, 4) * 2 };

            @Override
            public void run() {
                if (step >= path.length) {
                    this.cancel();
                    // Flash final slot
                    flashMultiplierSlot(inv, finalSlot, multiplier, player, animSpeed);
                    return;
                }

                // Place ball at current position
                if (step > 0) {
                    inv.setItem(path[step - 1], createPeg());
                }
                inv.setItem(path[step], ball);

                // Sound
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.3f, 1.0f + (step * 0.2f));

                step++;
            }
        }.runTaskTimer(plugin, 0L, animSpeed); // Use adaptive speed
    }

    private void flashMultiplierSlot(Inventory inv, int slot, double multiplier, Player player, long animSpeed) {
        new BukkitRunnable() {
            int flashes = 0;

            @Override
            public void run() {
                if (flashes >= 4) { // FASTER: was 6 → now 4
                    this.cancel();
                    inv.setItem(45 + slot, createMultiplierSlot(slot));
                    return;
                }

                Material material = (flashes % 2 == 0) ? Material.WHITE_STAINED_GLASS
                        : getColorForMultiplier(multiplier);
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(getColorCodeForMultiplier(multiplier) + "§l" + multiplier + "x");
                item.setItemMeta(meta);
                inv.setItem(45 + slot, item);

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);
                flashes++;
            }
        }.runTaskTimer(plugin, 0L, animSpeed); // Use adaptive speed
    }

    private void processResult(Player player, double totalBet, double totalWinnings, List<BallResult> results) {
        plugin.getEconomyManager().deposit(player, totalWinnings);

        if (totalWinnings > totalBet) {
            plugin.getStatsManager().addWin(player, totalWinnings - totalBet);
        } else {
            plugin.getStatsManager().addLoss(player, totalBet - totalWinnings);
        }

        plugin.getJackpotManager().contributeBet(totalBet);

        // Detailed messages
        player.sendMessage("");
        player.sendMessage("§6§l═══════════════════════════");
        player.sendMessage("§6§l[PLINKO " + getModeName() + "§6§l] §7Resultados:");
        player.sendMessage("");

        // Show each ball result
        for (BallResult result : results) {
            String color = result.multiplier >= 1.0 ? "§a" : "§c";
            player.sendMessage("  §7Bola → " + result.getColoredMultiplier() + " §7= " + color + "$"
                    + String.format("%.2f", result.winnings));
        }

        player.sendMessage("");
        player.sendMessage("§7Total apostado: §c$" + String.format("%.2f", totalBet));
        player.sendMessage("§7Total ganado: §a$" + String.format("%.2f", totalWinnings));

        if (totalWinnings > totalBet) {
            double profit = totalWinnings - totalBet;
            player.sendMessage("§a§l✦ GANANCIA: +$" + String.format("%.2f", profit) + " ✦");

            if (profit >= totalBet * 5) {
                player.sendMessage("§6§l✧✦ ¡MEGA GANANCIA! ✦✧");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
            }
        } else {
            double loss = totalBet - totalWinnings;
            player.sendMessage("§c§l✖ PÉRDIDA: -$" + String.format("%.2f", loss) + " ✖");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1f);
        }

        player.sendMessage("§6§l═══════════════════════════");
        player.sendMessage("");

        if (plugin.getJackpotManager().tryWinJackpot(player, totalBet)) {
            player.sendMessage("§6§l✦ ¡BONUS JACKPOT! ✦");
        }
    }

    private String getModeName() {
        switch (mode) {
            case "SAFE":
                return "§a🛡️";
            case "EXTREME":
                return "§c💀";
            default:
                return "§e⚖️";
        }
    }

    // Helper class to store ball results
    private static class BallResult {
        int slot;
        double multiplier;
        double winnings;

        BallResult(int slot, double multiplier, double winnings) {
            this.slot = slot;
            this.multiplier = multiplier;
            this.winnings = winnings;
        }

        String getColoredMultiplier() {
            if (multiplier >= 25.0)
                return "§d§l" + multiplier + "x";
            if (multiplier >= 10.0)
                return "§6§l" + multiplier + "x";
            if (multiplier >= 5.0)
                return "§a§l" + multiplier + "x";
            if (multiplier >= 2.0)
                return "§b" + multiplier + "x";
            if (multiplier >= 1.0)
                return "§e" + multiplier + "x";
            return "§c" + multiplier + "x";
        }
    }
}
