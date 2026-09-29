package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CasinoPassManager {

    private final GamblingPlugin plugin;
    private final Map<UUID, Integer> passTiers = new HashMap<>();
    private final Map<UUID, Double> passExp = new HashMap<>();

    public CasinoPassManager(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public int getTier(UUID uuid) {
        return passTiers.getOrDefault(uuid, 1);
    }

    public double getExp(UUID uuid) {
        return passExp.getOrDefault(uuid, 0.0);
    }

    public void addWagerExp(Player player, double wagerAmount) {
        UUID uuid = player.getUniqueId();
        double currentExp = getExp(uuid) + (wagerAmount * 0.1);
        int currentTier = getTier(uuid);
        double expNeeded = currentTier * 500.0;

        if (currentExp >= expNeeded) {
            currentExp -= expNeeded;
            currentTier++;
            passTiers.put(uuid, currentTier);
            passExp.put(uuid, currentExp);

            player.sendMessage(ChatColor.GOLD + "🌟 [Pase de Casino VIP] ¡Has subido al Nivel " + currentTier + "!");
            player.sendMessage(ChatColor.GREEN + "🎁 ¡Recompensa de Pase desbloqueada!");
            plugin.getEconomyManager().deposit(player, currentTier * 1000.0);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.4f);
        } else {
            passExp.put(uuid, currentExp);
        }
    }

    public void showPassStatus(Player player) {
        int tier = getTier(player.getUniqueId());
        double exp = getExp(player.getUniqueId());
        double expNeeded = tier * 500.0;

        player.sendMessage(ChatColor.GOLD + "=== 👑 " + ChatColor.YELLOW + "PASE DE TEMPORADA DEL CASINO" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.AQUA + "Nivel Actual: " + ChatColor.WHITE + tier + " / 50");
        player.sendMessage(ChatColor.AQUA + "Progreso de EXP: " + ChatColor.GREEN + String.format("%.0f", exp) + " / " + String.format("%.0f", expNeeded) + " EXP");
        player.sendMessage(ChatColor.GRAY + "Gana EXP por cada apuesta que realices en el casino.");
    }
}
