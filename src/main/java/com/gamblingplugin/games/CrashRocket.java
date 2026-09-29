package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.effects.DisplayManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CrashRocket extends Game {

    private final Map<UUID, Double> activeBettors = new HashMap<>();
    private final Set<UUID> cashedOutPlayers = new HashSet<>();
    
    private double currentMultiplier = 1.00;
    private double crashPoint = 1.00;
    private GameState state = GameState.IDLE;
    private BukkitTask activeTask;
    private int countdownSeconds = 8;

    public enum GameState {
        IDLE, COUNTDOWN, FLYING, CRASHED
    }

    public CrashRocket(GamblingPlugin plugin) {
        super(plugin, "crash");
    }

    public GameState getState() {
        return state;
    }

    public double getCurrentMultiplier() {
        return currentMultiplier;
    }

    @Override
    public void play(Player player, double bet, String[] args) {
        if (args != null && args.length > 0 && args[0].equalsIgnoreCase("cashout")) {
            cashOut(player);
            return;
        }
        placeBet(player, bet);
    }

    public synchronized boolean placeBet(Player player, double amount) {
        if (state == GameState.FLYING) {
            player.sendMessage(ChatColor.RED + "✖ ¡El cohete ya está volando! Espera a la siguiente ronda.");
            return false;
        }

        if (!plugin.getEconomyManager().hasEnough(player, amount)) {
            DisplayManager.showInsufficientFunds(player, amount);
            return false;
        }

        plugin.getEconomyManager().withdraw(player, amount);
        activeBettors.put(player.getUniqueId(), amount);
        player.sendMessage(ChatColor.GREEN + "✓ ¡Apuesta de " + plugin.getEconomyManager().format(amount) + " colocada para el siguiente despegue!");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);

        if (state == GameState.IDLE) {
            startCountdown();
        }
        return true;
    }

    public synchronized void cashOut(Player player) {
        if (state != GameState.FLYING) {
            player.sendMessage(ChatColor.RED + "✖ No puedes retirar ahora mismo.");
            return;
        }

        UUID uuid = player.getUniqueId();
        if (!activeBettors.containsKey(uuid) || cashedOutPlayers.contains(uuid)) {
            player.sendMessage(ChatColor.RED + "✖ No tienes una apuesta activa en vuelo.");
            return;
        }

        cashedOutPlayers.add(uuid);
        double bet = activeBettors.get(uuid);
        double winAmount = bet * currentMultiplier;

        plugin.getEconomyManager().deposit(player, winAmount);
        plugin.getStatsManager().addWin(player, winAmount);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.6f);
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1.2f);
        player.spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation().add(0, 1, 0), 20, 0.4, 0.4, 0.4, 0.05);

        player.sendTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "¡RETIRADO!",
                ChatColor.YELLOW + "x" + String.format("%.2f", currentMultiplier) + " | +" + plugin.getEconomyManager().format(winAmount),
                5, 30, 10);

        Bukkit.broadcastMessage(ChatColor.GOLD + "🚀 [Crash] " + ChatColor.YELLOW + player.getName() +
                ChatColor.GREEN + " se retiró en " + ChatColor.BOLD + "x" + String.format("%.2f", currentMultiplier) +
                ChatColor.GREEN + " (" + plugin.getEconomyManager().format(winAmount) + ")");
    }

    private void startCountdown() {
        state = GameState.COUNTDOWN;
        countdownSeconds = 8;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (countdownSeconds > 0) {
                    for (UUID uuid : activeBettors.keySet()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                    new TextComponent(ChatColor.GOLD + "🚀 Despegue en: " + ChatColor.YELLOW + countdownSeconds + "s" +
                                            ChatColor.GRAY + " | Apuestas: " + activeBettors.size()));
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.6f, 1.0f);
                        }
                    }
                    countdownSeconds--;
                } else {
                    this.cancel();
                    launchRocket();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void launchRocket() {
        state = GameState.FLYING;
        cashedOutPlayers.clear();
        currentMultiplier = 1.00;

        double r = ThreadLocalRandom.current().nextDouble();
        if (r < 0.04) {
            crashPoint = 1.00;
        } else {
            crashPoint = Math.min(250.0, 0.96 / (1.0 - r));
        }

        activeTask = new BukkitRunnable() {
            double elapsedTicks = 0;

            @Override
            public void run() {
                elapsedTicks += 2;
                currentMultiplier = 1.00 + Math.pow(elapsedTicks / 25.0, 1.8) * 0.18;

                for (UUID uuid : activeBettors.keySet()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null && p.isOnline()) {
                        if (!cashedOutPlayers.contains(uuid)) {
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                    new TextComponent(ChatColor.GOLD + "🚀 MULTIPLICADOR: " + ChatColor.GREEN + ChatColor.BOLD +
                                            String.format("%.2fx", currentMultiplier) +
                                            ChatColor.YELLOW + " [Escribe /gamble crash cashout]"));
                        }
                    }
                }

                if (currentMultiplier >= crashPoint) {
                    this.cancel();
                    onCrash();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void onCrash() {
        state = GameState.CRASHED;

        for (UUID uuid : activeBettors.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                if (!cashedOutPlayers.contains(uuid)) {
                    double bet = activeBettors.get(uuid);
                    plugin.getStatsManager().addLoss(p, bet);
                    p.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "💥 ¡CRASH!",
                            ChatColor.GRAY + "El cohete explotó a x" + String.format("%.2f", crashPoint),
                            5, 40, 10);
                    p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.2f);
                }
            }
        }

        Bukkit.broadcastMessage(ChatColor.RED + "💥 [Crash] ¡El cohete explotó a " + ChatColor.BOLD + "x" + String.format("%.2f", crashPoint) + "!");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            activeBettors.clear();
            cashedOutPlayers.clear();
            state = GameState.IDLE;
        }, 80L);
    }
}
