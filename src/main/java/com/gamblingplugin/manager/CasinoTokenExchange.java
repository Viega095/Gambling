package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class CasinoTokenExchange {

    private final GamblingPlugin plugin;
    private double tokenPrice = 100.0; // Base $100 per CHIP
    private final Map<UUID, Double> playerTokens = new HashMap<>();

    public CasinoTokenExchange(GamblingPlugin plugin) {
        this.plugin = plugin;
        startMarketFluctuationTask();
    }

    private void startMarketFluctuationTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Price fluctuates +/- 10%
            double changePercent = (ThreadLocalRandom.current().nextDouble() * 0.20) - 0.10;
            tokenPrice = Math.max(10.0, Math.min(1000.0, tokenPrice * (1.0 + changePercent)));
        }, 600L, 600L); // Every 30 seconds
    }

    public double getTokenPrice() {
        return tokenPrice;
    }

    public double getTokens(UUID uuid) {
        return playerTokens.getOrDefault(uuid, 0.0);
    }

    public boolean buyTokens(Player player, double chipsAmount) {
        double cost = chipsAmount * tokenPrice;
        if (!plugin.getEconomyManager().has(player, cost)) {
            player.sendMessage(ChatColor.RED + "✖ Fondos insuficientes para comprar " + chipsAmount + " $CHIPS (" +
                    plugin.getEconomyManager().format(cost) + ")");
            return false;
        }

        plugin.getEconomyManager().withdraw(player, cost);
        playerTokens.put(player.getUniqueId(), getTokens(player.getUniqueId()) + chipsAmount);

        player.sendMessage(ChatColor.GOLD + "🪙 [Exchange] ¡Has comprado " + ChatColor.YELLOW + chipsAmount + " $CHIPS " +
                ChatColor.GOLD + "por " + plugin.getEconomyManager().format(cost) + "!");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
        return true;
    }

    public boolean sellTokens(Player player, double chipsAmount) {
        double current = getTokens(player.getUniqueId());
        if (current < chipsAmount) {
            player.sendMessage(ChatColor.RED + "✖ No posees suficientes $CHIPS (Tienes: " + current + ")");
            return false;
        }

        double revenue = chipsAmount * tokenPrice;
        playerTokens.put(player.getUniqueId(), current - chipsAmount);
        plugin.getEconomyManager().deposit(player, revenue);

        player.sendMessage(ChatColor.GREEN + "💰 [Exchange] ¡Has vendido " + ChatColor.YELLOW + chipsAmount + " $CHIPS " +
                ChatColor.GREEN + "por " + plugin.getEconomyManager().format(revenue) + "!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.4f);
        return true;
    }
}
