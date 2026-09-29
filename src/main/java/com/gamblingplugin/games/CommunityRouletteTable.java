package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CommunityRouletteTable {

    public enum BetType {
        RED, BLACK, GREEN, NUMBER
    }

    public static class ActiveBet {
        public final Player player;
        public final BetType type;
        public final int number; // only if type == NUMBER
        public final double amount;

        public ActiveBet(Player player, BetType type, int number, double amount) {
            this.player = player;
            this.type = type;
            this.number = number;
            this.amount = amount;
        }
    }

    private final GamblingPlugin plugin;
    private final List<ActiveBet> bets = new ArrayList<>();
    private int countdownSeconds = 30;
    private boolean spinning = false;

    public CommunityRouletteTable(GamblingPlugin plugin) {
        this.plugin = plugin;
        startTableLoop();
    }

    public synchronized boolean placeBet(Player player, BetType type, int number, double amount) {
        if (spinning) {
            player.sendMessage(ChatColor.RED + "✖ La ruleta está girando. Espera al siguiente giro.");
            return false;
        }

        if (!plugin.getEconomyManager().hasEnough(player, amount)) {
            player.sendMessage(ChatColor.RED + "✖ Fondos insuficientes (" + plugin.getEconomyManager().format(amount) + ")");
            return false;
        }

        plugin.getEconomyManager().withdraw(player, amount);
        bets.add(new ActiveBet(player, type, number, amount));

        player.sendMessage(ChatColor.GREEN + "✓ ¡Apuesta de " + plugin.getEconomyManager().format(amount) +
                " colocada en " + type.name() + (type == BetType.NUMBER ? " [" + number + "]" : "") + "!");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.4f);
        return true;
    }

    private void startTableLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!spinning) {
                    if (countdownSeconds > 0) {
                        countdownSeconds--;
                    } else {
                        if (!bets.isEmpty()) {
                            spinWheel();
                        } else {
                            countdownSeconds = 30; // Reset timer if no bets
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void spinWheel() {
        spinning = true;
        int targetNumber = ThreadLocalRandom.current().nextInt(37); // 0 to 36
        String color = (targetNumber == 0) ? "GREEN" : (isRed(targetNumber) ? "RED" : "BLACK");

        Bukkit.broadcastMessage(ChatColor.GOLD + "🎰 [Ruleta Comunitaria] ¡La bola está girando en el cilindro...!");

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;
                if (ticks < 10) {
                    for (ActiveBet bet : bets) {
                        bet.player.playSound(bet.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.0f + (ticks * 0.1f));
                    }
                } else {
                    this.cancel();
                    resolvePayouts(targetNumber, color);
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void resolvePayouts(int number, String color) {
        ChatColor cColor = color.equals("RED") ? ChatColor.RED : (color.equals("GREEN") ? ChatColor.GREEN : ChatColor.DARK_GRAY);

        Bukkit.broadcastMessage(ChatColor.GOLD + "🎰 [Ruleta] ¡La bola cayó en: " + cColor + ChatColor.BOLD + number + " (" + color + ")!");

        for (ActiveBet bet : bets) {
            double winMultiplier = 0.0;
            if (bet.type == BetType.NUMBER && bet.number == number) {
                winMultiplier = 36.0; // 36x for exact number
            } else if (bet.type == BetType.RED && color.equals("RED")) {
                winMultiplier = 2.0;
            } else if (bet.type == BetType.BLACK && color.equals("BLACK")) {
                winMultiplier = 2.0;
            } else if (bet.type == BetType.GREEN && color.equals("GREEN")) {
                winMultiplier = 14.0;
            }

            if (winMultiplier > 0) {
                double winAmount = bet.amount * winMultiplier;
                plugin.getEconomyManager().deposit(bet.player, winAmount);
                plugin.getStatsManager().addWin(bet.player, winAmount);
                bet.player.sendMessage(ChatColor.GREEN + "✨ ¡GANASTE! Recibes " + plugin.getEconomyManager().format(winAmount));
                bet.player.playSound(bet.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                bet.player.spawnParticle(Particle.FIREWORKS_SPARK, bet.player.getLocation().add(0, 1, 0), 25, 0.4, 0.4, 0.4, 0.1);
            } else {
                plugin.getStatsManager().addLoss(bet.player, bet.amount);
                bet.player.sendMessage(ChatColor.RED + "✖ No acertaste esta ronda.");
            }
        }

        bets.clear();
        countdownSeconds = 30;
        spinning = false;
    }

    private boolean isRed(int num) {
        int[] reds = {1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36};
        for (int r : reds) if (num == r) return true;
        return false;
    }
}
