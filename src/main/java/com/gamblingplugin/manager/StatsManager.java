package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class StatsManager {

    private final GamblingPlugin plugin;
    private File statsFile;
    private FileConfiguration statsConfig;

    public StatsManager(GamblingPlugin plugin) {
        this.plugin = plugin;
        loadStats();
    }

    private void loadStats() {
        statsFile = new File(plugin.getDataFolder(), "stats.yml");
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create stats.yml!");
            }
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
    }

    public void saveStats() {
        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save stats.yml!");
        }
    }

    public void addWin(Player player, double amount) {
        String path = "players." + player.getUniqueId();
        statsConfig.set(path + ".name", player.getName());
        statsConfig.set(path + ".wins", getWins(player) + 1);
        statsConfig.set(path + ".total-won", getTotalWon(player) + amount);
        saveStats();
    }

    public void addLoss(Player player, double amount) {
        String path = "players." + player.getUniqueId();
        statsConfig.set(path + ".name", player.getName());
        statsConfig.set(path + ".losses", getLosses(player) + 1);
        statsConfig.set(path + ".total-lost", getTotalLost(player) + amount);
        saveStats();
    }

    public int getWins(Player player) {
        return statsConfig.getInt("players." + player.getUniqueId() + ".wins", 0);
    }

    public int getLosses(Player player) {
        return statsConfig.getInt("players." + player.getUniqueId() + ".losses", 0);
    }

    public double getTotalWon(Player player) {
        return statsConfig.getDouble("players." + player.getUniqueId() + ".total-won", 0);
    }

    public double getTotalLost(Player player) {
        return statsConfig.getDouble("players." + player.getUniqueId() + ".total-lost", 0);
    }

    public Map<String, Double> getTopWinners(int limit) {
        Map<String, Double> winners = new HashMap<>();
        if (statsConfig.getConfigurationSection("players") == null)
            return winners;

        for (String uuid : statsConfig.getConfigurationSection("players").getKeys(false)) {
            String name = statsConfig.getString("players." + uuid + ".name");
            double won = statsConfig.getDouble("players." + uuid + ".total-won");
            winners.put(name, won);
        }

        return winners.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}
