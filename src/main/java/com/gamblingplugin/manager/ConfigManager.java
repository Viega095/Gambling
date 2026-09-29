package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final GamblingPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(GamblingPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getMessage(String path) {
        return config.getString("messages." + path, "&cMessage not found: " + path).replace("&", "§");
    }

    public double getMinBet(String game) {
        return config.getDouble("games." + game + ".min-bet", 0);
    }

    public double getMaxBet(String game) {
        return config.getDouble("games." + game + ".max-bet", 1000000);
    }
}
