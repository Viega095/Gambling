package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.effects.ParticleEffects;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

public class NotificationManager {

    private final GamblingPlugin plugin;

    // Configurable thresholds
    private double bigWinThreshold = 1000.0;
    private int streakNotificationMin = 3;

    public NotificationManager(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    // ==================== JACKPOT NOTIFICATIONS ====================

    public void notifyJackpotWin(Player winner, double amount) {
        // Broadcast to entire server
        String message = "§6§l✦✦✦ JACKPOT ✦✦✦\n" +
                "§e" + winner.getName() + " §7ganó el §6§lJACKPOT §7de §a§l$" +
                String.format("%.2f", amount) + "§7!";

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(message);
        Bukkit.broadcastMessage("");

        // Play sound and effects for all online players
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.3f, 2.0f);

            // Send title
            p.sendTitle("§6§l✦ JACKPOT ✦",
                    "§e" + winner.getName() + " §7- §a$" + String.format("%.2f", amount),
                    20, 60, 20);
        }

        // Particle effect at winner location
        ParticleEffects.playJackpotEffect(winner.getLocation());
    }

    // ==================== BIG WIN NOTIFICATIONS ====================

    public void notifyBigWin(Player winner, String gameName, double amountWon) {
        if (amountWon < bigWinThreshold) {
            return; // Not big enough
        }

        // Broadcast to server
        String message = "§6§l⚡ GRAN VICTORIA §6§l⚡\n" +
                "§e" + winner.getName() + " §7ganó §a$" + String.format("%.2f", amountWon) +
                " §7en §6" + gameName + "§7!";

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(message);
        Bukkit.broadcastMessage("");

        // Play sound for all players
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        }
    }

    // ==================== WIN STREAK NOTIFICATIONS ====================

    public void notifyWinStreak(Player player, int streakCount, String gameName) {
        if (streakCount < streakNotificationMin) {
            return;
        }

        // Broadcast streak achievement
        String message = "§d§l⚡ RACHA INCREÍBLE ⚡\n" +
                "§e" + player.getName() + " §7tiene §d§l" + streakCount +
                " victorias consecutivas §7en §6" + gameName + "§7!";

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(message);
        Bukkit.broadcastMessage("");

        // Sound and effects
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_BELL_USE, 0.7f, 1.0f + (streakCount * 0.1f));
        }

        ParticleEffects.playWinStreakEffect(player, streakCount);
    }

    // ==================== FIRST WIN OF DAY ====================

    public void notifyFirstWin(Player player) {
        player.sendMessage("");
        player.sendMessage("§d§l⭐ ¡PRIMERA VICTORIA DEL DÍA! ⭐");
        player.sendMessage("§7Has recibido un bonus especial");
        player.sendMessage("");

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        com.gamblingplugin.effects.DisplayManager.showFirstWinBonus(player);
    }

    // ==================== ADMIN NOTIFICATIONS ====================

    public void notifyAdmins(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("gambling.admin")) {
                p.sendMessage("§c[Admin] §7" + message);
            }
        }
    }

    public void notifyStructureCreated(Player admin, String structureType) {
        admin.sendMessage("§a✓ Estructura creada: §6" + structureType);
        admin.playSound(admin.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 2.0f);
    }

    public void notifyStructureRemoved(Player admin, String structureType) {
        admin.sendMessage("§c✖ Estructura eliminada: §6" + structureType);
        admin.playSound(admin.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 1.0f);
    }

    // ==================== PERSONAL NOTIFICATIONS ====================

    public void notifyWin(Player player, String gameName, double amountWon) {
        player.sendMessage("§a§l[" + gameName + "] ¡Ganaste $" + String.format("%.2f", amountWon) + "!");
        ParticleEffects.playWinEffect(player, amountWon);
        com.gamblingplugin.effects.DisplayManager.showWinTitle(player, amountWon);
    }

    public void notifyLoss(Player player, String gameName) {
        player.sendMessage("§c[" + gameName + "] Perdiste. ¡Mejor suerte la próxima!");
        ParticleEffects.playLossEffect(player);
        com.gamblingplugin.effects.DisplayManager.showLossTitle(player);
    }

    // ==================== CONFIGURATION ====================

    public void setBigWinThreshold(double threshold) {
        this.bigWinThreshold = threshold;
    }

    public double getBigWinThreshold() {
        return bigWinThreshold;
    }

    public void setStreakNotificationMin(int min) {
        this.streakNotificationMin = min;
    }

    public int getStreakNotificationMin() {
        return streakNotificationMin;
    }
}
