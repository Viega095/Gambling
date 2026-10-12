package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.CoinflipStructure;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Coinflip {

    private final GamblingPlugin plugin;
    private final Random random = new Random();

    public enum Side {
        HEADS, TAILS
    }

    public Coinflip(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public static class StreakSession {
        public double initialBet;
        public int streakCount;
        public double currentPrize;
        public StreakSession(double initialBet) {
            this.initialBet = initialBet;
            this.streakCount = 0;
            this.currentPrize = initialBet;
        }
    }

    private final Map<UUID, StreakSession> streakSessions = new HashMap<>();

    public StreakSession getStreakSession(Player player) {
        return streakSessions.get(player.getUniqueId());
    }

    public void cashoutStreak(Player player) {
        StreakSession session = streakSessions.remove(player.getUniqueId());
        if (session == null || session.streakCount == 0) {
            player.sendMessage("§c[VieGambling] No tienes ninguna racha activa para cobrar.");
            return;
        }
        plugin.getEconomyManager().deposit(player, session.currentPrize);
        player.sendMessage("§a§l[VieGambling] ¡COBRO EXITOSO!");
        player.sendMessage("§7Cobraste tu racha de §e" + session.streakCount + " victorias §7por §a$" + String.format("%.2f", session.currentPrize));
        player.sendTitle("§a§l¡RACHA COBRADA!", "§a+" + plugin.getEconomyManager().format(session.currentPrize), 5, 50, 15);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
    }

    public void play(Player player, double bet, String selection) {
        if (selection.equalsIgnoreCase("cashout") || selection.equalsIgnoreCase("cobrar")) {
            cashoutStreak(player);
            return;
        }

        boolean isStreak = selection.toUpperCase().startsWith("STREAK_");
        String sideStr = isStreak ? selection.substring(7) : selection;

        if (sideStr.equalsIgnoreCase("CARA")) sideStr = "HEADS";
        if (sideStr.equalsIgnoreCase("CRUZ")) sideStr = "TAILS";

        Side chosenSide;
        try {
            chosenSide = Side.valueOf(sideStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c[VieGambling] Lado inválido. Usa HEADS, TAILS, CARA o CRUZ (o STREAK_HEADS/STREAK_TAILS)");
            return;
        }

        // Check cooldown
        if (plugin.getCooldownManager().hasCooldown(player, "coinflip")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player, "coinflip");
            player.sendMessage("§c[VieGambling] Espera " + remaining + " segundos");
            return;
        }

        if (isStreak) {
            StreakSession session = streakSessions.get(player.getUniqueId());
            if (session == null) {
                // First round of streak mode: charge bet
                if (!plugin.getEconomyManager().has(player, bet)) {
                    player.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds"));
                    return;
                }
                plugin.getEconomyManager().withdraw(player, bet);
                session = new StreakSession(bet);
                streakSessions.put(player.getUniqueId(), session);
            }
        } else {
            // Standard Coinflip: charge bet
            if (!plugin.getEconomyManager().has(player, bet)) {
                player.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds"));
                return;
            }
            plugin.getEconomyManager().withdraw(player, bet);
        }

        plugin.getJackpotManager().contributeBet(bet);
        plugin.getCooldownManager().setCooldown(player, "coinflip");

        player.sendMessage("");
        player.sendMessage("§6§l[VieGambling] Coinflip 3D" + (isStreak ? " §e[MODO RACHA]" : ""));
        player.sendMessage("§7Tu elección: §e" + (chosenSide == Side.HEADS ? "Cara (HEADS)" : "Cruz (TAILS)"));
        player.sendMessage("§7Apuesta: §a$" + String.format("%.2f", bet));
        player.sendMessage("");

        // Determine result
        Side result = random.nextBoolean() ? Side.HEADS : Side.TAILS;

        // Check if near a 3D physical coinflip structure
        CoinflipStructure structure = plugin.getStructureManager().getCoinflipStructure(player.getLocation());
        if (structure != null) {
            structure.flip3D(player, result, () -> {
                finalizeGame(player, bet, chosenSide, result, isStreak);
            });
        } else {
            com.gamblingplugin.gui.CoinflipSpinGUI spinGUI = new com.gamblingplugin.gui.CoinflipSpinGUI(plugin);
            spinGUI.open(player, chosenSide, result, bet);

            new BukkitRunnable() {
                @Override
                public void run() {
                    finalizeGame(player, bet, chosenSide, result, isStreak);
                }
            }.runTaskLater(plugin, 70L);
        }
    }

    private void finalizeGame(Player player, double bet, Side chosenSide, Side result, boolean isStreak) {
        String sideName = (result == Side.HEADS ? "§6Cara (HEADS)" : "§7Cruz (TAILS)");

        player.sendMessage("");
        player.sendMessage("§6§l[VieGambling] §7Resultado: " + sideName);
        player.sendMessage("");

        if (chosenSide == result) {
            if (isStreak) {
                StreakSession session = streakSessions.get(player.getUniqueId());
                if (session == null) session = new StreakSession(bet);
                session.streakCount++;

                double multiplier = switch (session.streakCount) {
                    case 1 -> 1.9;
                    case 2 -> 3.8;
                    case 3 -> 7.6;
                    case 4 -> 15.2;
                    default -> 15.2 * Math.pow(2, session.streakCount - 4);
                };
                session.currentPrize = session.initialBet * multiplier;

                player.sendMessage("§a§l[VieGambling] ¡ACIERTO EN RACHA! §e(" + session.streakCount + " seguidas)");
                player.sendMessage("§7Pozo acumulado: §a$" + String.format("%.2f", session.currentPrize) + " §7(" + String.format("%.1f", multiplier) + "x)");
                player.sendMessage("§e▶ Usa §f/gamble coinflip cobrar §eo el menú para retirar tus ganancias.");
                player.sendMessage("");

                player.sendTitle("§a§l¡RACHA X" + session.streakCount + "!", "§ePozo: " + plugin.getEconomyManager().format(session.currentPrize), 5, 50, 15);
            } else {
                // Standard WIN
                double payout = bet * 2;
                plugin.getEconomyManager().deposit(player, payout);
                player.sendMessage("§a§l[VieGambling] ¡GANASTE!");
                player.sendMessage("§7Premio: §a$" + String.format("%.2f", payout) + " §7(2.0x)");
                player.sendMessage("");

                player.sendTitle("§a§l¡GANASTE!", sideName + " §a+" + plugin.getEconomyManager().format(payout), 5, 50, 15);
            }

            Location loc = player.getLocation();
            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.add(0, 1.5, 0), 30, 0.5, 0.5, 0.5, 0);
            player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 25, 0.5, 0.5, 0.5, 0.1);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.4f);
        } else {
            // LOSE
            if (isStreak) {
                streakSessions.remove(player.getUniqueId());
                player.sendMessage("§c§l[VieGambling] ¡Racha perdida! El pozo acumulado se ha esfumado.");
            } else {
                player.sendMessage("§c§l[VieGambling] Perdiste");
            }
            player.sendMessage("§7Mejor suerte la próxima vez");
            player.sendMessage("");

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
