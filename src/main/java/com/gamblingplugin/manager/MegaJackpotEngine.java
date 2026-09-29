package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

public class MegaJackpotEngine {

    private final GamblingPlugin plugin;
    private double currentPool = 10000.0;
    private final double contributionRate = 0.02; // 2% of every bet

    public MegaJackpotEngine(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public double getCurrentPool() {
        return currentPool;
    }

    public void addContribution(double betAmount) {
        this.currentPool += (betAmount * contributionRate);
    }

    public void triggerMegaJackpotWin(Player winner) {
        double won = currentPool;
        currentPool = 10000.0; // Reset base

        plugin.getEconomyManager().deposit(winner, won);
        plugin.getStatsManager().addWin(winner, won);

        // Global Celebration
        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "========================================");
        Bukkit.broadcastMessage(ChatColor.GOLD + "✦✦✦ " + ChatColor.YELLOW + ChatColor.BOLD + "¡MEGA JACKPOT GLOBAL ALCANZADO!" + ChatColor.GOLD + " ✦✦✦");
        Bukkit.broadcastMessage(ChatColor.GREEN + "  Jugador: " + ChatColor.WHITE + winner.getName());
        Bukkit.broadcastMessage(ChatColor.GOLD + "  Premio Millonario: " + ChatColor.YELLOW + ChatColor.BOLD + plugin.getEconomyManager().format(won));
        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "========================================");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(ChatColor.GOLD + "✦ MEGA JACKPOT ✦",
                    ChatColor.YELLOW + winner.getName() + " ganó " + plugin.getEconomyManager().format(won),
                    10, 60, 20);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }

        // Spawn fireworks at winner location
        Location loc = winner.getLocation();
        for (int i = 0; i < 5; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Firework fw = loc.getWorld().spawn(loc, Firework.class);
                FireworkMeta meta = fw.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder().withColor(Color.YELLOW, Color.ORANGE, Color.PURPLE).with(FireworkEffect.Type.BALL_LARGE).build());
                meta.setPower(1);
                fw.setFireworkMeta(meta);
            }, i * 10L);
        }
    }
}
