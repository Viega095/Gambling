package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CasinoHologramEngine {

    private final GamblingPlugin plugin;
    private final List<ArmorStand> activeLines = new ArrayList<>();
    private Location hologramLocation = null;

    public CasinoHologramEngine(GamblingPlugin plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    public boolean spawnHologram(Location loc) {
        removeHologram();
        this.hologramLocation = loc.clone().add(0, 2.5, 0);

        List<String> lines = generateLines();
        Location currentLoc = this.hologramLocation.clone();

        for (String line : lines) {
            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(currentLoc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCustomName(line);
            stand.setCustomNameVisible(true);
            stand.setMarker(true);
            activeLines.add(stand);
            currentLoc.subtract(0, 0.28, 0);
        }

        return true;
    }

    public void removeHologram() {
        for (ArmorStand stand : activeLines) {
            if (stand != null && stand.isValid()) {
                stand.remove();
            }
        }
        activeLines.clear();
    }

    private List<String> generateLines() {
        List<String> lines = new ArrayList<>();
        lines.add("§6§l✦ CASINO ROYALE LEADERBOARD ✦");
        double jackpot = plugin.getJackpotManager() != null ? plugin.getJackpotManager().getJackpotAmount() : 50000.0;
        lines.add("§e🎰 Mega Jackpot Acumulado: §a" + plugin.getEconomyManager().format(jackpot));
        lines.add("§7§m--------------------------------");

        Map<String, Double> topWinners = plugin.getStatsManager().getTopWinners(3);
        int rank = 1;
        for (Map.Entry<String, Double> entry : topWinners.entrySet()) {
            String medal = rank == 1 ? "§6🥇" : (rank == 2 ? "§f🥈" : "§c🥉");
            lines.add(medal + " §e#" + rank + " " + entry.getKey() + " §7- §a" + plugin.getEconomyManager().format(entry.getValue()));
            rank++;
        }
        while (rank <= 3) {
            lines.add("§7#" + rank + " Sin Récord");
            rank++;
        }

        lines.add("§7§m--------------------------------");
        lines.add("§d⚡ ¡Escribe §e/gamble §dpara jugar y liderar!");
        return lines;
    }

    private void startUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeLines.isEmpty() || hologramLocation == null) return;

                List<String> lines = generateLines();
                for (int i = 0; i < activeLines.size() && i < lines.size(); i++) {
                    ArmorStand stand = activeLines.get(i);
                    if (stand != null && stand.isValid()) {
                        stand.setCustomName(lines.get(i));
                    }
                }
            }
        }.runTaskTimer(plugin, 200L, 200L); // Update every 10 seconds
    }
}
